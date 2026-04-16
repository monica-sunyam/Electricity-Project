import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { ContactPerson } from '../../layout/contact-person/contact-person';
import { NeedSupport } from '../../layout/need-support/need-support';
import { AuthService } from '../../services/auth.service';
import { FormsModule } from '@angular/forms';
const API_BASE = 'http://192.168.0.155:8080';
import { interval, Subscription } from 'rxjs';
import { NgZone } from '@angular/core';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ContactPerson, NeedSupport, FormsModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  /* ── Auth mode ─────────────────────────────────────────────────── */
  authMode: 'register' | 'login' = 'register';

  /* ── Step control ──────────────────────────────────────────────── */
  currentStep: number = 1;
  // currentStepLogin:
  //   | 'login'
  //   | 'loggedIn'
  //   | 'forgotPassword'
  //   | 'generatedCode'
  //   | 'verifyCode'
  //   | 'changePassword'
  //   | 'LoginFurther' = 'login';
  /* ── Main progress-bar step guard ──────────────────────────────── */
  // Tracks the highest main step the user is allowed to enter.
  // Starts at 1 (Account). Advances to 2 only after redirectToAccount()
  // succeeds. Persisted in AuthService so other pages can read it too.
  // get maxReachedMainStep(): number {
  //   return this.authService.getMaxReachedStep();
  // }

  // isMainStepAccessible(step: number): boolean {
  //   return step <= this.maxReachedMainStep;
  // }

  /* ── Loading / error state ─────────────────────────────────────── */
  isLoading: boolean = false;
  apiError: string = '';
  signupError: string = '';
  loginError: string = '';
  otpError: string = '';
  resendSuccess: boolean = false;
  email: string = '';
  existingEmail: string = '';
  password: string = '';
  isLoadingReset: boolean = false;
  isLoadingForgot: boolean = false;
  repeatPassword: string = '';

  /* ── Registered customer id (returned from signup) ─────────────── */
  registeredCustomerId: number | null = null;

  /* ── Form data ─────────────────────────────────────────────────── */
  formData = {
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    salutation: '',
    title: '',
    companyName: '',
    mobileNumberLocal: '', // digits only, prefix +49 added on submit
  };

  /* ── Field-level validation errors ─────────────────────────────── */
  fieldErrors: Record<string, string> = {};

  /* ── Customer type ─────────────────────────────────────────────── */
  customerType: 'private' | 'business' = 'private';

  /* ── Password visibility toggles ───────────────────────────────── */
  showPw: boolean = false;
  showRepPw: boolean = false;
  showLoginPw: boolean = false;

  /* ── Password validation flags ──────────────────────────────────── */
  pw_length: boolean = false;
  pw_case: boolean = false;
  pw_special: boolean = false;
  pw_number: boolean = false;
  pw_noEmail: boolean = false;
  passwordMismatch: boolean = false;

  /* ── OTP ────────────────────────────────────────────────────────── */
  otpValue: string = ''; // assembled 6-digit string

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    private router: Router,
    private zone: NgZone,
  ) {}

  /* ══════════════════════════════════════════════════════════════════
  AUTH MODE
  ══════════════════════════════════════════════════════════════════ */
  isLoggedIn: boolean = false;

  setAuthMode(mode: 'register' | 'login') {
    this.authMode = mode;
    this.apiError = '';
    this.loginError = '';
    this.currentStep = 1;

    if (this.isLoggedIn && this.authMode == 'login') {
      this.currentStep = 5;
    } else {
      this.currentStep = 1;
    }
  }

  setLoginMode(mode: 'register' | 'login') {
    this.authMode = mode;
    this.apiError = '';
    this.loginError = '';
    this.currentStep = 1;
  }

  selectedOption: 'same' | 'different' | null = null;
  backCheck() {
    if (this.isLoggedIn && this.authMode == 'login') {
      this.currentStep = 5;
    } else {
      this.currentStep = 1;
    }
  }

  handleContinue() {
    if (this.selectedOption === 'same') {
      this.router.navigate([this.mainStepRoutes[2]]);
    } else if (this.selectedOption === 'different') {
      this.currentStep = 1;
    } else {
      console.log('Please select an option');
    }
    this.cdr.detectChanges();
  }

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isLoggedIn();

    if (this.isLoggedIn) {
      // user logged in
      const customerId = this.authService.getUserId();
      this.existingEmail = this.authService.getUserEmailId() ?? '';
      this.email = this.existingEmail;
      console.log('Logged in user:', customerId);
    } else {
      // guest / temp user
      const tempId = this.authService.getTempUid();
      console.log('Guest user:', tempId);
    }
  }
  /* ══════════════════════════════════════════════════════════════════
  LOGIN
  ══════════════════════════════════════════════════════════════════ */

  doLogin(email: string, password: string) {
    this.loginError = '';

    if (!email || !password) {
      this.loginError = 'Bitte E-Mail und Passwort eingeben.';
      return;
    }

    this.isLoading = true;

    this.http
      .post<{
        res: boolean;
        message: string;
        data: { id: number; firstName: string; lastName: string; email: string };
      }>(`${API_BASE}/auth/login`, { email, password })
      .subscribe({
        next: (res) => {
          this.isLoading = false;

          if (res.res && res.data) {
            /* 🔥 Store user WITHOUT token */
            this.authService.login({
              user_id: res.data.id.toString(),
              email: res.data.email,
              full_name: `${res.data.firstName} ${res.data.lastName}`,
              token: undefined,
            });

            console.log('Login successful');
          } else {
            this.loginError = res.message || 'Anmeldung fehlgeschlagen.';
          }
        },
        error: (err) => {
          this.isLoading = false;
          this.loginError =
            err?.error?.message || 'Anmeldung fehlgeschlagen. Bitte erneut versuchen.';
        },
      });
  }

  /* ══════════════════════════════════════════════════════════════════
  MAIN STEP NAVIGATION (progress bar clicks)
  ══════════════════════════════════════════════════════════════════ */

  /** Maps outer progress-bar step numbers to their routes.
   *  Adjust the route paths to match your actual Angular routing config. */
  private readonly mainStepRoutes: Record<number, string> = {
    1: '/electricity-comparision/register', // Account (adjust if different)
    2: '/electricity-comparision/delivery-address',
    3: '/electricity-comparision/connection-data', // replace with actual path
    4: '/electricity-comparision/payment-method', // replace with actual path
    5: '/electricity-comparision/checkout', // replace with actual path
  };

  navigateToMainStep(step: number) {
    // if (!this.isMainStepAccessible(step)) {
    //   return; // silently block — step not yet unlocked
    // }
    const route = this.mainStepRoutes[step];
    if (route) {
      this.router.navigate([route]);
    }
  }

  /* ══════════════════════════════════════════════════════════════════
  STEP NAVIGATION
  ══════════════════════════════════════════════════════════════════ */

  goToStep(step: number) {
    this.currentStep = step;
    this.apiError = '';
    this.otpError = '';
    this.isLoading = false;

    // if (step === 7) {
    //   this.startResendTimer();
    // }
  }

  /* ══════════════════════════════════════════════════════════════════
  CUSTOMER TYPE
  ══════════════════════════════════════════════════════════════════ */

  setCustomerType(type: 'private' | 'business') {
    this.customerType = type;
    if (type === 'private') {
      this.formData.companyName = '';
      delete this.fieldErrors['companyName'];
    }
  }

  /* ══════════════════════════════════════════════════════════════════
  STEP 1 — FORM VALIDATION
  ══════════════════════════════════════════════════════════════════ */

  validatePassword(password: string, email: string, repeat: string) {
    // Update form data
    this.formData.password = password;

    // Criteria validation
    this.pw_length = password.length >= 8 && password.length <= 50;
    this.pw_case = /[a-z]/.test(password) && /[A-Z]/.test(password);
    this.pw_special = /[!@\$%\^&\*\+#]/.test(password);
    this.pw_number = /[0-9]/.test(password);
    this.pw_noEmail = email ? !password.toLowerCase().includes(email.toLowerCase()) : true;

    // Mismatch logic: Only show error if repeat field is not empty
    if (repeat.length > 0) {
      this.passwordMismatch = password !== repeat;
    } else {
      this.passwordMismatch = false;
    }
  }

  private isPasswordValid(): boolean {
    return this.pw_length && this.pw_case && this.pw_special && this.pw_number && this.pw_noEmail;
  }

  private validateStep1(passwordRepeat: string): boolean {
    this.fieldErrors = {};
    let valid = true;

    if (!this.formData.email) {
      this.fieldErrors['email'] = 'E-Mail-Adresse ist erforderlich.';
      valid = false;
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.formData.email)) {
      this.fieldErrors['email'] = 'Bitte geben Sie eine gültige E-Mail-Adresse ein.';
      valid = false;
    }

    if (!this.formData.password) {
      this.fieldErrors['password'] = 'Passwort ist erforderlich.';
      valid = false;
    } else if (!this.isPasswordValid()) {
      this.fieldErrors['password'] = 'Passwort erfüllt nicht alle Anforderungen.';
      valid = false;
    }

    if (this.formData.password !== passwordRepeat) {
      this.passwordMismatch = true;
      valid = false;
    }

    if (!this.formData.salutation) {
      this.fieldErrors['salutation'] = 'Bitte Anrede auswählen.';
      valid = false;
    }

    if (!this.formData.firstName.trim()) {
      this.fieldErrors['firstName'] = 'Vorname ist erforderlich.';
      valid = false;
    }

    if (!this.formData.lastName.trim()) {
      this.fieldErrors['lastName'] = 'Nachname ist erforderlich.';
      valid = false;
    }

    if (this.customerType === 'business' && !this.formData.companyName.trim()) {
      this.fieldErrors['companyName'] = 'Unternehmensname ist erforderlich.';
      valid = false;
    }

    if (!this.formData.mobileNumberLocal.trim()) {
      this.fieldErrors['mobileNumber'] = 'Handynummer ist erforderlich.';
      valid = false;
    }

    return valid;
  }

  /* ══════════════════════════════════════════════════════════════════
  STEP 1 — SUBMIT (SIGNUP API)
  ══════════════════════════════════════════════════════════════════ */

  submitRegistration(passwordRepeat: string) {
    this.apiError = '';

    if (!this.validateStep1(passwordRepeat)) return;

    const payload = {
      firstName: this.formData.firstName.trim(),
      lastName: this.formData.lastName.trim(),
      email: this.formData.email.trim(),
      password: this.formData.password,
      userType: this.customerType === 'business' ? 'BUSINESS' : 'PRIVATE',
      title: this.formData.title || '',
      salutation: this.formData.salutation,
      companyName: this.formData.companyName.trim(),
      mobileNumber: '+49' + this.formData.mobileNumberLocal.replace(/\s/g, ''),
    };

    this.isLoading = true;
    this.http
      .post<{
        res: boolean;
        data?: { id: number; firstName: string; lastName: string; email: string };
        page?: string;
        message?: string;
      }>(`${API_BASE}/auth/signup`, payload)
      .subscribe({
        next: (res) => {
          this.isLoading = false;
          if (res.res && res.data) {
            /* Store in AuthService */
            this.authService.setTempUid(res.data.id.toString());

            if (res.page === 'login') {
              this.signupError = 'E-Mail bereits registriert. Bitte einloggen.';
              console.log(this.signupError);
              // setTimeout(() => {
              this.setAuthMode('login');
              // this.currentStep = 5;
              // }, 2000);

              // return;
            } else {
              this.currentStep = 2;
              this.apiError = '';
            }
            this.cdr.detectChanges();
            console.log('UI update triggered for step:', this.currentStep);
          }
        },
        error: (err) => {
          this.isLoading = false;
          this.apiError =
            err?.error?.message || 'Ein Fehler ist aufgetreten. Bitte erneut versuchen.';
        },
      });
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

  /* ══════════════════════════════════════════════════════════════════
  STEP 2 — RESEND OTP
  ══════════════════════════════════════════════════════════════════ */

  resendOtp() {
    if (!this.authService.getTempUid()) return;
    this.resendSuccess = false;
    this.otpError = '';

    this.http
      .post<{
        res: boolean;
        message: string;
      }>(`${API_BASE}/auth/resend-otp`, { id: this.authService.getTempUid() })
      .subscribe({
        next: (res) => {
          this.resendSuccess = true;
          // Clear boxes
          for (let i = 0; i < 6; i++) {
            const el = document.getElementById(`otp-${i}`) as HTMLInputElement;
            if (el) el.value = '';
          }
          this.otpValue = '';
          setTimeout(() => (this.resendSuccess = false), 4000);
        },
        error: () => {
          this.otpError = 'Code konnte nicht gesendet werden. Bitte erneut versuchen.';
        },
      });
  }

  /* ══════════════════════════════════════════════════════════════════
  STEP 2 — VERIFY OTP
  ══════════════════════════════════════════════════════════════════ */
  otpInvalid = false;
  verifyOtp() {
    this.collectOtp();
    if (this.otpValue.length < 6) {
      this.otpError = 'Bitte alle 6 Stellen eingeben.';
      return;
    }
    if (!this.authService.getTempUid()) {
      this.otpError = 'Sitzung abgelaufen. Bitte neu registrieren.';
      return;
    }

    this.isLoading = true;
    this.otpError = '';

    this.http
      .post<{
        res: boolean;
        message: string;
      }>(`${API_BASE}/auth/verify-otp`, { id: this.authService.getTempUid(), otp: this.otpValue })
      .subscribe({
        // inside subscribe next block for verify-otp
        next: (res) => {
          this.isLoading = false;
          if (res.res) {
            this.currentStep = 3;
            this.otpInvalid = false;

            this.authService.finalizeUser(this.authService.getTempUid()!);

            this.cdr.detectChanges();
            console.log('Step changed to 3. UI should update now.');
          } else {
            this.otpError = 'Der eingegebene Code ist ungültig.';
            this.otpInvalid = true;
            this.cdr.detectChanges();
          }
        },
        error: (err) => {
          this.isLoading = false;
          this.otpError =
            err?.error?.message || 'Code-Überprüfung fehlgeschlagen. Bitte erneut versuchen.';
        },
      });
  }

  /* ══════════════════════════════════════════════════════════════════
  STEP 3 — COMPLETE REGISTRATION (mark-terms)
  ══════════════════════════════════════════════════════════════════ */

  completRegistration() {
    const userId = this.authService.getUserId();

    if (!userId) {
      this.apiError = 'Sitzung abgelaufen. Bitte neu registrieren.';
      return;
    }

    this.isLoading = true;
    this.apiError = '';

    this.http.post<any>(`${API_BASE}/auth/mark-terms`, { id: userId }).subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res.res) {
          this.currentStep = 4;
          console.log('Registration fully completed');

          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.apiError =
          err?.error?.message || 'Ein Fehler ist aufgetreten. Bitte erneut versuchen.';
      },
    });
  }

  /* ══════════════════════════════════════════════════════════════════
  STEP 4 — DIREKT LOGIN (mark-terms)
  ══════════════════════════════════════════════════════════════════ */

  redirectToAccount() {
    const userId = this.authService.getUserId();

    if (!userId) {
      this.apiError = 'Sitzung abgelaufen. Bitte neu registrieren.';
      return;
    }

    this.isLoading = true;
    this.apiError = '';

    this.http.post<any>(`${API_BASE}/auth/register-login`, { id: userId }).subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res.res) {
          if (res.res && res.data) {
            /* 🔥 Store user WITHOUT token */
            this.authService.login({
              user_id: res.data.id.toString(),
              email: res.data.email,
              full_name: `${res.data.firstName} ${res.data.lastName}`,
              token: undefined,
            });

            this.router.navigate([this.mainStepRoutes[2]]);
            console.log('Login successful');
          } else {
            this.loginError = res.message || 'Anmeldung fehlgeschlagen.';
          }
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.apiError = err?.error?.message || 'Anmeldung fehlgeschlagen. Bitte erneut versuchen.';
      },
    });
  }

  loginUser() {
    this.loginError = '';

    const payload = {
      email: this.email,
      password: this.password,
    };

    if (!this.email || !this.password) {
      this.loginError = 'Bitte E-Mail und Passwort eingeben.';
      return;
    }

    this.isLoading = true;

    this.http
      .post<{
        res: boolean;
        message: string;
        data: { id: number; firstName: string; lastName: string; email: string };
      }>(`${API_BASE}/auth/login`, payload)
      .subscribe({
        next: (res) => {
          this.isLoading = false;

          if (res.res && res.data) {
            // Store user
            this.authService.login({
              user_id: res.data.id.toString(),
              email: res.data.email,
              full_name: `${res.data.firstName} ${res.data.lastName}`,
              token: undefined,
            });

            console.log('Login successful');

            // Redirect
            this.router.navigate([this.mainStepRoutes[2]]);
          } else {
            this.loginError = res.message || 'E-Mail oder Passwort ist falsch.';
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.isLoading = false;
          this.loginError =
            err?.error?.message || 'E-Mail oder Passwort ist falsch. Bitte erneut versuchen.';
        },
      });
  }
  newOtp = false;
  sendForgotPasswordOtp() {
    this.apiError = '';

    if (!this.email) {
      this.apiError = 'Bitte geben Sie Ihre E-Mail-Adresse ein.';
      return;
    }

    this.isLoadingForgot = true;
    this.cdr.detectChanges();
    console.log('loading value', this.isLoadingForgot);
    // this.goToStep(7);
    this.http
      .post<{
        res: boolean;
        message: string;
        newOtp?: boolean;
        data?: { id: number };
      }>(`${API_BASE}/auth/forget-password`, { email: this.email })
      .subscribe({
        next: (res) => {
          // this.isLoadingForgot = false;
          console.log('loading value1', this.isLoadingForgot);
          if (res.res) {
            //  store temp user id for OTP verification
            if (res.data?.id) {
              this.authService.setTempUid(res.data.id.toString());
            }

            this.newOtp = !!res.newOtp;
            //  move to OTP screen
            this.goToStep(7);
            this.cdr.detectChanges();
            console.log('loading value2', this.isLoadingForgot);
          } else {
            this.apiError = res.message || 'Diese E-Mail-Adresse ist nicht registriert.';
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.isLoadingForgot = false;
          this.apiError =
            err?.error?.message ||
            'Diese E-Mail-Adresse ist nicht registriert. Bitte erneut versuchen.';
        },
      });
  }

  /* ══════════════════════════════════════════════════════════════════
  RESEND OTP Forgot
  ══════════════════════════════════════════════════════════════════ */
  resendTimer: number = 60;
  isResendDisabled: boolean = true;
  private timerSub!: Subscription;

  startResendTimer() {
    if (this.timerSub) {
      this.timerSub.unsubscribe();
    }

    this.resendTimer = 60;
    this.isResendDisabled = true;

    this.zone.runOutsideAngular(() => {
      this.timerSub = interval(1000).subscribe(() => {
        this.zone.run(() => {
          if (this.resendTimer > 0) {
            this.resendTimer--;
          } else {
            this.isResendDisabled = false;
            this.timerSub.unsubscribe();
          }
        });
      });
    });
  }

  resendOtpForgot() {
    if (!this.authService.getTempUid()) return;
    // if (this.isResendDisabled) return;

    this.resendSuccess = false;
    this.otpError = '';

    this.http
      .post<{
        res: boolean;
        message: string;
      }>(`${API_BASE}/auth/resend-forget-otp`, { id: this.authService.getTempUid() })
      .subscribe({
        next: (res) => {
          this.resendSuccess = true;
          // Clear boxes
          for (let i = 0; i < 6; i++) {
            const el = document.getElementById(`otp-${i}`) as HTMLInputElement;
            if (el) el.value = '';
          }
          this.otpValue = '';
          setTimeout(() => (this.resendSuccess = false), 4000);
          this.startResendTimer();
        },
        error: () => {
          this.otpError = 'Code konnte nicht gesendet werden. Bitte erneut versuchen.';
        },
      });
  }

  formatTime(seconds: number): string {
    const min = Math.floor(seconds / 60);
    const sec = seconds % 60;

    return `${this.pad(min)}:${this.pad(sec)}`;
  }

  pad(value: number): string {
    return value < 10 ? '0' + value : value.toString();
  }
  /* ══════════════════════════════════════════════════════════════════
  VERIFY OTP Forgot
  ══════════════════════════════════════════════════════════════════ */

  verifyOtpForgot() {
    this.collectOtp();
    if (this.otpValue.length < 6) {
      this.otpError = 'Bitte alle 6 Stellen eingeben.';
      return;
    }
    if (!this.authService.getTempUid()) {
      this.otpError = 'Sitzung abgelaufen. Bitte neu registrieren.';
      return;
    }

    this.isLoading = true;
    this.otpError = '';

    this.http
      .post<{
        res: boolean;
        message: string;
      }>(`${API_BASE}/auth/verify-otp`, { id: this.authService.getTempUid(), otp: this.otpValue })
      .subscribe({
        // inside subscribe next block for verify-otp
        next: (res) => {
          this.isLoading = false;
          if (res.res) {
            this.otpInvalid = false;

            // this.authService.finalizeUser(this.authService.getTempUid()!);

            this.goToStep(9);
            this.cdr.detectChanges();
          } else {
            this.otpError = 'Der eingegebene Code ist ungültig.';
            this.otpInvalid = true;
            // this.goToStep(8);
            this.cdr.detectChanges();
          }
        },
        error: (err) => {
          this.isLoading = false;
          this.otpError =
            err?.error?.message || 'Code-Überprüfung fehlgeschlagen. Bitte erneut versuchen.';
        },
      });
  }

  resetPassword() {
    console.log('resetPassword called');
    this.apiError = '';

    //  run validation first
    const isValid = this.validateStepReset(this.repeatPassword);

    if (!isValid) {
      console.log('not valid');
      return; // fieldErrors will be shown in UI
    }

    if (!this.authService.getTempUid()) {
      this.apiError = 'Session abgelaufen.';
      console.log('Session abgelaufen.');
      return;
    }

    this.isLoadingReset = true;

    this.http
      .post<{
        res: boolean;
        message: string;
      }>(`${API_BASE}/auth/reset-password`, {
        id: Number(this.authService.getTempUid()),
        password: this.password,
      })
      .subscribe({
        next: (res) => {
          this.isLoadingReset = false;

          if (res.res) {
            this.setLoginMode('login');
            this.cdr.detectChanges();
          } else {
            this.apiError = res.message || 'Fehler beim Zurücksetzen des Passworts.';
          }
        },
        error: (err) => {
          this.isLoadingReset = false;
          this.apiError =
            err?.error?.message || 'Fehler beim Zurücksetzen. Bitte erneut versuchen.';
        },
      });
  }

  validateForgotPassword(password: string, repeat: string, email?: string) {
    // Update form data
    this.password = password;

    // Criteria validation
    this.pw_length = password.length >= 8 && password.length <= 50;
    this.pw_case = /[a-z]/.test(password) && /[A-Z]/.test(password);
    this.pw_special = /[!@\$%\^&\*\+#]/.test(password);
    this.pw_number = /[0-9]/.test(password);
    this.pw_noEmail = email ? !password.toLowerCase().includes(email.toLowerCase()) : true;

    // Mismatch logic: Only show error if repeat field is not empty
    if (repeat.length > 0) {
      this.passwordMismatch = password !== repeat;
    } else {
      this.passwordMismatch = false;
    }
  }
  private validateStepReset(passwordRepeat: string): boolean {
    this.fieldErrors = {};
    let valid = true;

    if (!this.password) {
      this.fieldErrors['password'] = 'Passwort ist erforderlich.';
      valid = false;
    } else if (!this.isPasswordValid()) {
      this.fieldErrors['password'] = 'Passwort erfüllt nicht alle Anforderungen.';
      valid = false;
    }

    if (this.password !== passwordRepeat) {
      this.passwordMismatch = true;
      valid = false;
    }
    return valid;
  }
}
