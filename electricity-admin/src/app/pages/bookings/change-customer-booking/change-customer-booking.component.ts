import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ApiService } from '../../../shared/services/api.service';
import { AuthService } from '../../../shared/services/auth.service';
import { ApiBooking } from '../booking-list/booking-list.component';

type EditableBooking = {
  // Step 2 — Lieferadresse
  email: string;
  salutation: string;
  title: string;
  firstName: string;
  lastName: string;
  mobile: string;
  telephone: string;
  dob: string; // ISO date string for input[type=date]
  hasDifferentBilling: boolean;
  billingZip: string;
  billingCity: string;
  billingStreet: string;
  billingHouseNumber: string;

  // Step 3 — Anschlussdaten
  isMovingIn: boolean;
  moveInDate: string;
  submitLater: boolean;
  meterNumber: string;
  marketLocationId: string;
  currentProvider: string;
  autoCancellation: boolean;
  alreadyCancelled: boolean;
  selfCancellation: boolean;
  deliveryOption: 'schnellstmoeglich' | 'wunschtermin';
  desiredDeliveryDate: string;

  // Step 4 — Zahlung
  paymentMethod: string;
  iban: string;
  paymentFirstName: string;
  paymentLastName: string;
  sepaConsent: boolean;

  // Step 5 — Kontakttermin
  dayOfWeek: string;
  timeSlot: string;
  scheduleDescription: string;
};

@Component({
  selector: 'app-change-customer-booking',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './change-customer-booking.component.html',
  styleUrl: './change-customer-booking.component.css',
})
export class BookingEditComponent implements OnInit {
  originalBooking: ApiBooking | null = null;
  form: EditableBooking = this.emptyForm();
  deliveryId: number | null = null;

  isLoading = false;
  isSaving = false;
  errorMessage = '';
  successMessage = '';
  validationErrors: Record<string, string> = {};

  readonly dayOptions = [
    { value: 'MONDAY', label: 'Montag' },
    { value: 'TUESDAY', label: 'Dienstag' },
    { value: 'WEDNESDAY', label: 'Mittwoch' },
    { value: 'THURSDAY', label: 'Donnerstag' },
    { value: 'FRIDAY', label: 'Freitag' },
    { value: 'SATURDAY', label: 'Samstag' },
    { value: 'SUNDAY', label: 'Sonntag' },
  ];

  readonly timeSlotOptions = [
    { value: 'MORNING_08_11', label: '08–11 Uhr' },
    { value: 'AFTERNOON_11_14', label: '11–14 Uhr' },
    { value: 'AFTERNOON_14_17', label: '14–17 Uhr' },
    { value: 'EVENING_17_20', label: '17–20 Uhr' },
  ];

  constructor(
    private api: ApiService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
  ) { }

  ngOnInit(): void {
    // Try router navigation state first (passed from list page via editBooking())
    const state = this.router.getCurrentNavigation?.()?.extras?.state as
      | { booking?: ApiBooking }
      | undefined;

    if (state?.booking) {
      this.originalBooking = state.booking;
      this.deliveryId = state.booking.deliveryId ?? null;
      this.populateForm(state.booking);
    } else {
      // Fallback: re-fetch by ID from route param
      this.route.params.subscribe((params) => {
        this.deliveryId = Number(params['id']) || null;
        if (this.deliveryId) this.fetchBooking(this.deliveryId);
      });
    }
  }

  private fetchBooking(id: number): void {
    this.isLoading = true;
    this.api
      .post('admin/fetch-deliveries', {
        adminId: this.authService.getUserId(),
        page: 1,
      })
      .subscribe({
        next: (res: any) => {
          this.isLoading = false;
          const list: ApiBooking[] = Array.isArray(res?.data) ? res.data : [];
          const found = list.find((b) => b.deliveryId === id);
          if (found) {
            this.originalBooking = found;
            this.populateForm(found);
          } else {
            this.errorMessage = 'Buchung nicht gefunden.';
          }
        },
        error: () => {
          this.isLoading = false;
          this.errorMessage = 'Fehler beim Laden der Buchung.';
        },
      });
  }

  private populateForm(b: ApiBooking): void {
    const billing = b.billingAddress;
    const conn = b.connection;
    const pay = b.payment;
    const sched = b.contactSchedule;

    // this.form = {
    //   // Step 2
    //   email: b.email ?? '',
    //   salutation: (b as any).salutation ?? '',
    //   title: b.title ?? '',
    //   firstName: b.firstName ?? '',
    //   lastName: b.lastName ?? '',
    //   mobile: b.mobile ?? '',
    //   telephone: b.telephone ?? '',
    //   dob: this.tsToDateInput((b as any).dob ?? (b as any).deliveryDate ?? null),
    //   hasDifferentBilling: billing?.isDifferent ?? false,
    //   billingZip: billing?.zip ?? '',
    //   billingCity: billing?.city ?? '',
    //   billingStreet: billing?.street ?? '',
    //   billingHouseNumber: billing?.houseNumber ?? '',

    //   // Step 3
    //   isMovingIn: conn?.isMovingIn ?? false,
    //   moveInDate: this.tsToDateInput(conn?.moveInDate ?? null),
    //   submitLater: conn?.submitLater ?? false,
    //   meterNumber: conn?.meterNumber ?? '',
    //   marketLocationId: conn?.marketLocationId ?? '',
    //   currentProvider: conn?.currentProvider ?? '',
    //   autoCancellation: conn?.autoCancellation ?? false,
    //   alreadyCancelled: conn?.alreadyCancelled ?? false,
    //   selfCancellation: conn?.selfCancellation ?? false,
    //   // deliveryOption: conn?.desiredDelivery ? 'wunschtermin' : 'schnellstmoeglich',
    //   // desiredDeliveryDate: this.tsToDateInput(
    //   //   typeof conn?.desiredDelivery === 'number' ? conn.desiredDelivery : null,
    //   // ),

    //   // Step 4
    //   paymentMethod: pay?.paymentMethod ?? 'ueberweisung',
    //   iban: pay?.iban ?? '',
    //   paymentFirstName: pay?.firstName ?? '',
    //   paymentLastName: pay?.lastName ?? '',
    //   sepaConsent: pay?.sepaConsent ?? false,
    // };
  }

