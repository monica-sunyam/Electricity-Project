import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute, NavigationEnd } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ContactPerson } from '../../layout/contact-person/contact-person';
import { NeedSupport } from '../../layout/need-support/need-support';
import { Sidebar } from '../../layout/sidebar/sidebar';
import { AuthService } from '../../services/auth.service';

import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

const API_BASE = 'http://192.168.0.155:8080';

interface CustomerPayment {
  paymentMethod?: string | null;
  iban?: string | null;
  accountHolderFirstName?: string | null;
  accountHolderLastName?: string | null;
  accountHolder?: {
    firstName?: string | null;
    lastName?: string | null;
  } | null;
  sepaConsent?: boolean | null;
}

interface CustomerFormData extends CustomerPayment {
  customerPayment?: CustomerPayment | null;
  paymentData?: CustomerPayment | null;
}

interface FetchFormResponse {
  data?: CustomerFormData | null;
  message?: string;
  res?: boolean;
}

@Component({
  selector: 'app-payment-method',
  imports: [
    Sidebar,
    ContactPerson,
    NeedSupport,
    MatInputModule,
    MatIconModule,
    CommonModule,
    FormsModule,
    RouterModule,
  ],
  templateUrl: './payment-method.html',
  styleUrl: './payment-method.css',
})
export class PaymentMethod implements OnInit, OnDestroy {
  // ── Payment method toggle ────────────────────────────────────────────────
  paymentMethod: string = 'ueberweisung'; // 'lastschrift' | 'ueberweisung'

  // ── SEPA / Lastschrift fields ────────────────────────────────────────────
  iban: string = '';
  firstName: string = '';
  lastName: string = '';
  sepaConsent: boolean = false;

  // ── UI state ─────────────────────────────────────────────────────────────
  isLoading: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';

  /** Per-field validation error messages shown inline under each input */
  validationErrors: Record<string, string> = {};
  private routerSub?: Subscription;
  maxAccessibleStep = 1;

