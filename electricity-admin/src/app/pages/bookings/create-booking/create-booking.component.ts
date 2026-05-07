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
  providersList: string[] = [];
  rates: any[] = [];
  selectedRate: any = null;
  baseProvider: any = null;
  selectedCustomerId: number | null = null;
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

  // ── Computed getters used in the template ────────────────────────────────

  get isMovingIn(): boolean {
    return this.bookingForm?.get("isMovingIn")?.value === "yes";
  }

  get isLastschrift(): boolean {
    return this.bookingForm?.get("paymentMethod")?.value === "lastschrift";
  }

  get isWunschtermin(): boolean {
    return this.bookingForm?.get("deliveryOption")?.value === "wunschtermin";
  }

  get showBillingFields(): boolean {
    return this.bookingForm?.get("hasDifferentBilling")?.value === true;
  }

  // ── Init ─────────────────────────────────────────────────────────────────

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
      consumption: [4350, Validators.required],

      /* ── Delivery ────────────────────────────────────────────────── */
      deliveryEmail: ["", Validators.required],
      deliveryTitle: [""],
      salutation: ["", Validators.required],
      deliveryFirstName: ["", Validators.required],
      deliveryLastName: ["", Validators.required],
      deliveryMobile: ["", Validators.required],
      deliveryPhone: [""],
      dob: ["", Validators.required],
      persons: [2],
      hasDifferentBilling: [false],
      billingZip: [""],
      billingCity: [""],
      billingStreet: [""],
      billingHouseNumber: [""],

      /* ── Connection ──────────────────────────────────────────────── */
      // Moving-in toggle: 'yes' | 'no'
      isMovingIn: ["no"],
      // Only required when isMovingIn === 'yes'
      moveInDate: [""],

      // Always present
      submitLater: [false],
      meterNumber: ["", Validators.required],
      marketLocationId: [""], // optional

      // Only relevant when isMovingIn === 'no'
      currentProvider: ["", Validators.required], // required by default (default is 'no')
      autoCancellation: [true],
      alreadyCancelled: [false],
      selfCancellation: [false],
      // Delivery date toggle (only when isMovingIn === 'no')
      deliveryOption: ["schnellstmoeglich"], // 'schnellstmoeglich' | 'wunschtermin'
      desiredDeliveryDate: [""], // required when deliveryOption === 'wunschtermin'

      /* ── Payment ─────────────────────────────────────────────────── */
      paymentMethod: ["ueberweisung"], // 'ueberweisung' | 'lastschrift'
      // Only required when paymentMethod === 'lastschrift'
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
    /* isMovingIn toggle ───────────────────────────────────────────────── */
    this.bookingForm.get("isMovingIn")?.valueChanges.subscribe((value) => {
      const moveDate = this.bookingForm.get("moveInDate");
      const currentProvider = this.bookingForm.get("currentProvider");
      const desiredDate = this.bookingForm.get("desiredDeliveryDate");

      if (value === "yes") {
        moveDate?.setValidators([Validators.required]);
        currentProvider?.clearValidators();
        desiredDate?.clearValidators();
        desiredDate?.setValue("");
        // reset fields that belong to the 'no' branch
        this.bookingForm.patchValue(
          {
            currentProvider: "",
            alreadyCancelled: false,
            selfCancellation: false,
            deliveryOption: "schnellstmoeglich",
            desiredDeliveryDate: "",
          },
          { emitEvent: false },
        );
      } else {
        moveDate?.clearValidators();
        moveDate?.setValue("");
        currentProvider?.setValidators([Validators.required]);
      }

      moveDate?.updateValueAndValidity();
      currentProvider?.updateValueAndValidity();
    });

    /* Cancellation sub-options are mutually exclusive
       (mirrors selectCancellation() in connection-data.ts) ──────────── */
    this.bookingForm.get("alreadyCancelled")?.valueChanges.subscribe((v) => {
      if (v)
        this.bookingForm.patchValue(
          { selfCancellation: false },
          { emitEvent: false },
        );
    });
    this.bookingForm.get("selfCancellation")?.valueChanges.subscribe((v) => {
      if (v)
        this.bookingForm.patchValue(
          { alreadyCancelled: false },
          { emitEvent: false },
        );
    });

    /* deliveryOption ─────────────────────────────────────────────────── */
    this.bookingForm.get("deliveryOption")?.valueChanges.subscribe((value) => {
      const desiredDate = this.bookingForm.get("desiredDeliveryDate");
      if (value === "wunschtermin") {
        desiredDate?.setValidators([Validators.required]);
      } else {
        desiredDate?.clearValidators();
        desiredDate?.setValue("");
      }
      desiredDate?.updateValueAndValidity();
    });

    /* paymentMethod ──────────────────────────────────────────────────── */
    this.bookingForm.get("paymentMethod")?.valueChanges.subscribe((value) => {
      const iban = this.bookingForm.get("iban");
      const firstName = this.bookingForm.get("accountHolderFirstName");
      const lastName = this.bookingForm.get("accountHolderLastName");
      const sepa = this.bookingForm.get("sepaConsent");

      if (value === "lastschrift") {
        iban?.setValidators([Validators.required]);
        firstName?.setValidators([Validators.required]);
        lastName?.setValidators([Validators.required]);
      } else {
        iban?.clearValidators();
        firstName?.clearValidators();
        lastName?.clearValidators();
        // clear values when switching away from Lastschrift
        iban?.setValue("");
        firstName?.setValue("");
        lastName?.setValue("");
        sepa?.setValue(false);
      }

      iban?.updateValueAndValidity();
      firstName?.updateValueAndValidity();
      lastName?.updateValueAndValidity();
    });
  }

  // ── Rate search ───────────────────────────────────────────────────────────

  fetchRates(): void {
    this.rateError = "";
    this.rates = [];
    this.selectedRate = null;

    if (!this.selectedCustomerId) {
      this.rateError = "Please select a customer";
      return;
    }

    if (
      !this.searchForm.zip ||
      !this.searchForm.city ||
      !this.searchForm.street ||
      !this.searchForm.houseNumber
    ) {
      this.rateError = "Please complete address fields";
      return;
    }

    this.isLoadingRates = true;

    const payload = {
      ...this.searchForm,
      customerId: this.selectedCustomerId,
      adminId: this.authService.getUserId(),
    };

    this.http.post<any>(`${API_BASE}/api/get-rates`, payload).subscribe({
      next: (res) => {
        this.isLoadingRates = false;
        this.rates = res?.rates?.result ?? [];
        this.baseProvider = res?.baseProvider?.result?.[0] ?? null;

        // Build unique provider names for the 'Current Provider' dropdown
        // from fetched rate cards + base provider
        const fromRates = this.rates
          .map((r: any) => r.providerName)
          .filter(Boolean);
        const fromBase = this.baseProvider?.providerName
          ? [this.baseProvider.providerName]
          : [];
        this.providersList = [...new Set([...fromRates, ...fromBase])];
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

  // ── Submit ────────────────────────────────────────────────────────────────

  createBooking(): void {
    this.errorMessage = "";
    this.successMessage = "";

    if (!this.selectedRate) {
      this.errorMessage = "Please select a provider";
      return;
    }

    if (this.bookingForm.invalid) {
      this.bookingForm.markAllAsTouched();
      this.errorMessage = "Please fill in all required fields";
      return;
    }

    this.isLoading = true;
    const form = this.bookingForm.value;
    const movingIn = form.isMovingIn === "yes";

    const payload = {
      customerId: this.selectedCustomerId,
      adminId: this.authService.getUserId(),
      provider: this.selectedRate,

      // ── Delivery ──────────────────────────────────────────────────
      delivery: {
        email: form.deliveryEmail,
        title: form.deliveryTitle,
        firstName: form.deliveryFirstName,
        lastName: form.deliveryLastName,
        salutation: form.salutation,
        mobile: form.deliveryMobile,
        telephone: form.deliveryPhone,
        dob: this.formatDate(form.dob),
        zip: this.searchForm.zip,
        city: this.searchForm.city,
        street: this.searchForm.street,
        houseNumber: this.searchForm.houseNumber,
        deliveryType: "electricity",
        persons: form.persons,
        consumption: form.consumption,
      },
      billingAddress: {
        different: form.hasDifferentBilling,
        ...(form.hasDifferentBilling && {
          zip: form.billingZip,
          city: form.billingCity,
          street: form.billingStreet,
          houseNumber: form.billingHouseNumber,
        }),
      },

      // ── Connection ────────────────────────────────────────────────
      connection: {
        isMovingIn: movingIn,
        ...(movingIn && {
          moveInDate: this.formatDate(form.moveInDate),
        }),
        submitLater: form.submitLater,
        meterNumber: form.meterNumber,
        marketLocationId: form.marketLocationId,
        ...(!movingIn && {
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
              ? this.formatDate(form.desiredDeliveryDate)
              : null,
        }),
      },

      // ── Payment ───────────────────────────────────────────────────
      paymentDetails: {
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
      },
    };
    console.log("Submitting booking with payload:", payload);
    this.http.post<any>(`${API_BASE}/admin/add-new-delivery`, payload).subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res?.res === false) {
          this.errorMessage = res?.message || "Failed to create booking";
          return;
        }
        this.successMessage = "Booking created successfully";
        setTimeout(() => this.router.navigate(["/admin/bookings"]), 1500);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err?.error?.message || "Failed to create booking";
      },
    });
  }

  formatDate(date: string): string | null {
    if (!date) return null;

    const d = new Date(date);

    if (isNaN(d.getTime())) return null;

    const day = String(d.getDate()).padStart(2, "0");
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const year = d.getFullYear();

    return `${day}.${month}.${year}`;
  }
}
