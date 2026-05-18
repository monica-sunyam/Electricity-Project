import { Component, OnInit, OnDestroy, ChangeDetectorRef, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { ContactPerson } from '../../layout/contact-person/contact-person';
import { NeedSupport } from '../../layout/need-support/need-support';
import { AuthService } from '../../services/auth.service';

const API_BASE = 'http://192.168.0.155:8080';

@Component({
  selector: 'app-create-business-account',
  standalone: true,
  imports: [
    ContactPerson,
    NeedSupport,
    MatDatepickerModule,
    MatNativeDateModule,
    CommonModule,
    FormsModule,
    RouterModule,
  ],
  templateUrl: './create-account-business.html',
  styleUrl: './create-account-business.css',
})
export class CreateBusinessAccount implements OnInit, OnDestroy {
  // ── Form fields ──────────────────────────────────────────────
  salutation: string = '';
  title: string = ''; // optional title toggle (Dr. / Prof. / Prof. Dr.)
  firstName: string = '';
  lastName: string = '';
  emailBusiness: string = ''; // E-Mail 1 – geschäftlich (required)
  emailPrivate: string = ''; // E-Mail 2 – privat       (optional)
  phone: string = ''; // Telefonnummer (required)
  mobile: string = ''; // Handynummer   (optional)
  dob: Date | null = null;

  // ── UI state ─────────────────────────────────────────────────
  isLoading: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';
  validationErrors: Record<string, string> = {};
  providerDetails: any = null;

  // ── Datepicker bounds ────────────────────────────────────────
  /** Earliest accepted birth date */
  readonly minDob: Date = new Date(1900, 0, 1);

  /** Latest accepted birth date — must be at least 18 years old */
  readonly maxDob: Date = (() => {
    const d = new Date();
    d.setFullYear(d.getFullYear() - 18);
    d.setHours(23, 59, 59, 999);
    return d;
  })();

  private routerSub?: Subscription;

  isLoggedIn = computed(() => !!this.authService.currentUser()?.user_id);

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private http: HttpClient,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
  ) {}

  // ── Lifecycle ────────────────────────────────────────────────
  ngOnInit(): void {

  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
  }

  private resetFields(): void {
    this.salutation = '';
    this.title = '';
    this.firstName = '';
    this.lastName = '';
    this.emailBusiness = '';
    this.emailPrivate = '';
    this.phone = '';
    this.mobile = '';
    this.dob = null;
    this.validationErrors = {};
    this.successMessage = '';
    this.errorMessage = '';
  }

  private prefillForm(data: any): void {
    this.salutation = data.salutation ?? '';
    this.title = (data.title ?? '').trim();
    this.firstName = data.firstName ?? '';
    this.lastName = data.lastName ?? '';
    this.emailBusiness = data.emailBusiness ?? data.email ?? '';
    this.emailPrivate = data.emailPrivate ?? '';
    this.phone = data.telephone ?? data.phone ?? '';
    this.mobile = data.mobile ?? '';

    if (data.dob) {
      const parsed = new Date(Number(data.dob) * 1000);
      if (!isNaN(parsed.getTime())) {
        this.dob = parsed;
      }
    }

    this.cdr.detectChanges();
  }

  // ── Title toggle helpers ─────────────────────────────────────
  isTitleSelected(t: string): boolean {
    return this.title.trim() === t;
  }

  selectTitle(t: string): void {
    this.title = this.title === t ? '' : t;
  }

  // ── Validation ───────────────────────────────────────────────
  private readonly EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  private readonly PHONE_RE = /^[+\d][\d\s\-/]{6,}$/;

  private validate(): boolean {
    this.validationErrors = {};

    // Salutation
    if (!this.salutation?.trim()) {
      this.validationErrors['salutation'] = 'Bitte wählen Sie eine Anrede aus.';
    }

    // First / Last name
    if (!this.firstName?.trim()) {
      this.validationErrors['firstName'] = 'Bitte geben Sie Ihren Vornamen ein.';
    }
    if (!this.lastName?.trim()) {
      this.validationErrors['lastName'] = 'Bitte geben Sie Ihren Nachnamen ein.';
    }

    // E-Mail 1 – business (required)
    if (!this.emailBusiness?.trim()) {
      this.validationErrors['emailBusiness'] =
        'Bitte geben Sie Ihre geschäftliche E-Mail-Adresse ein.';
    } else if (!this.EMAIL_RE.test(this.emailBusiness.trim())) {
      this.validationErrors['emailBusiness'] = 'Bitte geben Sie eine gültige E-Mail-Adresse ein.';
    }

    // E-Mail 2 – private (optional, validate format only when filled)
    if (this.emailPrivate?.trim() && !this.EMAIL_RE.test(this.emailPrivate.trim())) {
      this.validationErrors['emailPrivate'] = 'Bitte geben Sie eine gültige E-Mail-Adresse ein.';
    }

    // Phone (required)
    if (!this.phone?.trim()) {
      this.validationErrors['phone'] = 'Bitte geben Sie Ihre Telefonnummer ein.';
    } else if (!this.PHONE_RE.test(this.phone.trim())) {
      this.validationErrors['phone'] = 'Bitte geben Sie eine gültige Telefonnummer ein.';
    }

    // Mobile (optional, validate format only when filled)
    if (this.mobile?.trim() && !this.PHONE_RE.test(this.mobile.trim())) {
      this.validationErrors['mobile'] = 'Bitte geben Sie eine gültige Handynummer ein.';
    }

    // Date of birth
    if (!this.dob) {
      this.validationErrors['dob'] = 'Bitte Geburtsdatum auswählen.';
    } else {
      const selected = new Date(this.dob);
      selected.setHours(0, 0, 0, 0);

      const minDate = new Date(1900, 0, 1);
      const maxDate = new Date();
      maxDate.setFullYear(maxDate.getFullYear() - 18);
      maxDate.setHours(0, 0, 0, 0);

      if (selected < minDate) {
        this.validationErrors['dob'] = 'Bitte geben Sie ein gültiges Geburtsdatum ein.';
      } else if (selected > maxDate) {
        this.validationErrors['dob'] = 'Sie müssen mindestens 18 Jahre alt sein.';
      }
    }

    return Object.keys(this.validationErrors).length === 0;
  }

  // ── Submit ───────────────────────────────────────────────────
  submitForm(): void {
    if (!this.validate()) return;

    this.isLoading = true;
    this.errorMessage = '';

    const userId = this.authService.getUserId();
    const deliveryId = this.authService.getDeliveryId();

    const payload = {
      customerId: userId,
      ...(deliveryId && { deliveryId }),
      accountType: 'business',
      salutation: this.salutation,
      title: this.title,
      firstName: this.firstName,
      lastName: this.lastName,
      emailBusiness: this.emailBusiness.trim(),
      emailPrivate: this.emailPrivate.trim() || null,
      phone: this.phone.trim(),
      mobile: this.mobile.trim() || null,
      dob: this.dob ? this.formatDate(this.dob) : null,
    };

    this.http
      .post<{
        res: boolean;
        deliveryId?: number;
      }>(`${API_BASE}/customer/create-business-account`, payload)
      .subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response?.res === true) {
            this.successMessage = 'Account erfolgreich erstellt.';
          } else {
            this.errorMessage = 'Speichern fehlgeschlagen. Bitte versuchen Sie es erneut.';
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = 'Speichern fehlgeschlagen. Bitte versuchen Sie es erneut.';
          console.error('Submit error:', err);
          this.cdr.detectChanges();
        },
      });
  }

  // ── Helpers ──────────────────────────────────────────────────
  private formatDate(date: Date): string {
    const d = new Date(date);
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();
    return `${day}.${month}.${year}`;
  }
}
