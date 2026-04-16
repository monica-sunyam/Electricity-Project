import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { Router, RouterModule, ActivatedRoute, NavigationEnd } from '@angular/router';
import { Sidebar } from '../../layout/sidebar/sidebar';
import { ContactPerson } from '../../layout/contact-person/contact-person';
import { NeedSupport } from '../../layout/need-support/need-support';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

const API_BASE = 'http://192.168.0.155:8080';

interface CustomerConnection {
  isMovingIn?: boolean | null;
  moveInDate?: number | string | null;
  submitLater?: boolean | null;
  meterNumber?: string | null;
  currentProvider?: string | null;
  autoCancellation?: boolean | null;
  alreadyCancelled?: boolean | null;
  selfCancellation?: boolean | null;
  delivery?: boolean | string | number | null;
  desiredDelivery?: boolean | string | number | null;
  deliveryDate?: {
    hasDesiredDate?: boolean | null;
    desiredDate?: number | string | null;
  } | null;
  marketLocationId?: string | null;
}

interface CustomerFormData extends CustomerConnection {
  customerConnection?: CustomerConnection | null;
  connectionData?: CustomerConnection | null;
}

interface FetchFormResponse {
  data?: CustomerFormData | null;
  message?: string;
  res?: boolean;
}

@Component({
  selector: 'app-connection-data',
  imports: [
    MatInputModule,
    MatNativeDateModule,
    MatIconModule,
    CommonModule,
    FormsModule,
    RouterModule,
    MatDatepickerModule,
    Sidebar,
    ContactPerson,
    NeedSupport,
  ],
  templateUrl: './connection-data.html',
  styleUrl: './connection-data.css',
})
export class ConnectionData implements OnInit, OnDestroy {
  // ── Move-in toggle ───────────────────────────────────────────────────────
  selection: string = 'no';

  // ── Connection data fields ───────────────────────────────────────────────
  moveInDate: Date | null = null;
  submitLaterChecked: boolean = false;
  meterNumber: string = '';
  marketLocationId: string = '';

  // ── Cancellation options (only when selection === 'no') ──────────────────
  currentProvider: string = '';
  autoCancellation: boolean = true;
  alreadyCancelled: boolean = false;
  selfCancellation: boolean = false;

  // ── Delivery date options (only when selection === 'no') ─────────────────
  deliveryOption: string = 'schnellstmoeglich'; // 'schnellstmoeglich' | 'wunschtermin'
  desiredDeliveryDate: Date | null = null;

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
    1: '/electricity-comparision/register', // Account (adjust if different)
    2: '/electricity-comparision/delivery-address',
    3: '/electricity-comparision/connection-data', // replace with actual path
    4: '/electricity-comparision/payment-method', // replace with actual path
    5: '/electricity-comparision/checkout', // replace with actual path
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
        if (e.urlAfterRedirects === this.mainStepRoutes[3]) {
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
    this.selection = 'no';
    this.moveInDate = null;
    this.submitLaterChecked = false;
    this.meterNumber = '';
    this.marketLocationId = '';
    this.currentProvider = '';
    this.autoCancellation = true;
    this.alreadyCancelled = false;
    this.selfCancellation = false;
    this.deliveryOption = 'schnellstmoeglich';
    this.desiredDeliveryDate = null;
    this.successMessage = '';
    this.errorMessage = '';
    this.validationErrors = {};
  }

  // ── Toggle helpers ───────────────────────────────────────────────────────

  selectOption(value: string): void {
    this.selection = value;
    if (value !== 'yes') {
      this.moveInDate = null;
      delete this.validationErrors['moveInDate'];
    }
    if (value !== 'no') {
      delete this.validationErrors['currentProvider'];
      delete this.validationErrors['desiredDeliveryDate'];
    }
  }

  selectDeliveryOption(value: string): void {
    this.deliveryOption = value;
    if (value !== 'wunschtermin') {
      this.desiredDeliveryDate = null;
      delete this.validationErrors['desiredDeliveryDate'];
    }
  }

