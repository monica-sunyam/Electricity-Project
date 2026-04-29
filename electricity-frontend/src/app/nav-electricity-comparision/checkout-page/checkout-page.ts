import { ChangeDetectorRef, Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';
import { ContactPerson } from '../../layout/contact-person/contact-person';
import { NeedSupport } from '../../layout/need-support/need-support';
import { AuthService } from '../../services/auth.service';
import { Environment, ENVIRONMENT } from '../../environment.token';

interface CustomerAddress {
  id?: number;
  zip?: string | null;
  city?: string | null;
  street?: string | null;
  houseNumber?: string | null;
  isDifferent?: boolean | null;
  createdOn?: number | null;
}

interface CustomerConnection {
  id?: number;
  isMovingIn?: boolean | null;
  moveInDate?: number | null;
  submitLater?: boolean | null;
  meterNumber?: string | null;
  currentProvider?: string | null;
  autoCancellation?: boolean | null;
  alreadyCancelled?: boolean | null;
  selfCancellation?: boolean | null;
  delivery?: boolean | string | number | null;
  desiredDelivery?: boolean | string | number | null;
  marketLocationId?: string | null;
  createdOn?: number | null;
}

interface CustomerPayment {
  id?: number;
  paymentMethod?: string | null;
  iban?: string | null;
  accountHolderFirstName?: string | null;
  accountHolderLastName?: string | null;
  accountHolder?: {
    firstName?: string | null;
    lastName?: string | null;
  } | null;
  sepaConsent?: boolean | null;
  createdOn?: number | null;
}

interface CustomerFormData {
  id?: number;
  title?: string | null;
  email?: string | null;
  firstName?: string | null;
  lastName?: string | null;
  mobile?: string | null;
  telephone?: string | null;
  deliveryDate?: number | null;
  orderPlacedOn?: number | null;
  orderPlaced?: boolean | null;
  billingAddress?: CustomerAddress | null;
  address?: CustomerAddress | null;
  customerConnection?: CustomerConnection | null;
  customerPayment?: CustomerPayment | null;
  customerSchedule?: unknown;
}

interface FetchFormResponse {
  data?: CustomerFormData | null;
  message?: string;
  res?: boolean;
}

@Component({
  selector: 'app-checkout-page',
  imports: [ContactPerson, NeedSupport, RouterModule, CommonModule, FormsModule],
  templateUrl: './checkout-page.html',
  styleUrl: './checkout-page.css',
})
export class CheckoutPage implements OnInit {
  private API_BASE: string;
  private readonly LOCAL_API_BASE = 'http://192.168.0.155:8080';
  private redirectTimeoutId: ReturnType<typeof setTimeout> | null = null;
  showConfirmation = false;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  formData: CustomerFormData | null = null;
  maxAccessibleStep = 1;
  private readonly currentStep = 5;
  private isJourneyCompleted = false;

  // Schedule / callback time-slot state
  selectedDay: string = '';
  selectedTimeSlot: string = '';
  scheduleDescription: string = '';
  isScheduleLoading = false;
  scheduleErrorMessage = '';
  scheduleSuccessMessage = '';

  readonly daysOfWeek = [
    { label: 'Montag', value: 'MONDAY' },
    { label: 'Dienstag', value: 'TUESDAY' },
    { label: 'Mittwoch', value: 'WEDNESDAY' },
    { label: 'Donnerstag', value: 'THURSDAY' },
    { label: 'Freitag', value: 'FRIDAY' },
    { label: 'Samstag', value: 'SATURDAY' },
  ];

  readonly timeSlots = [
    { label: 'Vormittags von 08:00 - 11:00 Uhr', value: 'MORNING_08_11' },
    { label: 'Mittags von 11:00 - 14:00 Uhr', value: 'MIDDAY_11_14' },
    { label: 'Nachmittags von 14:00 - 17:00 Uhr', value: 'AFTERNOON_14_17' },
    { label: 'Abends von 17:00 - 20:00 Uhr', value: 'EVENING_17_20' },
  ];

  /** Maps day enum values to JS Date.getDay() numbers (0=Sun … 6=Sat). */
  private readonly dayValueToJsDay: Record<string, number> = {
    MONDAY: 1,
    TUESDAY: 2,
    WEDNESDAY: 3,
    THURSDAY: 4,
    FRIDAY: 5,
    SATURDAY: 6,
  };

  /** Start hour (24-h) for each time-slot. */
  private readonly slotStartHour: Record<string, number> = {
    MORNING_08_11: 8,
    MIDDAY_11_14: 11,
    AFTERNOON_14_17: 14,
    EVENING_17_20: 17,
  };

  private readonly mainStepRoutes: Record<number, string> = {
    1: '/electricity-comparision/register',
    2: '/electricity-comparision/delivery-address',
    3: '/electricity-comparision/connection-data',
    4: '/electricity-comparision/payment-method',
    5: '/electricity-comparision/checkout',
  };

  constructor(
    @Inject(ENVIRONMENT) private env: Environment,
    private router: Router,
    private http: HttpClient,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
  ) {
    this.API_BASE = env.apiBaseUrl;
  }

  ngOnInit(): void {
    this.fetchFormData();
  }

  openPage(): void {
    console.log('Clicking');
    this.successMessage = '';
    this.errorMessage = '';

    const userId = this.authService.getUserId();
    const deliveryId = this.authService.getDeliveryId();

    console.log('[DEBUG] openPage: userId=', userId, 'deliveryId=', deliveryId);

    if (!userId || !deliveryId) {
      console.warn('[DEBUG] openPage: missing userId or deliveryId – showing error');
      this.isLoading = false; // ← guard never reset this; button stayed disabled forever
      this.errorMessage = 'Bitte füllen Sie alle vorherigen Schritte vollständig aus.';
      this.cdr.detectChanges();
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }

    this.errorMessage = '';
    this.isLoading = true;

    const payload = {
      customerId: parseInt(userId ?? '0', 10),
      deliveryId: parseInt(deliveryId ?? '0', 10),
    };

    console.log('Payload being sent to API:', JSON.stringify(payload, null, 2));

    const submit = (apiBase: string) =>
      this.http.post(`${apiBase}/customer/submit-declaration`, payload);

    submit(this.API_BASE).subscribe({
      next: () => {
        this.isLoading = false;
        this.handleDeclarationSuccess();
        this.showConfirmation = true;
        this.scheduleErrorMessage = '';
        this.scheduleSuccessMessage = '';
        this.successMessage = '';
        this.cdr.detectChanges();
        window.scrollTo({ top: 0, behavior: 'smooth' });
      },
      error: (err) => {
        submit(this.LOCAL_API_BASE).subscribe({
          next: () => {
            this.isLoading = false;
            this.handleDeclarationSuccess();
            this.showConfirmation = true;
            this.scheduleErrorMessage = '';
            this.scheduleSuccessMessage = '';
            this.successMessage = '';
            this.cdr.detectChanges();
            window.scrollTo({ top: 0, behavior: 'smooth' });
          },
          error: (err2) => {
            console.error(
              '[DEBUG] Both submit APIs failed. Primary err:',
              err,
              'Fallback err:',
              err2,
            );
            this.isLoading = false;
            this.errorMessage =
              err2?.error?.message ||
              err?.error?.message ||
              'Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.';
            this.cdr.detectChanges(); // ← was missing: error message and re-enabled button never rendered
            window.scrollTo({ top: 0, behavior: 'smooth' });
          },
        });
      },
    });
  }

  /**
   * Returns the set of day values that are selectable.
   * Only the 3 days starting from today (Mon–Sat) are enabled.
   * If today is Sunday it is treated as Monday.
   */
  get enabledDays(): Set<string> {
    const now = new Date();
    let todayJs = now.getDay(); // 0=Sun, 1=Mon … 6=Sat
    if (todayJs === 0) todayJs = 1; // Sunday → roll to Monday

    const enabled = new Set<string>();
    let count = 0;
    let current = todayJs;

    while (count < 3 && current <= 6) {
      const entry = Object.entries(this.dayValueToJsDay).find(([, v]) => v === current);
      if (entry) {
        enabled.add(entry[0]);
        count++;
      }
      current++;
    }

    return enabled;
  }

  isDayEnabled(dayValue: string): boolean {
    return this.enabledDays.has(dayValue);
  }

  /**
   * A time slot is enabled when:
   *  - No day is selected yet (preview state — all shown as selectable), OR
   *  - The selected day is in the future → all slots open, OR
   *  - The selected day is today → the slot's start hour must be > now + 2 h (2-hour buffer).
   */
  isTimeSlotEnabled(slotValue: string): boolean {
    if (!this.selectedDay) return true;

    const now = new Date();
    let todayJs = now.getDay();
    if (todayJs === 0) todayJs = 1;

    const selectedJsDay = this.dayValueToJsDay[this.selectedDay];
    if (selectedJsDay === todayJs) {
      const currentDecimalHour = now.getHours() + now.getMinutes() / 60;
      const slotStart = this.slotStartHour[slotValue] ?? 0;
      return slotStart > currentDecimalHour + 2;
    }

    return true; // future day → all slots available
  }

  selectDay(day: string): void {
    if (!this.isDayEnabled(day)) return;
    this.selectedDay = day;
    // Clear time slot if it is no longer valid for the newly selected day
    if (this.selectedTimeSlot && !this.isTimeSlotEnabled(this.selectedTimeSlot)) {
      this.selectedTimeSlot = '';
    }
    this.cdr.detectChanges();
  }

  selectTimeSlot(slot: string): void {
    if (!this.isTimeSlotEnabled(slot)) return;
    this.selectedTimeSlot = slot;
    this.cdr.detectChanges();
  }

  submitSchedule(): void {
    console.log('select day:', this.selectedDay);
    console.log('select time slot:', this.selectedTimeSlot);
    if (!this.selectedDay || !this.selectedTimeSlot) {
      this.scheduleErrorMessage = 'Bitte wählen Sie einen Tag und eine Uhrzeit aus.';
      console.log('error', this.scheduleErrorMessage);
      return;
    }

    const userId = this.authService.getUserId();
    const deliveryId = this.authService.getDeliveryId();

    if (!userId || !deliveryId) {
      this.scheduleErrorMessage = 'Benutzerdaten nicht gefunden. Bitte laden Sie die Seite neu.';
      return;
    }

    this.scheduleErrorMessage = '';
    this.scheduleSuccessMessage = '';
    this.successMessage = '';
    this.isScheduleLoading = true;

    const payload = {
      customerId: parseInt(userId, 10),
      deliveryId: parseInt(deliveryId, 10),
      dayOfWeek: this.selectedDay,
      timeSlot: this.selectedTimeSlot,
      description: this.scheduleDescription ?? '',
    };

    console.log('Schedule payload:', JSON.stringify(payload, null, 2));

    const submit = (apiBase: string) => this.http.post(`${apiBase}/customer/add-schedule`, payload);

    submit(this.API_BASE).subscribe({
      next: () => {
        this.isScheduleLoading = false;
        this.scheduleSuccessMessage =
          'Ihre Rückrufzeit wurde erfolgreich übermittelt. Vielen Dank!';
        this.startSuccessRedirect('Ihre Anfrage wurde erfolgreich übermittelt.');
        this.authService.clearCheckoutFlowData();
        this.cdr.detectChanges();
      },
      error: (err) => {
        submit(this.LOCAL_API_BASE).subscribe({
          next: () => {
            this.isScheduleLoading = false;
            this.scheduleSuccessMessage =
              'Ihre Rückrufzeit wurde erfolgreich übermittelt. Vielen Dank!';
            this.startSuccessRedirect('Ihre Anfrage wurde erfolgreich übermittelt.');
            this.cdr.detectChanges();
          },
          error: (err2) => {
            this.isScheduleLoading = false;
            this.scheduleErrorMessage =
              err2?.error?.message ||
              err?.error?.message ||
              'Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.';
            console.error('Add-schedule API error:', err, err2);
            this.cdr.detectChanges();
          },
        });
        this.cdr.detectChanges();
      },
    });
  }

  skipSchedule(): void {
    this.scheduleErrorMessage = '';
    this.scheduleSuccessMessage = '';
    this.authService.clearCheckoutFlowData();
    this.startSuccessRedirect('Ihre Anfrage wurde erfolgreich übermittelt.');
    this.cdr.detectChanges();
  }

  navigateToMainStep(step: number): void {
    if (this.isJourneyCompleted) {
      return;
    }

    if (step > this.currentStep) {
      return;
    }

    if (step > this.maxAccessibleStep) {
      return;
    }

    const route = this.mainStepRoutes[step];
    if (route) {
      this.router.navigate([route]);
    }
  }

  get selectedProvider(): any {
    return this.authService.getSelectedProvider();
  }

  get selectedAddress(): any {
    return this.authService.getAddressData();
  }

  get email(): string {
    const d: any = this.formData;
    return this.valueOrFallback(
      d?.email ??
      d?.deliveryAddress?.email ??
      d?.address?.email ??
      this.authService.getCurrentUser()?.email,
    );
  }

  get deliveryAddress(): CustomerAddress | null | undefined {
    const d: any = this.formData;
    const addr = d?.address ?? d?.deliveryAddress ?? d?.customerAddress ?? null;
    if (addr) {
      return addr;
    }

    const stored = this.authService.getAddressData();
    if (stored?.zip && stored?.city && stored?.street && stored?.houseNumber) {
      return {
        zip: stored.zip,
        city: stored.city,
        street: stored.street,
        houseNumber: stored.houseNumber,
      } as CustomerAddress;
    }

    return null;
  }

  get deliveryMobile(): string {
    const d: any = this.formData;
    return this.valueOrFallback(
      d?.mobile ?? d?.deliveryAddress?.mobile ?? d?.address?.mobile ?? null,
    );
  }

  get deliveryTelephone(): string {
    const d: any = this.formData;
    return this.valueOrFallback(
      d?.telephone ?? d?.deliveryAddress?.telephone ?? d?.address?.telephone ?? null,
    );
  }

  get deliveryDateLabel(): string {
    const d: any = this.formData;
    return this.formatTimestamp(d?.dob ?? d?.deliveryAddress?.dob ?? d?.address?.dob ?? null);
  }

  get billingAddress(): CustomerAddress | null | undefined {
    return this.formData?.billingAddress;
  }

  get billingDisplayAddress(): CustomerAddress | null | undefined {
    return this.billingAddress || this.deliveryAddress;
  }

  get connection(): CustomerConnection | null | undefined {
    const d: any = this.formData;
    return d?.customerConnection ?? d?.connectionData ?? d ?? null;
  }

  get payment(): CustomerPayment | null | undefined {
    const d: any = this.formData;
    return d?.customerPayment ?? d?.paymentData ?? d ?? null;
  }

  get fullName(): string {
    const d: any = this.formData;
    const title = d?.title ?? d?.deliveryAddress?.title ?? d?.address?.title;
    const firstName = d?.firstName ?? d?.deliveryAddress?.firstName ?? d?.address?.firstName;
    const lastName = d?.lastName ?? d?.deliveryAddress?.lastName ?? d?.address?.lastName;
    const parts = [title, firstName, lastName].filter(Boolean);
    if (parts.length) {
      return this.valueOrFallback(parts.join(' '));
    }

    return this.valueOrFallback(this.authService.getCurrentUser()?.full_name ?? null);
  }

  get salutation(): string {
    const d: any = this.formData;
    return d?.salutation ?? d?.deliveryAddress?.salutation ?? d?.address?.salutation ?? '';
  }

  get billingAddressTitle(): string {
    return this.billingAddress?.isDifferent ? 'Abweichende Rechnungsadresse' : 'Rechnungsadresse';
  }

  get billingAddressHint(): string {
    return this.billingAddress?.isDifferent
      ? 'Ja, abweichende Rechnungsadresse'
      : 'Nein, identisch mit Lieferadresse';
  }

  get moveInLabel(): string {
    if (this.connection?.isMovingIn === null || this.connection?.isMovingIn === undefined) {
      return 'Keine Angabe';
    }

    return this.connection.isMovingIn ? 'Ja' : 'Nein';
  }

  get meterNumberLabel(): string {
    // if (this.connection?.submitLater) {
    //   return 'Wird nachgereicht';
    // }
    console.log('meterNumberLabel: connection?.meterNumber=', this.connection?.meterNumber);
    return this.valueOrFallback(this.connection?.meterNumber);
  }

  get marketLocationLabel(): string {
    return this.valueOrFallback(this.connection?.marketLocationId, 'Keine Angabe');
  }

  get currentProviderLabel(): string {
    return this.valueOrFallback(this.connection?.currentProvider, 'Wird nachgereicht');
  }

  get cancellationLabel(): string {
    if (this.connection?.autoCancellation) {
      return 'Automatische Kündigung durch neuen Anbieter';
    }

    if (this.connection?.alreadyCancelled) {
      return 'Vertrag wurde bereits gekündigt';
    }

    if (this.connection?.selfCancellation) {
      return 'Ich werde selbst kündigen';
    }

    if (this.connection?.isMovingIn) {
      return 'Keine Kündigung bei Neueinzug';
    }

    return 'Keine Angabe';
  }

  get desiredDeliveryLabel(): string {
    if (this.connection?.isMovingIn) {
      return this.formatTimestamp(this.connection?.moveInDate || this.formData?.deliveryDate);
    }

    if (this.connection?.desiredDelivery) {
      return this.formatFlexibleDate(this.connection.desiredDelivery);
    }

    if (this.connection?.delivery) {
      return this.formatFlexibleDate(this.connection.delivery);
    }

    return this.formData?.deliveryDate
      ? this.formatTimestamp(this.formData.deliveryDate)
      : 'Schnellstmöglich';
  }

  get paymentMethodLabel(): string {
    const method = this.payment?.paymentMethod;

    if (method === 'lastschrift') {
      return 'Lastschrift';
    }

    if (method === 'ueberweisung' || method === 'überweisung') {
      return 'Überweisung';
    }

    return this.valueOrFallback(method);
  }

  get accountHolderLabel(): string {
    const parts = [
      this.payment?.accountHolderFirstName ?? this.payment?.accountHolder?.firstName,
      this.payment?.accountHolderLastName ?? this.payment?.accountHolder?.lastName,
    ].filter(Boolean);
    return this.valueOrFallback(parts.join(' '));
  }

  formatTimestamp(value?: number | string | null): string {
    if (value === null || value === undefined || value === '') {
      return 'Keine Angabe';
    }

    const numericValue = typeof value === 'number' ? value : Number(value);

    if (!Number.isFinite(numericValue)) {
      return this.valueOrFallback(String(value));
    }

    const milliseconds = numericValue < 1000000000000 ? numericValue * 1000 : numericValue;

    return new Intl.DateTimeFormat('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    }).format(new Date(milliseconds));
  }

  valueOrFallback(value?: string | number | boolean | null, fallback = 'Keine Angabe'): string {
    if (value === null || value === undefined || value === '') {
      return fallback;
    }

    if (typeof value === 'boolean') {
      return value ? 'Ja' : 'Nein';
    }

    return String(value);
  }

  private fetchFormData(): void {
    const userId = this.authService.getUserId();
    const deliveryId = this.authService.getDeliveryId();

    // this.isLoading = true;
    this.errorMessage = '';

    this.fetchFormDataWithPost(userId, deliveryId);
    this.cdr.detectChanges();
  }

  private fetchFormDataWithPost(userId: string | null, deliveryId: string | null): void {
    const payload = {
      customerId: parseInt(userId ?? '0', 10),
      deliveryId: parseInt(deliveryId ?? '0', 10),
      step: 0,
    };

    console.log('Fetching form data with payload:', JSON.stringify(payload, null, 2));
    const fetch = (apiBase: string) =>
      this.http.post<FetchFormResponse>(`${apiBase}/customer/fetch-form`, payload);

    fetch(this.API_BASE).subscribe({
      next: (res) => this.handleFetchSuccess(res),
      error: (err) => {
        fetch(this.LOCAL_API_BASE).subscribe({
          next: (res) => this.handleFetchSuccess(res),
          error: (err2) => this.handleFetchError(err2 ?? err),
        });
      },
    });
    this.cdr.detectChanges();
  }

  private handleFetchSuccess(res: FetchFormResponse): void {
    console.log('[DEBUG] handleFetchSuccess called, res.res=', res?.res, 'data=', res?.data);
    this.isLoading = false;

    if (res?.res === false) {
      this.errorMessage = res?.message || 'Die gespeicherten Daten konnten nicht geladen werden.';
      console.warn('[DEBUG] fetch-form returned res=false, errorMessage set:', this.errorMessage);
      this.cdr.detectChanges(); // ← was missing: UI never saw isLoading=false
      return;
    }

    this.formData = res?.data ?? null;
    this.isJourneyCompleted = !!this.formData?.orderPlaced;
    this.maxAccessibleStep = this.getMaxAccessibleStep(this.formData);
    console.log('[DEBUG] formData set, maxAccessibleStep=', this.maxAccessibleStep);
    this.cdr.detectChanges(); // ← ensure button re-enables even with OnPush
  }

  private handleFetchError(err: any): void {
    console.error('[DEBUG] handleFetchError called:', err);
    this.isLoading = false;
    this.errorMessage =
      err?.error?.message || 'Die gespeicherten Daten konnten nicht geladen werden.';
    this.cdr.detectChanges();
  }

  private formatFlexibleDate(value: boolean | string | number): string {
    if (value === true) {
      return 'Schnellstmöglich';
    }

    if (value === false) {
      return 'Keine Angabe';
    }

    if (typeof value === 'string' && value.toLowerCase().includes('schnell')) {
      return 'Schnellstmöglich';
    }

    return this.formatTimestamp(value);
  }

  // FIX: timeout changed from 2500 → 5000 ms so the success message is readable
  private startSuccessRedirect(message: string): void {
    this.successMessage = `${message} Sie werden zur Startseite weitergeleitet...`;
    if (this.redirectTimeoutId) {
      clearTimeout(this.redirectTimeoutId);
    }

    this.redirectTimeoutId = setTimeout(() => {
      this.authService.clearAddress();
      this.router.navigate(['/home']);
    }, 5000);
  }

  private handleDeclarationSuccess(): void {
    this.formData = {
      ...(this.formData ?? {}),
      orderPlaced: true,
    };
    this.isJourneyCompleted = true;
    this.maxAccessibleStep = 0;
    // this.authService.clearCheckoutFlowData();
  }

  private getMaxAccessibleStep(data: CustomerFormData | null): number {
    if (!data) {
      return 1;
    }

    const deliveryAddress = this.extractDeliveryAddress(data);
    const connection = this.extractConnection(data);
    const payment = this.extractPayment(data);

    let maxStep = 1;
    if (this.isAccountComplete(data)) maxStep = 2;
    if (this.isDeliveryComplete(data, deliveryAddress)) maxStep = 3;
    if (this.isConnectionComplete(connection)) maxStep = 4;
    if (this.isPaymentComplete(payment)) maxStep = 5;

    return maxStep;
  }

  private extractDeliveryAddress(data: any): CustomerAddress | null {
    return data?.address ?? data?.deliveryAddress ?? data?.customerAddress ?? null;
  }

  private extractConnection(data: any): CustomerConnection | null {
    return data?.customerConnection ?? data?.connectionData ?? data ?? null;
  }

  private extractPayment(data: any): CustomerPayment | null {
    return data?.customerPayment ?? data?.paymentData ?? data ?? null;
  }

  private isAccountComplete(data: CustomerFormData): boolean {
    const d: any = data;
    const email = d?.email ?? d?.deliveryAddress?.email ?? d?.address?.email;
    const firstName = d?.firstName ?? d?.deliveryAddress?.firstName ?? d?.address?.firstName;
    const lastName = d?.lastName ?? d?.deliveryAddress?.lastName ?? d?.address?.lastName;
    return !!email && !!firstName && !!lastName;
  }

  private isDeliveryComplete(data: CustomerFormData, address: CustomerAddress | null): boolean {
    const d: any = data;
    const mobile = d?.mobile ?? d?.deliveryAddress?.mobile ?? d?.address?.mobile;
    const deliveryDate =
      d?.deliveryDate ?? d?.deliveryAddress?.deliveryDate ?? d?.address?.deliveryDate;
    return !!(
      address?.zip &&
      address?.city &&
      address?.street &&
      address?.houseNumber &&
      mobile &&
      deliveryDate
    );
  }

  private isConnectionComplete(connection?: CustomerConnection | null): boolean {
    if (!connection) {
      return false;
    }

    const hasMeter = !!connection.submitLater || !!connection.meterNumber;
    if (!hasMeter) {
      return false;
    }

    if (connection.isMovingIn) {
      return !!connection.moveInDate;
    }

    const hasProvider = !!connection.currentProvider;
    const hasDeliveryChoice =
      (connection.delivery !== null && connection.delivery !== undefined) ||
      (connection.desiredDelivery !== null && connection.desiredDelivery !== undefined);

    return hasProvider && hasDeliveryChoice;
  }

  private isPaymentComplete(payment?: CustomerPayment | null): boolean {
    if (!payment?.paymentMethod) {
      return false;
    }

    if (payment.paymentMethod.toLowerCase().includes('lastschrift')) {
      return !!(
        payment.iban &&
        (payment.accountHolderFirstName ?? payment.accountHolder?.firstName) &&
        (payment.accountHolderLastName ?? payment.accountHolder?.lastName) &&
        payment.sepaConsent
      );
    }

    return true;
  }
}