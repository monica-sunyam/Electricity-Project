import { ChangeDetectorRef, Component, ElementRef, HostListener, ViewChild } from '@angular/core';
import { ContactPerson } from '../../layout/contact-person/contact-person';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { Router } from 'express';
import { CommonModule } from '@angular/common';
import SignaturePad from 'signature_pad';
import { CountdownConfig, CountdownEvent, CountdownComponent } from 'ngx-countdown';
import { environment } from '../../environments/environment';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';

const API_BASE = 'http://192.168.0.155:8080';
@Component({
  selector: 'app-customer',
  imports: [
    CommonModule,
    CountdownComponent,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    FormsModule,
  ],
  templateUrl: './customer.html',
  styleUrl: './customer.css',
})
export class Customer {
  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    private eRef: ElementRef,
    // private router: Router,
  ) {}

  tabs: string[] = [
    'Persönliche Daten / Verträge',
    'Mein Haushalt',
    'Zählerstand melden',
    'Wechselinnerung',
    'Serviceanfragen',
    'Beratervollmacht',
    'Dokumentenarchiv',
    'Passwort zurücksetzen',
  ];

  /* ── Tab control ──────────────────────────────────────────────── */
  activeTab: number = 4;
  serviceTab: number = 1;
  /* ── Step control ──────────────────────────────────────────────── */
  currentStep: number = 1;
  /* ── Customer Type (Personal/Business) ──────────────────────────────────────────────── */
  customerType: number = 1;

  customerData: any = {
    id: null,
    name: '',
    email: '',
    phone: '',
    salutation: '',
    title: '',
    userType: '',
    companyName: '',
    isVerified: false,
    joinedOn: null,
    address: {
      zip: '',
      city: '',
      street: '',
      houseNumber: '',
    },
    deliveryDetails: [],
  };
  isLoading = true;

  setActiveTab(index: number) {
    this.activeTab = index;
    this.currentStep = 1;
    this.serviceTab = 1;
  }

  nextStep(step: number) {
    this.currentStep = step;
    if (step === 2 && this.activeTab === 5) {
      setTimeout(() => {
        this.initSignature();
      });
    }

    if (step === 2 && this.activeTab === 7) {
      setTimeout(() => {
        this.isResendDisabled = true;
        this.resendSuccess = false;

        if (this.countdown) {
          this.countdown.restart();
        }
      }, 0);
    }
  }

  showLogoutModal: boolean = false;

  openLogoutModal() {
    this.showLogoutModal = true;
  }

  closeLogoutModal() {
    this.showLogoutModal = false;
  }

  logout() {
    this.showLogoutModal = false;

    console.log('Logged out');

    this.authService.logout();
  }

  ngOnInit(): void {
    this.fetchCustomer();
  }

  /*── Fetch customer details ──*/

  private fetchCustomer(): void {
    const customerId = this.authService.getUserId() || 0;

    const body = {
      id: Number(customerId),
    };

    this.isLoading = true;

    this.http
      .post<any>('http://192.168.0.155:8080/customer/fetch-customer-detail', body)
      .subscribe({
        next: (res) => {
          if (!res?.res || !res?.data) {
            console.error('Invalid response');
            this.isLoading = false;
            return;
          }

          const data = res.data;

          this.customerData = {
            id: data.id,
            name: `${data?.firstName || ''} ${data?.lastName || ''}`.trim(),
            email: data.email || '',
            phone: data.mobileNumber || '',
            salutation: data.salutation || '',
            title: data.title || '',
            userType: data.userType || '',
            companyName: data.companyName || '',
            isVerified: data.isVerified || false,
            joinedOn: data.joinedOn || null,

            address: {
              zip: data?.address?.zip || '',
              city: data?.address?.city || '',
              street: data?.address?.street || '',
              houseNumber: data?.address?.houseNumber || '',
            },

            deliveryDetails: data.deliveryDetails || [],
          };

          console.log('customerData:', this.customerData);

          this.cdr.detectChanges();
          this.isLoading = false;
        },
        error: (err) => {
          console.error('API Error:', err);
          this.isLoading = false;
        },
      });
  }

  /* ════════════════════════════════════════════════════════════════════════════════════════════════*/

  /*── Service Section Start ──*/

  showList = true;
  showDetails = false;
  confirmationList = false;
  showDropdown = false;
  selectedIndex: number = -1; // -1 = Orange card selected by default
  selectedCategory = '';

  categories = [
    'Rechnung / Billing',
    'Vertrag / Contract',
    'Zähler / Meter',
    'Stromausfall / Power outage',
    'Tarif / Pricing',
    'Allgemeine Anfrage / General inquiry',
  ];

  selectCard(index: number) {
    this.selectedIndex = index;
  }

  selectOrangeCard() {
    this.selectedIndex = -1;
  }
  toggleDropdown(event: Event) {
    event.stopPropagation();
    this.showDropdown = !this.showDropdown;
  }

  selectCategory(item: string, event: Event) {
    event.stopPropagation();
    this.selectedCategory = item;
    this.showDropdown = false;
  }

  // Outside click listener
  @HostListener('document:click', ['$event'])
  clickOutside(event: Event) {
    if (!this.eRef.nativeElement.contains(event.target)) {
      this.showDropdown = false;
    }
  }

  openDetails() {
    this.showList = false;
    this.showDetails = true;
  }

  toggleService(step: number) {
    this.serviceTab = step;

    this.showList = true;
    this.showDetails = false;
    this.confirmationList = false;
  }
  backToList() {
    this.showList = true;
    this.showDetails = false;
  }
  confirmation() {
    this.showList = false;
    this.showDetails = false;
    this.confirmationList = true;
  }

  cards = [
    {
      logo: 'assets/icons/Icons_energyprovider/eon.png',
      title: 'E.ON Ökostrom Extra 12',
      date: '27.03.2026',
      data: [
        { label: 'Zählernummer:', value: 'XXX-1399-24689-07', icon: 'meter' },
        { label: 'Adresse:', value: 'Marie Mustermann, Musterstraße 4a', icon: 'home' },
        { label: 'Stromtyp:', value: 'Hausstrom', icon: 'current' },
        { label: 'Vertragsnummer:', value: 'ÖKO-234-6789-87451', icon: 'doc' },
      ],
    },
    {
      logo: 'assets/icons/Icons_energyprovider/vattenfall.png',
      title: 'Strom XXL Extra 12',
      date: '14.09.2026',
      data: [
        { label: 'Zählernummer:', value: 'B-3951-875429-07', icon: 'meter' },
        {
          label: 'Adresse:',
          value: 'Marie Mustermann Mustertraße 4a 67890 Musterhausen',
          icon: 'home',
        },
        { label: 'Stromtyp:', value: 'Hausstrom', icon: 'current' },
        { label: 'Vertragsnummer:', value: 'Flex-555-697945100', icon: 'doc' },
      ],
    },
    {
      logo: 'assets/icons/Icons_energyprovider/eon.png',
      title: 'E.ON Ökostrom Extra 12',
      date: '27.03.2026',
      data: [
        { label: 'Zählernummer:', value: 'XXX-1399-24689-07', icon: 'meter' },
        { label: 'Adresse:', value: 'Marie Mustermann, Musterstraße 4a', icon: 'home' },
        { label: 'Stromtyp:', value: 'Hausstrom', icon: 'current' },
        { label: 'Vertragsnummer:', value: 'ÖKO-234-6789-87451', icon: 'doc' },
      ],
    },
    {
      logo: 'assets/icons/Icons_energyprovider/vattenfall.png',
      title: 'Strom XXL Extra 12',
      date: '14.09.2026',
      data: [
        { label: 'Zählernummer:', value: 'B-3951-875429-07', icon: 'meter' },
        {
          label: 'Adresse:',
          value: 'Marie Mustermann Mustertraße 4a 67890 Musterhausen',
          icon: 'home',
        },
        { label: 'Stromtyp:', value: 'Hausstrom', icon: 'current' },
        { label: 'Vertragsnummer:', value: 'Flex-555-697945100', icon: 'doc' },
      ],
    },
  ];

  normalizeIcon(icon: string): string {
    if (!icon) return 'meter';

    icon = icon.toLowerCase();

    if (icon.includes('meter')) return 'meter';
    if (icon.includes('home')) return 'home';
    if (icon.includes('current') || icon.includes('electric')) return 'current';
    if (icon.includes('doc')) return 'doc';

    return 'meter';
  }

  @ViewChild('scrollContainer', { static: false })
  scrollContainer!: ElementRef;

  scrollLeft() {
    this.scrollContainer.nativeElement.scrollBy({
      left: -330,
      behavior: 'smooth',
    });
  }

  scrollRight() {
    this.scrollContainer.nativeElement.scrollBy({
      left: 330,
      behavior: 'smooth',
    });
  }

  /*── Service Section End ──*/

  /* ════════════════════════════════════════════════════════════════════════════════════════════════*/

  /*── Power of Attorney Section Start ──*/
  /* ── Signature ─── */

  @ViewChild('canvas') canvasRef!: ElementRef<HTMLCanvasElement>;
  signaturePad!: SignaturePad;

  initSignature() {
    if (!this.canvasRef) return;

    const canvas = this.canvasRef.nativeElement;

    const ratio = Math.max(window.devicePixelRatio || 1, 1);
    canvas.width = canvas.offsetWidth * ratio;
    canvas.height = canvas.offsetHeight * ratio;
    canvas.getContext('2d')!.scale(ratio, ratio);

    this.signaturePad = new SignaturePad(canvas);
  }

  clear() {
    this.signaturePad.clear();
  }

  /*── Power of Attorney Section End ──*/

  /* ════════════════════════════════════════════════════════════════════════════════════════════════*/

  /* ──  Reset Password Section Start ──*/
  /* ══════════════════════════════════════════════════════════════════
  STEP 1 — Password VALIDATION
  ══════════════════════════════════════════════════════════════════ */

  /* ── Password validation flags ──────────────────────────────────── */
  pw_length: boolean = false;
  pw_case: boolean = false;
  pw_special: boolean = false;
  pw_number: boolean = false;
  showPw: boolean = false;
  showOldPw: boolean = false;
  showRepPw: boolean = false;
  otpError: string = '';
  newPassword: string = '';
  oldPassword: string = '';
  confirmPassword: string = '';
  passwordMismatch: boolean = false;
  otpValue: string = '';
  otpInvalid = false;
  newOtp = false;
  isLoadingReset: boolean = false;
  apiError: string = '';
  resendSuccess: boolean = false;
  /* ── Field-level validation errors ─────────────────────────────── */
  fieldErrors: Record<string, string> = {};

  validatePassword(password: string, repeat: string) {
    this.newPassword = password;

    this.pw_length = password.length >= 8 && password.length <= 50;
    this.pw_case = /[a-z]/.test(password) && /[A-Z]/.test(password);
    this.pw_special = /[!@\$%\^&\*\+#]/.test(password);
    this.pw_number = /[0-9]/.test(password);

    if (repeat.length > 0) {
      this.passwordMismatch = password !== repeat;
    } else {
      this.passwordMismatch = false;
    }
  }

  private isPasswordValid(): boolean {
    return this.pw_length && this.pw_case && this.pw_special && this.pw_number;
  }

  private validateStepReset(passwordRepeat: string): boolean {
    this.fieldErrors = {};
    let valid = true;

    if (!this.oldPassword) {
      this.fieldErrors['oldPassword'] = 'Ein altes Passwort wird benötigt.';
      valid = false;
    } else if (!this.isPasswordValid()) {
      this.fieldErrors['oldPassword'] = 'Passwort erfüllt nicht alle Anforderungen.';
      valid = false;
    }

    if (!this.newPassword) {
      this.fieldErrors['newPassword'] = 'Ein neues Passwort ist erforderlich.';
      valid = false;
    } else if (!this.isPasswordValid()) {
      this.fieldErrors['newPassword'] = 'Passwort erfüllt nicht alle Anforderungen.';
      valid = false;
    }

    if (this.newPassword !== passwordRepeat) {
      this.passwordMismatch = true;
      valid = false;
    }
    return valid;
  }

  resetPassword() {
    console.log('resetPassword called');
    this.apiError = '';

    const isValid = this.validateStepReset(this.confirmPassword);

    if (!isValid) {
      console.log('not valid');
      return;
    }

    if (!this.authService.getUserId()) {
      this.apiError = 'Session abgelaufen.';
      console.log('Session abgelaufen.');
      return;
    }

    this.isLoadingReset = true;

    this.http
      .post<{
        res: boolean;
        message: string;
        errMessage: string;
      }>(`${API_BASE}/auth/change-password-request`, {
        id: Number(this.authService.getUserId()),
        oldPassword: this.oldPassword,
        newPassword: this.newPassword,
        confirmPassword: this.confirmPassword,
        adminId: 1,
      })
      .subscribe({
        next: (res) => {
          this.isLoadingReset = false;

          if (res.res) {
            this.currentStep = 2;
          } else {
            console.log('false going');
            console.log('error message', res.errMessage);

            this.apiError = res.errMessage || 'Error occurred';
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.isLoadingReset = false;
          this.apiError =
            err?.error?.message || 'Fehler beim Zurücksetzen. Bitte erneut versuchen.';
        },
      });
  }

  @ViewChild('countdown', { static: false }) private countdown!: CountdownComponent;

  config: CountdownConfig = {
    leftTime: environment.resendTimer,
    format: 'm:ss',
    demand: false,
  };

  isResendDisabled: boolean = true;

  handleEvent(event: CountdownEvent) {
    if (event.action === 'done') {
      this.isResendDisabled = false;
    }
  }

  /* ══════════════════════════════════════════════════════════════════
  STEP 2 — OTP INPUT HELPERS
  ══════════════════════════════════════════════════════════════════ */

  onOtpInput(event: Event, index: number) {
    const input = event.target as HTMLInputElement;
    const val = input.value.replace(/\D/g, '').slice(-1);
    input.value = val;
    this.collectOtp();
    this.otpError = '';

    if (val && index < 5) {
      const next = document.getElementById(`otp-${index + 1}`) as HTMLInputElement;
      if (next) next.focus();
    }
  }

  onOtpKeydown(event: KeyboardEvent, index: number) {
    if (event.key === 'Backspace') {
      const input = event.target as HTMLInputElement;
      if (!input.value && index > 0) {
        const prev = document.getElementById(`otp-${index - 1}`) as HTMLInputElement;
        if (prev) {
          prev.value = '';
          prev.focus();
        }
      }
      this.collectOtp();
    }
  }

  onOtpPaste(event: ClipboardEvent) {
    event.preventDefault();
    const pasted = event.clipboardData?.getData('text').replace(/\D/g, '').slice(0, 6) || '';
    pasted.split('').forEach((ch, i) => {
      const el = document.getElementById(`otp-${i}`) as HTMLInputElement;
      if (el) el.value = ch;
    });
    this.collectOtp();
    const last = document.getElementById(
      `otp-${Math.min(pasted.length - 1, 5)}`,
    ) as HTMLInputElement;
    if (last) last.focus();
  }

  private collectOtp() {
    let val = '';
    for (let i = 0; i < 6; i++) {
      const el = document.getElementById(`otp-${i}`) as HTMLInputElement;
      val += el ? el.value || '' : '';
    }
    this.otpValue = val;
  }

  verifyOtp() {
    this.collectOtp();
    if (this.otpValue.length < 6) {
      this.otpError = 'Bitte alle 6 Stellen eingeben.';
      return;
    }
    if (!this.authService.getUserId()) {
      this.otpError = 'Sitzung abgelaufen. Bitte neu registrieren.';
      return;
    }

    this.isLoading = true;
    this.otpError = '';

    this.http
      .post<{
        res: boolean;
        newOtp?: boolean;
        message: string;
      }>(`${API_BASE}/auth/verify-change-password`, {
        id: this.authService.getUserId(),
        otp: this.otpValue,
        adminId: 1,
      })
      .subscribe({
        next: (res) => {
          this.isLoading = false;
          if (res.res) {
            this.otpInvalid = false;

            this.nextStep(3);
            this.cdr.detectChanges();
          } else {
            this.otpError = 'Der eingegebene Code ist ungültig.';
            this.otpInvalid = true;
          }
          this.newOtp = !!res.newOtp;
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.isLoading = false;
          this.otpError =
            err?.error?.message || 'Code-Überprüfung fehlgeschlagen. Bitte erneut versuchen.';
        },
      });
  }

  resendOtp() {
    if (!this.authService.getUserId()) return;
    if (this.isResendDisabled) return;
    this.isResendDisabled = true;

    setTimeout(() => {
      this.countdown?.restart();
    });
    this.resendSuccess = false;
    this.otpError = '';

    this.http
      .post<{
        res: boolean;
        message: string;
      }>(`${API_BASE}/auth/resend-change-otp`, { id: this.authService.getUserId() })
      .subscribe({
        next: (res) => {
          this.resendSuccess = true;
          // Clear boxes
          for (let i = 0; i < 6; i++) {
            const el = document.getElementById(`otp-${i}`) as HTMLInputElement;
            if (el) el.value = '';
          }
          this.otpValue = '';
          this.cdr.detectChanges();
          setTimeout(() => (this.resendSuccess = false), 4000);
        },
        error: () => {
          this.otpError = 'Code konnte nicht gesendet werden. Bitte erneut versuchen.';
        },
      });
  }
  /* ──  Reset Password Section End ──*/
  /* ════════════════════════════════════════════════════════════════════════════════════════════════*/
}
