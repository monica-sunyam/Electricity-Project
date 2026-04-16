import { MatDatepickerModule } from '@angular/material/datepicker';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute, NavigationEnd } from '@angular/router';
import { Sidebar } from '../../layout/sidebar/sidebar';
import { MatIcon } from '@angular/material/icon';
import { MatNativeDateModule } from '@angular/material/core';
import { ContactPerson } from '../../layout/contact-person/contact-person';
import { NeedSupport } from '../../layout/need-support/need-support';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Subscription } from 'rxjs';
import { take, filter } from 'rxjs/operators';

const API_BASE = 'http://192.168.0.155:8080';

@Component({
  selector: 'app-delivery-address',
  standalone: true,
  imports: [
    Sidebar,
    ContactPerson,
    NeedSupport,
    MatDatepickerModule,
    MatNativeDateModule,
    CommonModule,
    MatIcon,
    FormsModule,
    RouterModule,
  ],
  templateUrl: './delivery-address.html',
  styleUrl: './delivery-address.css',
})
export class DeliveryAddress implements OnInit, OnDestroy {
  deliveryEmail: string = '';
  deliveryTitle: string = '';
  deliveryFirstName: string = '';
  deliveryLastName: string = '';
  deliveryPLZ: string = '';
  deliveryOrt: string = '';
  deliveryStreet: string = '';
  deliveryHouseNumber: string = '';
  deliveryMobile: string = '';
  deliveryPhone: string = '';
  deliveryDate: Date | null = null;

  hasDifferentBilling: boolean = false;

  billingPLZ: string = '';
  billingOrt: string = '';
  billingStreet: string = '';
  billingHouseNumber: string = '';

  isLoading: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';
  validationErrors: Record<string, string> = {};

  private routerSub?: Subscription;
  maxAccessibleStep = 1;

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
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    // Run initial load
    this.initForm();