  private emptyForm(): EditableBooking {
    return {
      email: '', salutation: '', title: '', firstName: '', lastName: '',
      mobile: '', telephone: '', dob: '', hasDifferentBilling: false,
      billingZip: '', billingCity: '', billingStreet: '', billingHouseNumber: '',
      isMovingIn: false, moveInDate: '', submitLater: false,
      meterNumber: '', marketLocationId: '', currentProvider: '',
      autoCancellation: false, alreadyCancelled: false, selfCancellation: false,
      deliveryOption: 'schnellstmoeglich', desiredDeliveryDate: '',
      paymentMethod: 'ueberweisung', iban: '', paymentFirstName: '', paymentLastName: '',
      sepaConsent: false, dayOfWeek: '', timeSlot: '', scheduleDescription: '',
    };
  }

  private tsToDateInput(ts: number | string | null | undefined): string {
    if (!ts) return '';
    const num = typeof ts === 'string' ? Number(ts) : ts;
    if (!num || isNaN(num)) return '';
    const ms = num < 1_000_000_000_000 ? num * 1000 : num;
    return new Date(ms).toISOString().split('T')[0];
  }

  private dateInputToTs(dateStr: string): number {
    return dateStr ? Math.floor(new Date(dateStr).getTime() / 1000) : 0;
  }

  private validate(): boolean {
    this.validationErrors = {};
    if (!this.form.email?.trim()) this.validationErrors['email'] = 'E-Mail ist erforderlich.';
    if (!this.form.firstName?.trim()) this.validationErrors['firstName'] = 'Vorname ist erforderlich.';
    if (!this.form.lastName?.trim()) this.validationErrors['lastName'] = 'Nachname ist erforderlich.';
    if (!this.form.mobile?.trim()) this.validationErrors['mobile'] = 'Handynummer ist erforderlich.';
    if (this.form.paymentMethod === 'lastschrift') {
      if (!this.form.iban?.trim()) this.validationErrors['iban'] = 'IBAN ist erforderlich.';
      if (!this.form.sepaConsent) this.validationErrors['sepaConsent'] = 'SEPA-Mandat muss erteilt werden.';
    }
    return Object.keys(this.validationErrors).length === 0;
  }

  goBack(): void {
    this.router.navigate(['/bookings']);
  }

  onSubmit(): void {
    if (!this.originalBooking || !this.deliveryId || !this.validate()) return;

    this.isSaving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const payload = {
      adminId: this.authService.getUserId(),
      deliveryId: this.deliveryId,
      // Step 2 fields
      email: this.form.email,
      salutation: this.form.salutation,
      title: this.form.title,
      firstName: this.form.firstName,
      lastName: this.form.lastName,
      mobile: this.form.mobile,
      telephone: this.form.telephone,
      dob: this.dateInputToTs(this.form.dob) || null,
      billingAddress: {
        ...this.originalBooking.billingAddress,
        isDifferent: this.form.hasDifferentBilling,
        ...(this.form.hasDifferentBilling && {
          zip: this.form.billingZip,
          city: this.form.billingCity,
          street: this.form.billingStreet,
          houseNumber: this.form.billingHouseNumber,
        }),
      },
      // Step 3
      connection: this.originalBooking.connection
        ? {
          ...this.originalBooking.connection,
          isMovingIn: this.form.isMovingIn,
          moveInDate: this.dateInputToTs(this.form.moveInDate) || null,
          submitLater: this.form.submitLater,
          meterNumber: this.form.meterNumber,
          marketLocationId: this.form.marketLocationId,
          currentProvider: this.form.currentProvider,
          autoCancellation: this.form.autoCancellation,
          alreadyCancelled: this.form.alreadyCancelled,
          selfCancellation: this.form.selfCancellation,
          delivery: this.form.deliveryOption === 'wunschtermin',
          desiredDelivery:
            this.form.deliveryOption === 'wunschtermin'
              ? this.dateInputToTs(this.form.desiredDeliveryDate) || null
              : null,
        }
        : null,
      // Step 4
      payment: this.originalBooking.payment
        ? {
          ...this.originalBooking.payment,
          paymentMethod: this.form.paymentMethod,
          iban: this.form.iban,
          firstName: this.form.paymentFirstName,
          lastName: this.form.paymentLastName,
          sepaConsent: this.form.sepaConsent,
        }
        : null,
      // Step 5
      contactSchedule: this.form.dayOfWeek
        ? {
          ...(this.originalBooking.contactSchedule ?? {}),
          dayOfWeek: this.form.dayOfWeek,
          timeSlot: this.form.timeSlot,
          description: this.form.scheduleDescription,
        }
        : null,
    };

    this.api.post('admin/update-delivery', payload).subscribe({
      next: () => {
        this.isSaving = false;
        this.successMessage = 'Buchung erfolgreich aktualisiert.';
        setTimeout(() => this.goBack(), 1500);
      },
      error: (err: any) => {
        this.isSaving = false;
        this.errorMessage = err?.error?.message || 'Fehler beim Speichern. Bitte erneut versuchen.';
      },
    });
  }
}