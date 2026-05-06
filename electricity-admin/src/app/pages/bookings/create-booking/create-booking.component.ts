import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { Router } from "@angular/router";
import { HttpClient } from "@angular/common/http";
import { AuthService } from "../../../shared/services/auth.service";

const API_BASE = "http://192.168.0.155:8080";

@Component({
  selector: "app-create-booking",
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: "./create-booking.component.html",
  styleUrl: "./create-booking.component.css",
})
export class CreateBookingComponent implements OnInit {
  bookingForm!: FormGroup;

  customers: any[] = [];
  rates: any[] = [];
  selectedRate: any = null;
  baseProvider: any = null;
  selectedCustomerId: number | null = 0;

  isLoading = false;
  isLoadingRates = false;

  successMessage = "";
  errorMessage = "";
  rateError = "";

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private authService: AuthService,
  ) {}

  searchForm = {
    zip: "",
    city: "",
    street: "",
    houseNumber: "",
    consum: 4350,
    type: "private",
    branch: "electric",
  };

  ngOnInit(): void {
    this.initForm();
    this.loadCustomers();
    this.setupDynamicValidation();
  }

  onCustomerSelect(): void {
    const customer = this.customers.find(
      (c) => c.id === this.selectedCustomerId,
    );

    if (!customer?.address) return;

    this.searchForm = {
      ...this.searchForm,
      zip: customer.address.zip || "",
      city: customer.address.city || "",
      street: customer.address.street || "",
      houseNumber: customer.address.houseNumber || "",
    };
  }

  initForm() {
    this.bookingForm = this.fb.group({
      /* Step 1 */
      customerId: ["", Validators.required],

      zip: ["", Validators.required],
      city: ["", Validators.required],
      street: ["", Validators.required],
      houseNumber: ["", Validators.required],
      consumption: [4350, Validators.required],

      /* Delivery */
      deliveryEmail: ["", Validators.required],
      deliveryFirstName: ["", Validators.required],
      deliveryLastName: ["", Validators.required],
      deliveryMobile: ["", Validators.required],
      deliveryPhone: [""],
      dob: [""],

      /* Connection */
      isMovingIn: ["no"],
      moveInDate: [""],
      submitLater: [false],
      meterNumber: ["", Validators.required],
      marketLocationId: [""],

      currentProvider: [""],
      autoCancellation: [true],
      alreadyCancelled: [false],
      selfCancellation: [false],

      deliveryOption: ["schnellstmoeglich"],
      desiredDeliveryDate: [""],

      /* Payment */
      paymentMethod: ["ueberweisung"],
      iban: [""],
      accountHolderFirstName: [""],
      accountHolderLastName: [""],
      sepaConsent: [false],
    });
  }

  loadCustomers(): void {
    const payload = {
      adminId: this.authService.getUserId(),
      page: 1,
      search: "",
      userType: "",
      isVerified: "",
    };

    this.http
      .post<any>(`${API_BASE}/admin/fetch-customer-details`, payload)
      .subscribe({
        next: (res) => {
          const customerList = Array.isArray(res?.data) ? res.data : [];

          this.customers = customerList.map((customer: any) => ({
            id: customer.id,
            firstName: customer.firstName ?? "",
            lastName: customer.lastName ?? "",
            email: customer.email ?? "",
            mobileNumber: customer.mobileNumber ?? "",
            userType: customer.userType ?? "",
            address: customer.address ?? null,
          }));
        },

        error: (err) => {
          console.error("Failed to load customers", err);

          this.errorMessage = err?.error?.message || "Failed to load customers";
        },
      });
  }

  setupDynamicValidation() {
    /* Moving in logic */
    this.bookingForm.get("isMovingIn")?.valueChanges.subscribe((value) => {
      const moveDate = this.bookingForm.get("moveInDate");
      const currentProvider = this.bookingForm.get("currentProvider");

      if (value === "yes") {
        moveDate?.setValidators([Validators.required]);
        currentProvider?.clearValidators();
      } else {
        moveDate?.clearValidators();
        currentProvider?.setValidators([Validators.required]);
      }

      moveDate?.updateValueAndValidity();
      currentProvider?.updateValueAndValidity();
    });

    /* Payment method */
    this.bookingForm.get("paymentMethod")?.valueChanges.subscribe((value) => {
      const iban = this.bookingForm.get("iban");
      const firstName = this.bookingForm.get("accountHolderFirstName");
      const lastName = this.bookingForm.get("accountHolderLastName");

      if (value === "lastschrift") {
        iban?.setValidators([Validators.required]);
        firstName?.setValidators([Validators.required]);
        lastName?.setValidators([Validators.required]);
      } else {
        iban?.clearValidators();
        firstName?.clearValidators();
        lastName?.clearValidators();
      }

      iban?.updateValueAndValidity();
      firstName?.updateValueAndValidity();
      lastName?.updateValueAndValidity();
    });

    /* Desired delivery date */
    this.bookingForm.get("deliveryOption")?.valueChanges.subscribe((value) => {
      const desiredDate = this.bookingForm.get("desiredDeliveryDate");

      if (value === "wunschtermin") {
        desiredDate?.setValidators([Validators.required]);
      } else {
        desiredDate?.clearValidators();
      }

      desiredDate?.updateValueAndValidity();
    });
  }

  fetchRates(): void {
    this.isLoadingRates = true;
    this.rateError = "";
    this.rates = [];
    this.selectedRate = null;

    const customerId = this.selectedCustomerId;
    
    const payload = {
      ...this.searchForm,
      customerId,
      adminId: this.authService.getUserId(),
    };

    this.http.post<any>(`${API_BASE}/api/get-rates`, payload).subscribe({
      next: (res) => {
        this.isLoadingRates = false;
        this.rates = res?.rates?.result ?? [];
        this.baseProvider = res?.baseProvider?.result?.[0] ?? null;
      },
      error: (err) => {
        this.isLoadingRates = false;
        this.rateError = err?.error?.message || "Failed to fetch providers";
      },
    });
  }

  selectRate(rate: any) {
    this.selectedRate = this.selectedRate?.rateId === rate.rateId ? null : rate;
  }

  async createBooking() {
    if (!this.selectedRate) {
      this.errorMessage = "Please select a provider";
      return;
    }

    if (this.bookingForm.invalid) {
      this.bookingForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = "";

    const form = this.bookingForm.value;

    try {
      /* DELIVERY */
      const deliveryRes: any = await this.http
        .post(`${API_BASE}/customer/add-delivery`, {
          customerId: form.customerId,
          delivery: {
            email: form.deliveryEmail,
            firstName: form.deliveryFirstName,
            lastName: form.deliveryLastName,
            mobile: form.deliveryMobile,
            phone: form.deliveryPhone,
            dob: form.dob,
            street: form.street,
            houseNumber: form.houseNumber,
            zip: form.zip,
            city: form.city,
          },
        })
        .toPromise();

      const deliveryId = deliveryRes?.data?.id;

      /* CONNECTION */
      await this.http
        .post(`${API_BASE}/customer/add-connection`, {
          customerId: form.customerId,
          deliveryId,
          provider: this.selectedRate,
          connectionData: {
            isMovingIn: form.isMovingIn === "yes",
            moveInDate: form.moveInDate,
            submitLater: form.submitLater,
            meterNumber: form.meterNumber,
            marketLocationId: form.marketLocationId,
            currentProvider: form.currentProvider,

            cancellation: {
              autoCancellation: form.autoCancellation,
              alreadyCancelled: form.alreadyCancelled,
              selfCancellation: form.selfCancellation,
            },

            autoCancellation: form.autoCancellation,
            alreadyCancelled: form.alreadyCancelled,
            selfCancellation: form.selfCancellation,

            delivery: form.deliveryOption === "wunschtermin",

            desiredDelivery:
              form.deliveryOption === "wunschtermin"
                ? form.desiredDeliveryDate
                : null,
          },
        })
        .toPromise();

      /* PAYMENT */
      await this.http
        .post(`${API_BASE}/customer/add-payment`, {
          customerId: form.customerId,
          deliveryId,
          paymentData: {
            paymentMethod: form.paymentMethod,

            ...(form.paymentMethod === "lastschrift" && {
              iban: form.iban,
              accountHolder: {
                firstName: form.accountHolderFirstName,
                lastName: form.accountHolderLastName,
              },
              sepaConsent: form.sepaConsent,
            }),
          },
        })
        .toPromise();

      this.successMessage = "Booking created successfully";

      setTimeout(() => {
        this.router.navigate(["/admin/bookings"]);
      }, 1500);
    } catch (error: any) {
      this.errorMessage = error?.error?.message || "Failed to create booking";
    } finally {
      this.isLoading = false;
    }
  }
}
