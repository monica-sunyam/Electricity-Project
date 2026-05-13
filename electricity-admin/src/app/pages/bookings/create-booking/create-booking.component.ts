import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { HttpClient } from "@angular/common/http";
import { AuthService } from "../../../shared/services/auth.service";

const API_BASE = "http://192.168.0.155:8080";

/**
 * Converts a Unix timestamp (seconds) to an HTML date-input string (YYYY-MM-DD).
 * Returns "" if the value is falsy or not a valid number.
 */
function unixToDateInput(unix: number | null | undefined): string {
  if (!unix) return "";
  const d = new Date(unix * 1000);
  if (isNaN(d.getTime())) return "";
  return d.toISOString().substring(0, 10); // YYYY-MM-DD
}

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
  /** True while the prefill fetch is in-flight */
  isLoadingPrefill = false;

  successMessage = "";
  errorMessage = "";
  rateError = "";

  /**
   * deliveryId from the route query-param (?deliveryId=35).
   * When present we fetch the existing delivery and prefill the form.
   */
  prefillDeliveryId: number | null = null;

  /**
   * Whether the form is being opened in "edit / continue" mode
   * (i.e. a deliveryId was passed in the URL).
   */
  get isEditMode(): boolean {
    return this.prefillDeliveryId !== null;
  }

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute,
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

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.initForm();
    this.loadCustomers();
    this.setupDynamicValidation();

    // Read optional deliveryId from query params: /create-booking?deliveryId=35
    const param = this.route.snapshot.queryParamMap.get("deliveryId");
    if (param) {
      this.prefillDeliveryId = Number(param);
    }
  }

  // ── Customer helpers ──────────────────────────────────────────────────────

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

  // ── Form init ─────────────────────────────────────────────────────────────

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
      isMovingIn: ["no"],
      moveInDate: [""],
      submitLater: [false],
      meterNumber: ["", Validators.required],
      marketLocationId: [""],
      currentProvider: ["", Validators.required],
      autoCancellation: [true],
      alreadyCancelled: [false],
      selfCancellation: [false],
      deliveryOption: ["schnellstmoeglich"],
      desiredDeliveryDate: [""],

      /* ── Payment ─────────────────────────────────────────────────── */
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

          // ── Prefill after customers are loaded so we can match the customer ──
          if (this.prefillDeliveryId) {
            this.loadDeliveryAndPrefill(this.prefillDeliveryId);
          }
        },
        error: (err) => {
          console.error("Failed to load customers", err);
          this.errorMessage = err?.error?.message || "Failed to load customers";
        },
      });
  }

  // ── Prefill logic ─────────────────────────────────────────────────────────

  /**
   * Fetches the existing delivery record and populates every form field
   * that has already been filled. Fields from steps the user never reached
   * will remain empty so the admin can complete them.
   */
  loadDeliveryAndPrefill(deliveryId: number): void {
    this.isLoadingPrefill = true;

    const payload = {
      adminId: this.authService.getUserId(),
      deliveryId,
    };

    this.http
      .post<any>(`${API_BASE}/admin/fetch-deliveries`, payload)
      .subscribe({
        next: (res) => {
          this.isLoadingPrefill = false;
          if (!res?.res || !res?.data) {
            this.errorMessage = "Could not load delivery for prefill.";
            return;
          }
          this.applyPrefill(res.data);
        },
        error: (err) => {
          this.isLoadingPrefill = false;
          this.errorMessage =
            err?.error?.message || "Failed to load delivery data for prefill.";
        },
      });
  }

  /**
   * Maps the fetch-deliveries API response onto the form and provider search.
   * Only patches fields that are non-null/non-empty in the API response so
   * intentionally blank values aren't overwritten.
   */
  private applyPrefill(data: any): void {
    // ── 1. Select the customer ───────────────────────────────────────────
    if (data.email) {
      const match = this.customers.find((c) => c.email === data.email);
      if (match) {
        this.selectedCustomerId = match.id;
      }
    }

    // ── 2. Address / search form (used for provider search) ─────────────
    const addr = data.customerAddress;
    if (addr) {
      this.searchForm = {
        ...this.searchForm,
        zip: addr.zip || this.searchForm.zip,
        city: addr.city || this.searchForm.city,
        street: addr.street || this.searchForm.street,
        houseNumber: addr.houseNumber || this.searchForm.houseNumber,
      };
    }
    if (data.consumption) {
      this.searchForm.consum = data.consumption;
    }

    // ── 3. Delivery info form fields ─────────────────────────────────────
    const patch: Record<string, any> = {};

    if (data.email) patch["deliveryEmail"] = data.email;
    if (data.title) patch["deliveryTitle"] = data.title;
    if (data.firstName) patch["deliveryFirstName"] = data.firstName;
    if (data.lastName) patch["deliveryLastName"] = data.lastName;
    if (data.mobile) patch["deliveryMobile"] = data.mobile;
    if (data.telephone) patch["deliveryPhone"] = data.telephone;
    if (data.dob) patch["dob"] = unixToDateInput(data.dob);
    if (data.persons) patch["persons"] = data.persons;
    if (data.consumption) patch["consumption"] = data.consumption;

    // Salutation comes from the nested order.doc.customer object
    const docCustomer = data.order?.doc?.customer;
    if (docCustomer?.salutation) patch["salutation"] = docCustomer.salutation;

    // ── 4. Billing address ───────────────────────────────────────────────
    const billing = data.billingAddress;
    if (billing) {
      patch["hasDifferentBilling"] = !!billing.isDifferent;
      if (billing.isDifferent) {
        if (billing.zip) patch["billingZip"] = billing.zip;
        if (billing.city) patch["billingCity"] = billing.city;
        if (billing.street) patch["billingStreet"] = billing.street;
        if (billing.houseNumber)
          patch["billingHouseNumber"] = billing.houseNumber;
      }
    }

    // ── 5. Connection ─────────────────────────────────────────────────────
    const conn = data.connection;
    if (conn) {
      if (conn.isMovingIn !== undefined && conn.isMovingIn !== null) {
        patch["isMovingIn"] = conn.isMovingIn ? "yes" : "no";
      }
      if (conn.moveInDate)
        patch["moveInDate"] = unixToDateInput(conn.moveInDate);
      if (conn.submitLater !== undefined)
        patch["submitLater"] = conn.submitLater;
      if (conn.meterNumber) patch["meterNumber"] = conn.meterNumber;
      if (conn.marketLocationId)
        patch["marketLocationId"] = conn.marketLocationId;
      if (conn.currentProvider) patch["currentProvider"] = conn.currentProvider;
      if (conn.autoCancellation !== undefined)
        patch["autoCancellation"] = conn.autoCancellation;
      if (conn.alreadyCancelled !== undefined)
        patch["alreadyCancelled"] = conn.alreadyCancelled;
      if (conn.selfCancellation !== undefined)
        patch["selfCancellation"] = conn.selfCancellation;

      // Desired delivery
      if (conn.desiredDelivery) {
        patch["deliveryOption"] = "wunschtermin";
        patch["desiredDeliveryDate"] = unixToDateInput(conn.desiredDelivery);
      } else if (conn.delivery === false) {
        patch["deliveryOption"] = "schnellstmoeglich";
      }
    }

    // ── 6. Payment ────────────────────────────────────────────────────────
    const payment = data.payment;
    if (payment) {
      if (payment.paymentMethod) patch["paymentMethod"] = payment.paymentMethod;
      if (payment.iban) patch["iban"] = payment.iban;
      if (payment.firstName)
        patch["accountHolderFirstName"] = payment.firstName;
      if (payment.lastName) patch["accountHolderLastName"] = payment.lastName;
      if (payment.sepaConsent !== undefined)
        patch["sepaConsent"] = payment.sepaConsent;
    }

    // Apply all patches at once
    this.bookingForm.patchValue(patch);

    // ── 7. Provider (rate) — fetch the provider list then select the saved rate ──
    const savedProvider = data.provider;
    if (savedProvider) {
      // Run the provider search so the rate cards appear, then auto-select
      this.fetchRatesAndSelect(savedProvider);
    }
  }

  /**
   * Fetches rates for the current searchForm state, then auto-selects the
   * rate whose rateId matches `savedProvider.rateId`.
   * If the rateId is not found in fresh results the saved provider object
   * is used as a fallback so the admin still sees what was chosen before.
   */
  private fetchRatesAndSelect(savedProvider: any): void {
    if (
      !this.searchForm.zip ||
      !this.searchForm.city ||
      !this.searchForm.street ||
      !this.searchForm.houseNumber
    ) {
      // Not enough address data — just display the saved provider as selected
      this.selectedRate = savedProvider;
      return;
    }

    this.isLoadingRates = true;
    this.rateError = "";

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

        const fromRates = this.rates
          .map((r: any) => r.providerName)
          .filter(Boolean);
        const fromBase = this.baseProvider?.providerName
          ? [this.baseProvider.providerName]
          : [];
        this.providersList = [...new Set([...fromRates, ...fromBase])];

        // Auto-select the previously chosen rate
        const match = this.rates.find((r) => r.rateId === savedProvider.rateId);
        this.selectedRate = match ?? savedProvider;
      },
      error: (err) => {
        this.isLoadingRates = false;
        this.rateError = err?.error?.message || "Failed to fetch providers";
        // Still show the saved provider so admin can see what was picked
        this.selectedRate = savedProvider;
      },
    });
  }

  // ── Dynamic validation setup ──────────────────────────────────────────────

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

    /* Cancellation sub-options are mutually exclusive ─────────────────── */
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

  // ── Rate search (manual — "Search Providers" button) ─────────────────────

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

    const payload: Record<string, any> = {
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

    // Pass the deliveryId through so the backend can upsert rather than insert
    if (this.prefillDeliveryId) {
      payload["deliveryId"] = this.prefillDeliveryId;
    }

    console.log("Submitting booking with payload:", payload);
    this.http
      .post<any>(`${API_BASE}/admin/add-new-delivery`, payload)
      .subscribe({
        next: (res) => {
          this.isLoading = false;
          if (res?.res === false) {
            this.errorMessage = res?.message || "Failed to create booking";
            return;
          }
          this.successMessage = this.isEditMode
            ? "Booking updated successfully! Redirecting…"
            : "Booking created successfully! Redirecting…";
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