  selectCancellation(type: 'alreadyCancelled' | 'selfCancellation'): void {
    this.alreadyCancelled = type === 'alreadyCancelled';
    this.selfCancellation = type === 'selfCancellation';
  }

  // ── Validation ───────────────────────────────────────────────────────────

  /**
   * Validates all required fields based on the current form state.
   * Populates `validationErrors` with a message for every invalid field.
   * Returns true only when the form is fully valid.
   */
  private validate(): boolean {
    this.validationErrors = {};

    // Move-in date — required only when user is moving in
    if (this.selection === 'yes') {
      if (!this.moveInDate) {
        this.validationErrors['moveInDate'] = 'Bitte wählen Sie ein Einzugsdatum.';
      }
    }

    // Meter number — required unless the user checked "Ich reiche … nach"
    if (!this.submitLaterChecked && !this.meterNumber?.trim()) {
      this.validationErrors['meterNumber'] = 'Bitte geben Sie Ihre Zählernummer ein.';
    }

    if (this.selection === 'no') {
      // Current provider
      if (!this.currentProvider) {
        this.validationErrors['currentProvider'] =
          'Bitte wählen Sie Ihren derzeitigen Stromanbieter.';
      }

      // Desired delivery date — required only when "Wunschtermin" is chosen
      if (this.deliveryOption === 'wunschtermin' && !this.desiredDeliveryDate) {
        this.validationErrors['desiredDeliveryDate'] = 'Bitte wählen Sie Ihren Wunschtermin.';
      }
    }

    return Object.keys(this.validationErrors).length === 0;
  }

  // ── Submit ───────────────────────────────────────────────────────────────