    this.routerSub = this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd))
      .subscribe((e: any) => {
        if (e.urlAfterRedirects === this.mainStepRoutes[2]) {
          this.resetFields();
          // Use a timeout to ensure the reset finishes before we fetch/fill
          setTimeout(() => {
            this.initForm();
          }, 0);
        }
      });
  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
  }

  /**
   * Resets all form fields to blank, then loads fresh data.
   * Called on init AND on every back-navigation to this route.
   */
  private initForm(): void {
    // Hard-reset all fields first so stale rendered values never survive a
    // re-visit. Without this, fields prefilled on the first visit stay visible
    // while the new HTTP response is in-flight, and if the response arrives
    // before Angular's change detection the assignment is silently discarded
    // because the value hasn't "changed".
    this.resetFields();

    // Apply locked address fields from localStorage (sync, always wins)
    this.applyLocalStorageAddress();

    // Use take(1) so the auth observable only fires once per initForm() call.
    // A BehaviorSubject replays its last value synchronously — without take(1)
    // every future auth change would re-trigger fetchFormData.
    this.authService
      .getAuthState()
      .pipe(take(1))
      .subscribe((user) => {
        // Seed email from auth as a baseline (may be overwritten by API below)
        if (user?.email) {
          this.deliveryEmail = user.email;
        }

        const userId = this.authService.getUserId();
        const deliveryId = this.authService.getDeliveryId();

        if (userId && deliveryId) {
          this.fetchFormData(userId, deliveryId);
        }
      });
  }

  /** Zero out every form field so prefillForm always produces a visible change. */
  private resetFields(): void {
    this.deliveryEmail = '';
    this.deliveryTitle = '';
    this.deliveryFirstName = '';
    this.deliveryLastName = '';
    this.deliveryPLZ = '';
    this.deliveryOrt = '';
    this.deliveryStreet = '';
    this.deliveryHouseNumber = '';
    this.deliveryMobile = '';
    this.deliveryPhone = '';
    this.deliveryDate = null;
    this.hasDifferentBilling = false;
    this.billingPLZ = '';
    this.billingOrt = '';
    this.billingStreet = '';
    this.billingHouseNumber = '';
    this.validationErrors = {};
    this.successMessage = '';
    this.errorMessage = '';
  }

  private applyLocalStorageAddress(): void {
    const storedAddress = this.authService.getAddressData();
    if (storedAddress) {
      this.deliveryPLZ = storedAddress.zip || '';
      this.deliveryOrt = storedAddress.city || '';
      this.deliveryStreet = storedAddress.street || '';
      this.deliveryHouseNumber = storedAddress.houseNumber || '';
    }
  }

  fetchFormData(id: string, deliveryId: string): void {
    this.isLoading = true;
    const payload = {
      customerId: parseInt(id, 10),
      deliveryId: parseInt(deliveryId, 10),
      step: 0,
    };

    this.http.post<any>(`${API_BASE}/customer/fetch-form`, payload).subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res?.res === true && res.data) {
          this.prefillForm(res.data);
          this.maxAccessibleStep = this.getMaxAccessibleStep(res.data);
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Formulardaten konnten nicht geladen werden.';
        console.error('Fetch error:', err);
      },
    });
  }

  private prefillForm(data: any): void {
    // 1. Basic user data
    if (data.email) this.deliveryEmail = data.email;
    this.deliveryTitle = data.title ?? '';
    this.deliveryFirstName = data.firstName ?? '';
    this.deliveryLastName = data.lastName ?? '';
    this.deliveryMobile = data.mobile ?? '';

    if (data.deliveryDate) {
      const timestampInMs = Number(data.deliveryDate) * 1000;
      const parsed = new Date(timestampInMs);

      if (!isNaN(parsed.getTime())) {
        this.deliveryDate = parsed;
      }
    }

    // 3. Address — localStorage wins for locked fields (already applied in
    //    applyLocalStorageAddress). Only use API values if localStorage is empty.
    const localAddr = this.authService.getAddressData();
    if (!localAddr) {
      const addr = data.deliveryAddress ?? data.address ?? data.customerAddress ?? data;
      this.deliveryPLZ = addr?.zip || addr?.plz || '';
      this.deliveryOrt = addr?.city || addr?.ort || '';
      this.deliveryStreet = addr?.street || addr?.strasse || '';
      this.deliveryHouseNumber = addr?.houseNumber || addr?.hausnummer || '';
    }

    // 4. Billing address
    const bill = data.billingAddress;
    this.hasDifferentBilling = !!(bill?.different || bill?.isDifferent);
    if (this.hasDifferentBilling) {
      this.billingPLZ = bill?.zip || '';
      this.billingOrt = bill?.city || '';
      this.billingStreet = bill?.street || '';
      this.billingHouseNumber = bill?.houseNumber || '';
    }

    this.cdr.detectChanges();
  }

  selectTitle(title: string): void {
    this.deliveryTitle = this.deliveryTitle === title ? '' : title;
  }

  setBillingToggle(value: boolean): void {
    this.hasDifferentBilling = value;
  }

  navigateToMainStep(step: number): void {
    if (step > this.maxAccessibleStep) {
      return;
    }

    const route = this.mainStepRoutes[step];
    if (route) this.router.navigate([route]);
  }

  private validate(): boolean {
    this.validationErrors = {};

    if (!this.deliveryEmail?.trim()) {
      this.validationErrors['deliveryEmail'] = 'Bitte geben Sie Ihre E-Mail-Adresse ein.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.deliveryEmail.trim())) {
      this.validationErrors['deliveryEmail'] = 'Bitte geben Sie eine gültige E-Mail-Adresse ein.';
    }

    if (!this.deliveryFirstName?.trim()) {
      this.validationErrors['deliveryFirstName'] = 'Bitte geben Sie Ihren Vornamen ein.';
    }

    if (!this.deliveryLastName?.trim()) {
      this.validationErrors['deliveryLastName'] = 'Bitte geben Sie Ihren Nachnamen ein.';
    }

    if (!this.deliveryMobile?.trim()) {
      this.validationErrors['deliveryMobile'] = 'Bitte geben Sie Ihre Handynummer ein.';
    }

    if (!this.deliveryDate) {
      this.validationErrors['deliveryDate'] = 'Bitte wählen Sie einen Liefertermin.';
    }

    if (this.hasDifferentBilling) {
      if (!this.billingPLZ?.trim()) this.validationErrors['billingPLZ'] = 'Bitte geben Sie Ihre PLZ ein.';
      if (!this.billingOrt?.trim()) this.validationErrors['billingOrt'] = 'Bitte geben Sie Ihren Ort ein.';
      if (!this.billingStreet?.trim()) this.validationErrors['billingStreet'] = 'Bitte geben Sie Ihre Straße ein.';
      if (!this.billingHouseNumber?.trim()) this.validationErrors['billingHouseNumber'] = 'Bitte geben Sie Ihre Hausnummer ein.';
    }

    return Object.keys(this.validationErrors).length === 0;
  }

  openPage(): void {
    if (!this.validate()) return;

    this.isLoading = true;

    const userId = this.authService.getUserId();
    const deliveryId = this.authService.getDeliveryId();

    const payload = {
      customerId: userId,
      ...(deliveryId && { deliveryId }),
      deliveryAddress: {
        email: this.deliveryEmail,
        title: this.deliveryTitle,
        firstName: this.deliveryFirstName,
        lastName: this.deliveryLastName,
        mobile: this.deliveryMobile,
        telephone: this.deliveryPhone,
        deliveryDate: this.deliveryDate ? this.formatDate(this.deliveryDate) : null,
        zip: this.deliveryPLZ,
        city: this.deliveryOrt,
        street: this.deliveryStreet,
        houseNumber: this.deliveryHouseNumber,
      },
      billingAddress: {
        different: this.hasDifferentBilling,
        ...(this.hasDifferentBilling && {
          zip: this.billingPLZ,
          city: this.billingOrt,
          street: this.billingStreet,
          houseNumber: this.billingHouseNumber,
        }),
      },
    };

    this.http
      .post<{ res: boolean; deliveryId: number }>(`${API_BASE}/customer/add-delivery`, payload)
      .subscribe({
        next: (res) => {
          if (res?.deliveryId) {
            this.authService.setDeliveryId(res.deliveryId.toString());
          }
          this.isLoading = false;
          this.successMessage = 'Daten erfolgreich gespeichert.';
          this.router.navigate([this.mainStepRoutes[3]]);
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = 'Speichern fehlgeschlagen. Bitte versuchen Sie es erneut.';
          console.error('Submit error:', err);
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

  private getMaxAccessibleStep(data: any): number {
    const hasAccount = !!(data?.email && data?.firstName && data?.lastName);
    if (!hasAccount) {
      return 1;
    }

    const address = data?.deliveryAddress ?? data?.address ?? data?.customerAddress ?? data;
    const hasDelivery = !!(
      address?.zip &&
      address?.city &&
      address?.street &&
      address?.houseNumber &&
      data?.mobile &&
      data?.deliveryDate
    );

    return hasDelivery ? 3 : 2;
  }
}