  // ── Main progress-bar step routes ────────────────────────────────────────
  private readonly mainStepRoutes: Record<number, string> = {
    1: '/electricity-comparision/register',
    2: '/electricity-comparision/delivery-address',
    3: '/electricity-comparision/connection-data',
    4: '/electricity-comparision/payment-method',
    5: '/electricity-comparision/checkout',
  };

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private http: HttpClient,
    private authService: AuthService,
  ) { }

  ngOnInit(): void {
    this.initForm();

    this.routerSub = this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd))
      .subscribe((e: any) => {
        if (e.urlAfterRedirects === this.mainStepRoutes[4]) {
          this.initForm();
        }
      });
  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
  }

  private initForm(): void {
    this.resetFields();
    this.fetchFormData();
  }

  private resetFields(): void {
    this.paymentMethod = 'ueberweisung';
    this.iban = '';
    this.firstName = '';
    this.lastName = '';
    this.sepaConsent = false;
    this.successMessage = '';
    this.errorMessage = '';
    this.validationErrors = {};
  }

  // ── Toggle helpers ───────────────────────────────────────────────────────

  /** Toggle payment method (Lastschrift / Überweisung) */
  selectPaymentMethod(value: string): void {
    this.paymentMethod = value;
    // Reset SEPA fields and validation errors when switching away from Lastschrift
    if (value !== 'lastschrift') {
      this.iban = '';
      this.firstName = '';
      this.lastName = '';
      this.sepaConsent = false;
      delete this.validationErrors['iban'];
      delete this.validationErrors['firstName'];
      delete this.validationErrors['lastName'];
      delete this.validationErrors['sepaConsent'];
    }
  }

  // ── Validation ───────────────────────────────────────────────────────────

  /**
   * Validates all required fields based on the current form state.
   * Populates `validationErrors` with a message for every invalid field.
   * Returns true only when the form is fully valid.
   */
  private validate(): boolean {
    this.validationErrors = {};

    if (this.paymentMethod === 'lastschrift') {
      // IBAN — required and must be non-empty
      if (!this.iban?.trim()) {
        this.validationErrors['iban'] = 'Bitte geben Sie Ihre IBAN ein.';
      }

      // First name — required
      if (!this.firstName?.trim()) {
        this.validationErrors['firstName'] = 'Bitte geben Sie Ihren Vornamen ein.';
      }

      // Last name — required
      if (!this.lastName?.trim()) {
        this.validationErrors['lastName'] = 'Bitte geben Sie Ihren Nachnamen ein.';
      }

      // SEPA consent — must be accepted
      if (!this.sepaConsent) {
        this.validationErrors['sepaConsent'] = 'Bitte stimmen Sie dem SEPA-Lastschriftmandat zu.';
      }
    }

    return Object.keys(this.validationErrors).length === 0;
  }

  // ── Submit ───────────────────────────────────────────────────────────────

  /** Validate, then build and POST the payload, then navigate to step 5 */
  openPage(): void {
    if (!this.validate()) {
      return;
    }

    const userId = this.authService.getUserId();
    const deliveryId = this.authService.getDeliveryId();

    this.successMessage = '';
    this.errorMessage = '';
    this.isLoading = true;

    const payload = {
      customerId: userId,
      ...(deliveryId && { deliveryId }),
      paymentData: {
        paymentMethod: this.paymentMethod,
        ...(this.paymentMethod === 'lastschrift' && {
          iban: this.iban,
          accountHolder: {
            firstName: this.firstName,
            lastName: this.lastName,
          },
          sepaConsent: this.sepaConsent,
        }),
      },
    };

    console.log('Payload being sent to API:', JSON.stringify(payload, null, 2));

    this.http.post(`${API_BASE}/customer/add-payment`, payload).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Zahlungsart erfolgreich gespeichert.';
        this.router.navigate([this.mainStepRoutes[5]]);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage =
          err?.error?.message || 'Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.';
        console.error('Payment method API error:', err);
      },
    });
  }

  private fetchFormData(): void {
    const userId = this.authService.getUserId();
    const deliveryId = this.authService.getDeliveryId();

    if (!deliveryId) {
      return;
    }

    this.errorMessage = '';
    this.isLoading = true;

    const payload = {
      customerId: parseInt(userId ?? '0', 10),
      deliveryId: parseInt(deliveryId, 10),
      step: 4,
    };

    this.http.post<FetchFormResponse>(`${API_BASE}/customer/fetch-form`, payload).subscribe({
      next: (res) => {
        this.isLoading = false;

        if (res?.res === false) {
          this.errorMessage = res?.message || 'Die gespeicherten Daten konnten nicht geladen werden.';
          return;
        }

        this.prefillForm(res?.data ?? null);
        this.maxAccessibleStep = this.getMaxAccessibleStep(res?.data ?? null);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage =
          err?.error?.message || 'Die gespeicherten Daten konnten nicht geladen werden.';
        console.error('Payment method fetch-form API error:', err);
      },
    });
  }

  private prefillForm(data: CustomerFormData | null): void {
    const payment = data?.customerPayment || data?.paymentData || data;

    if (!payment) {
      return;
    }

    this.paymentMethod = this.normalizePaymentMethod(payment.paymentMethod);
    this.iban = payment.iban || '';
    this.firstName = payment.accountHolderFirstName || payment.accountHolder?.firstName || '';
    this.lastName = payment.accountHolderLastName || payment.accountHolder?.lastName || '';
    this.sepaConsent = !!payment.sepaConsent;
  }

  private normalizePaymentMethod(paymentMethod?: string | null): string {
    const normalized = (paymentMethod || '').trim().toLowerCase();

    if (normalized === 'lastschrift') {
      return 'lastschrift';
    }

    if (normalized.includes('berweisung')) {
      return 'ueberweisung';
    }

    return this.paymentMethod;
  }

  navigateToMainStep(step: number): void {
    if (step > this.maxAccessibleStep) {
      return;
    }

    const route = this.mainStepRoutes[step];
    if (route) {
      this.router.navigate([route]);
    }
  }

  private getMaxAccessibleStep(data: CustomerFormData | null): number {
    if (!data) {
      return 1;
    }

    let maxStep = this.isAccountDeliveryConnectionComplete(data) ? 4 : 3;
    if (!this.isAccountDeliveryConnectionComplete(data)) {
      return 3;
    }

    const payment = data.customerPayment || data.paymentData || data;
    if (this.isPaymentComplete(payment)) {
      maxStep = 5;
    }

    return maxStep;
  }

  private isAccountDeliveryConnectionComplete(data: any): boolean {
    const address = data?.address || data?.deliveryAddress || data;
    const connection = data?.customerConnection || data?.connectionData;
    const hasDelivery = !!(
      data?.email &&
      data?.firstName &&
      data?.lastName &&
      address?.zip &&
      address?.city &&
      address?.street &&
      address?.houseNumber &&
      data?.mobile &&
      data?.deliveryDate
    );
    if (!hasDelivery) {
      return false;
    }

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

    return !!connection.currentProvider;
  }

  private isPaymentComplete(payment: CustomerPayment | null | undefined): boolean {
    if (!payment?.paymentMethod) {
      return false;
    }

    const method = payment.paymentMethod.toLowerCase();
    if (method.includes('lastschrift')) {
      const first = payment.accountHolderFirstName || payment.accountHolder?.firstName;
      const last = payment.accountHolderLastName || payment.accountHolder?.lastName;
      return !!(payment.iban && first && last && payment.sepaConsent);
    }

    return true;
  }
}