  /** Validate, then build and POST the payload, then navigate to step 4 */
  openPage(): void {
    if (!this.validate()) {
      return;
    }

    const userId = this.authService.getUserId();
    const deliveryId = this.getDeliveryId();

    this.successMessage = '';
    this.errorMessage = '';
    this.isLoading = true;

    const payload = {
      customerId: userId,
      ...(deliveryId && { deliveryId }),
      connectionData: {
        isMovingIn: this.selection === 'yes',
        ...(this.selection === 'yes' && {
          moveInDate: this.moveInDate ? this.formatDate(this.moveInDate) : null,
        }),
        submitLater: this.submitLaterChecked,
        meterNumber: this.meterNumber,
        marketLocationId: this.marketLocationId,
        ...(this.selection === 'no' && {
          currentProvider: this.currentProvider,
          cancellation: {
            autoCancellation: this.autoCancellation,
            alreadyCancelled: this.alreadyCancelled,
            selfCancellation: this.selfCancellation,
          },
          autoCancellation: this.autoCancellation,
          alreadyCancelled: this.alreadyCancelled,
          selfCancellation: this.selfCancellation,
          delivery: this.deliveryOption === 'wunschtermin',
          desiredDelivery:
            this.deliveryOption === 'wunschtermin' && this.desiredDeliveryDate
              ? this.formatDate(this.desiredDeliveryDate)
              : null,
        }),
      },
    };

    console.log('Payload being sent to API:', JSON.stringify(payload, null, 2));

    this.http.post(`${API_BASE}/customer/add-connection`, payload).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Daten erfolgreich gespeichert.';
        this.router.navigate([this.mainStepRoutes[4]]);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage =
          err?.error?.message || 'Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.';
        console.error('Connection data API error:', err);
      },
    });
  }

  private formatDate(date: Date): string {
    const d = new Date(date);
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();
    return `${day}.${month}.${year}`;
  }

  private fetchFormData(): void {
    const userId = this.authService.getUserId();
    const deliveryId = this.getDeliveryId();

    if (!deliveryId) {
      return;
    }

    this.errorMessage = '';
    this.isLoading = true;

    const payload = {
      customerId: parseInt(userId ?? '0', 10),
      deliveryId: parseInt(deliveryId, 10),
      step: 3,
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
        console.error('Connection data fetch-form API error:', err);
      },
    });
  }

  private prefillForm(data: CustomerFormData | null): void {
    const connection = data?.customerConnection || data?.connectionData || data;

    if (!connection) {
      return;
    }

    if (connection.isMovingIn !== null && connection.isMovingIn !== undefined) {
      this.selection = connection.isMovingIn ? 'yes' : 'no';
    }

    this.moveInDate = this.parseStoredDate(connection.moveInDate);
    this.submitLaterChecked = !!connection.submitLater;
    this.meterNumber = connection.meterNumber || '';
    this.marketLocationId = connection.marketLocationId || '';
    this.currentProvider = connection.currentProvider || '';
    this.autoCancellation = connection.autoCancellation ?? true;
    this.alreadyCancelled = !!connection.alreadyCancelled;
    this.selfCancellation = !!connection.selfCancellation;

    const desiredDate =
      this.parseStoredDate(connection.desiredDelivery) ||
      this.parseStoredDate(connection.deliveryDate?.desiredDate) ||
      this.parseStoredDate(connection.delivery);

    if (desiredDate || connection.deliveryDate?.hasDesiredDate) {
      this.deliveryOption = 'wunschtermin';
      this.desiredDeliveryDate = desiredDate;
    } else {
      this.deliveryOption = 'schnellstmoeglich';
      this.desiredDeliveryDate = null;
    }
  }

  private parseStoredDate(value?: boolean | number | string | null): Date | null {
    if (
      value === null ||
      value === undefined ||
      value === '' ||
      typeof value === 'boolean' ||
      (typeof value === 'string' && value.toLowerCase().includes('schnell'))
    ) {
      return null;
    }

    if (typeof value === 'number' || /^\d+$/.test(value)) {
      const numericValue = Number(value);
      const milliseconds = numericValue < 1000000000000 ? numericValue * 1000 : numericValue;
      const parsed = new Date(milliseconds);

      return Number.isNaN(parsed.getTime()) ? null : parsed;
    }

    const germanDate = /^(\d{2})\.(\d{2})\.(\d{4})$/.exec(value);
    if (germanDate) {
      const [, day, month, year] = germanDate;
      return new Date(Number(year), Number(month) - 1, Number(day));
    }

    const parsed = new Date(value);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  }

  private getDeliveryId(): string | null {
    return (
      this.authService.getDeliveryId() ||
      this.route.snapshot.queryParamMap.get('deliveryId') ||
      this.route.snapshot.queryParamMap.get('deliveryid') ||
      this.route.snapshot.paramMap.get('deliveryId') ||
      this.route.snapshot.paramMap.get('deliveryid')
    );
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

    let maxStep = this.isAccountAndDeliveryComplete(data) ? 3 : 2;
    if (!this.isAccountAndDeliveryComplete(data)) {
      return 2;
    }

    if (this.isConnectionComplete(data.customerConnection || data.connectionData || data)) {
      maxStep = 4;
    }

    return maxStep;
  }

  private isAccountAndDeliveryComplete(data: CustomerFormData): boolean {
    const source: any = data as any;
    const address = source.address || source.deliveryAddress || source.customerAddress || source;
    return !!(
      source.email &&
      source.firstName &&
      source.lastName &&
      address?.zip &&
      address?.city &&
      address?.street &&
      address?.houseNumber &&
      source.mobile &&
      source.deliveryDate
    );
  }

  private isConnectionComplete(connection: CustomerConnection): boolean {
    const hasMeter = !!connection.submitLater || !!connection.meterNumber;
    if (!hasMeter) {
      return false;
    }

    if (connection.isMovingIn) {
      return !!connection.moveInDate;
    }

    if (!connection.currentProvider) {
      return false;
    }

    return (
      (connection.delivery !== null && connection.delivery !== undefined) ||
      (connection.desiredDelivery !== null && connection.desiredDelivery !== undefined)
    );
  }
}
