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
import { Component, OnInit, OnDestroy, ChangeDetectorRef, computed } from '@angular/core';
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
  salutation: string = '';
  deliveryLastName: string = '';
  deliveryPLZ: string = '';
  deliveryOrt: string = '';
  deliveryStreet: string = '';
  deliveryHouseNumber: string = '';
  deliveryMobile: string = '';
  deliveryPhone: string = '';
  dob: Date | null = null;
  providerDetails: any = null;
  hasDifferentBilling: boolean = false;

  billingPLZ: string = '';
  billingOrt: string = '';
  billingStreet: string = '';
  billingHouseNumber: string = '';

  isLoading: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';
  validationErrors: Record<string, string> = {};

  /**
   * Datepicker bounds for the delivery date.
   * min  → today (no past dates)
   * max  → 2 years from today (reasonable upper bound)
   */
  // readonly minDeliveryDate: Date = (() => {
  //   const d = new Date();
  //   d.setHours(0, 0, 0, 0);
  //   return d;
  // })();

  // readonly maxDeliveryDate: Date = (() => {
  //   const d = new Date();
  //   d.setFullYear(d.getFullYear() + 2);
  //   d.setHours(23, 59, 59, 999);
  //   return d;
  // })();

  readonly minDob: Date = new Date(1900, 0, 1);

  readonly maxDob: Date = (() => {
    const d = new Date();
    d.setFullYear(d.getFullYear() - 18);
    d.setHours(23, 59, 59, 999);
    return d;
  })();

  private routerSub?: Subscription;
  maxAccessibleStep = 1;
  private readonly currentStep = 2;

  private readonly mainStepRoutes: Record<number, string> = {
    1: '/electricity-comparision/register',
    2: '/electricity-comparision/delivery-address',
    3: '/electricity-comparision/connection-data',
    4: '/electricity-comparision/payment-method',
    5: '/electricity-comparision/checkout',
  };

  isLoggedIn = computed(() => !!this.authService.currentUser()?.user_id);

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private http: HttpClient,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.initForm();

    this.routerSub = this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd))
      .subscribe((e: any) => {
        if (e.urlAfterRedirects === this.mainStepRoutes[2]) {
          this.resetFields();
          setTimeout(() => this.initForm(), 0);
        }
      });
  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
  }

  private initForm(): void {
    this.resetFields();
    this.applyLocalStorageAddress();

    this.authService
      .getAuthState()
      .pipe(take(1))
      .subscribe((user) => {
        if (user?.email) {
          this.deliveryEmail = user.email;
        }

        const userId = this.authService.getUserId();
        const deliveryId = this.authService.getDeliveryId();

        if (userId && deliveryId) {
          this.fetchFormData(userId, deliveryId);
        } else {
          if (this.isLoggedIn()) {
            this.authService.fetchCustomer();
          }

          this.authService.getCustomerData().subscribe((data) => {
            if (!data) return;

            this.deliveryFirstName = data.firstName || '';
            this.deliveryLastName = data.lastName || '';
            this.deliveryMobile = data.phone || '';
            this.salutation = data.salutation || '';

            console.log('Customer data:', data);

            this.cdr.detectChanges();
            console.log('Customer data updated in DeliveryAddress:', data);
            console.log('firstName:', this.deliveryFirstName, 'lastName:', this.deliveryLastName);
          });
        }
      });
  }

  private resetFields(): void {
    this.deliveryEmail = '';
    // this.deliveryTitle = '';
    // this.deliveryFirstName = '';
    // this.deliveryLastName = '';
    this.deliveryPLZ = '';
    this.deliveryOrt = '';
    this.deliveryStreet = '';
    this.deliveryHouseNumber = '';
    this.deliveryMobile = '';
    this.deliveryPhone = '';
    this.dob = null;
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
    this.providerDetails = {
      ...this.authService.getSelectedProvider(),
      consumption: storedAddress?.consumption || null,
    };
  }

  fetchFormData(id: string, deliveryId: string): void {
    // this.isLoading = true;
    const payload = {
      customerId: parseInt(id, 10),
      deliveryId: parseInt(deliveryId, 10),
      step: 0,
    };

    this.http.post<any>(`${API_BASE}/customer/fetch-form`, payload).subscribe({
      next: (res) => {
        // this.isLoading = false;
        if (res?.res === true && res.data) {
          this.prefillForm(res.data);
          this.maxAccessibleStep = this.getMaxAccessibleStep(res.data);
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        // this.isLoading = false;
        this.errorMessage = 'Formulardaten konnten nicht geladen werden.';
        console.error('Fetch error:', err);
        this.cdr.detectChanges();
      },
    });
  }
  isTitleSelected(title: string): boolean {
    return (this.deliveryTitle || '').trim() === title;
  }
  private prefillForm(data: any): void {
    if (data.email) this.deliveryEmail = data.email;
    // this.deliveryTitle = data.title ?? '';
    this.deliveryFirstName = data.firstName ?? '';
    this.deliveryLastName = data.lastName ?? '';
    this.deliveryMobile = data.mobile ?? '';
    this.deliveryPhone = data.telephone ?? data.phone ?? '';
    this.deliveryTitle = (data.title || '').trim();
    this.deliveryTitle = this.deliveryTitle + '';
    this.salutation = data.salutation ?? '';

    if (data.dob) {
      const timestampInMs = Number(data.dob) * 1000;
      const parsed = new Date(timestampInMs);
      if (!isNaN(parsed.getTime())) {
        this.dob = parsed;
      }
    }

    const localAddr = this.authService.getAddressData();
    if (!localAddr) {
      const addr = data.deliveryAddress ?? data.address ?? data.customerAddress ?? data;
      this.deliveryPLZ = addr?.zip || addr?.plz || '';
      this.deliveryOrt = addr?.city || addr?.ort || '';
      this.deliveryStreet = addr?.street || addr?.strasse || '';
      this.deliveryHouseNumber = addr?.houseNumber || addr?.hausnummer || '';
    }

    const bill = data.billingAddress;
    this.hasDifferentBilling = !!(bill?.different || bill?.isDifferent);
    if (this.hasDifferentBilling) {
      this.billingPLZ = bill?.zip || '';
      this.billingOrt = bill?.city || '';
      this.billingStreet = bill?.street || '';
      this.billingHouseNumber = bill?.houseNumber || '';
    } else {
      this.billingPLZ = '';
      this.billingOrt = '';
      this.billingStreet = '';
      this.billingHouseNumber = '';
    }

    this.cdr.detectChanges();
    console.log('TITLE:', this.deliveryTitle);
  }

  selectTitle(title: string): void {
    this.deliveryTitle = this.deliveryTitle === title ? '' : title;
  }

  setBillingToggle(value: boolean): void {
    this.hasDifferentBilling = value;
  }

  navigateToMainStep(step: number): void {
    if (step > this.currentStep || step > this.maxAccessibleStep) return;
    const route = this.mainStepRoutes[step];
    if (route) this.router.navigate([route]);
  }

  private validate(): boolean {
    this.validationErrors = {};

    // E-Mail
    if (!this.deliveryEmail?.trim()) {
      this.validationErrors['deliveryEmail'] = 'Bitte geben Sie Ihre E-Mail-Adresse ein.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.deliveryEmail.trim())) {
      this.validationErrors['deliveryEmail'] = 'Bitte geben Sie eine gültige E-Mail-Adresse ein.';
    }

    // Title (Titel)
    // if (!this.deliveryTitle?.trim()) {
    //   this.validationErrors['deliveryTitle'] = 'Bitte wählen Sie einen Titel aus.';
    // }

    // Salutation
    console.log('Validating salutation:', this.salutation);
    if (!this.salutation?.trim()) {
      this.validationErrors['salutation'] = 'Bitte wählen Sie eine Anrede aus.';
    }

    // Vorname / Nachname
    if (!this.deliveryFirstName?.trim()) {
      this.validationErrors['deliveryFirstName'] = 'Bitte geben Sie Ihren Vornamen ein.';
    }
    if (!this.deliveryLastName?.trim()) {
      this.validationErrors['deliveryLastName'] = 'Bitte geben Sie Ihren Nachnamen ein.';
    }

    // Handynummer (required) — digits, spaces, +, hyphens only
    if (!this.deliveryMobile?.trim()) {
      this.validationErrors['deliveryMobile'] = 'Bitte geben Sie Ihre Handynummer ein.';
    } else if (!/^[+\d][\d\s\-/]{6,}$/.test(this.deliveryMobile.trim())) {
      this.validationErrors['deliveryMobile'] = 'Bitte geben Sie eine gültige Handynummer ein.';
    }

    // Telefonnummer (optional) — validate format only when filled in
    if (this.deliveryPhone?.trim() && !/^[+\d][\d\s\-/]{6,}$/.test(this.deliveryPhone.trim())) {
      this.validationErrors['deliveryPhone'] = 'Bitte geben Sie eine gültige Telefonnummer ein.';
    }

    // Liefertermin
    // if (!this.deliveryDate) {
    //   this.validationErrors['deliveryDate'] = 'Bitte wählen Sie einen Liefertermin.';
    // } else {
    //   const selected = new Date(this.deliveryDate);
    //   selected.setHours(0, 0, 0, 0);

    //   if (selected < this.minDeliveryDate) {
    //     this.validationErrors['deliveryDate'] =
    //       'Der Liefertermin darf nicht in der Vergangenheit liegen.';
    //   } else if (selected > this.maxDeliveryDate) {
    //     this.validationErrors['deliveryDate'] =
    //       'Der Liefertermin darf maximal 2 Jahre in der Zukunft liegen.';
    //   }
    // }

    if (!this.dob) {
      this.validationErrors['dob'] = 'Bitte Geburtsdatum auswählen.';
    } else {
      const selected = new Date(this.dob);
      selected.setHours(0, 0, 0, 0);

      const today = new Date();
      today.setHours(0, 0, 0, 0);

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

    // Billing address fields (only when different billing is selected)
    if (this.hasDifferentBilling) {
      if (!this.billingPLZ?.trim())
        this.validationErrors['billingPLZ'] = 'Bitte geben Sie Ihre PLZ ein.';
      if (!this.billingOrt?.trim())
        this.validationErrors['billingOrt'] = 'Bitte geben Sie Ihren Ort ein.';
      if (!this.billingStreet?.trim())
        this.validationErrors['billingStreet'] = 'Bitte geben Sie Ihre Straße ein.';
      if (!this.billingHouseNumber?.trim())
        this.validationErrors['billingHouseNumber'] = 'Bitte geben Sie Ihre Hausnummer ein.';
    }

    return Object.keys(this.validationErrors).length === 0;
  }

  openPage(): void {
    // Provider validation
    if (!this.providerDetails) {
      this.errorMessage = 'Bitte wählen Sie zuerst einen Anbieter aus.';
      alert(this.errorMessage);
      return;
    }
    if (!this.validate()) return;

    this.isLoading = true;

    const userId = this.authService.getUserId();
    const deliveryId = this.authService.getDeliveryId();

    const payload = {
      customerId: userId,
      ...(deliveryId && { deliveryId }),
      provider: this.providerDetails,
      deliveryAddress: {
        email: this.deliveryEmail,
        title: this.deliveryTitle,
        firstName: this.deliveryFirstName,
        lastName: this.deliveryLastName,
        salutation: this.salutation,
        mobile: this.deliveryMobile,
        telephone: this.deliveryPhone,
        dob: this.dob ? this.formatDate(this.dob) : null,
        zip: this.deliveryPLZ,
        city: this.deliveryOrt,
        street: this.deliveryStreet,
        houseNumber: this.deliveryHouseNumber,
        deliveryType: 'electricity',
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
        next: (response) => {
          if (response && response.res === true) {
            if (response.deliveryId) {
              this.authService.setDeliveryId(response.deliveryId.toString());
            }
            this.isLoading = false;
            this.successMessage = 'Daten erfolgreich gespeichert.';
            this.router.navigate([this.mainStepRoutes[3]]);
          } else {
            this.isLoading = false;
            this.errorMessage = 'Speichern fehlgeschlagen. Bitte versuchen Sie es erneut.';
            console.error('Submit error: API response returned false', response);
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

  private formatDate(date: Date): string {
    const d = new Date(date);
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();
    return `${day}.${month}.${year}`;
  }

  private getMaxAccessibleStep(data: any): number {
    const hasAccount = !!(data?.email && data?.firstName && data?.lastName);
    if (!hasAccount) return 1;

    const address = data?.deliveryAddress ?? data?.address ?? data?.customerAddress ?? data;
    const hasDelivery = !!(
      address?.zip &&
      address?.city &&
      address?.street &&
      address?.houseNumber &&
      data?.mobile &&
      data?.dob
    );

    return hasDelivery ? 3 : 2;
  }
}
