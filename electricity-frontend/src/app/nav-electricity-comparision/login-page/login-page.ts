import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { ContactPerson } from '../../layout/contact-person/contact-person';
import { NeedSupport } from '../../layout/need-support/need-support';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
const API_BASE = 'http://localhost:8080';

@Component({
  selector: 'app-login-page',
  imports: [CommonModule, ContactPerson, NeedSupport, FormsModule],
  templateUrl: './login-page.html',
  styleUrl: './login-page.css',
})
export class LoginPage {
  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private http: HttpClient,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
  ) {}

  currentStep:
    | 'login'
    | 'loggedIn'
    | 'forgotPassword'
    | 'generatedCode'
    | 'verifyCode'
    | 'changePassword'
    | 'LoginFurther' = 'login';

  goToStep(
    step:
      | 'login'
      | 'loggedIn'
      | 'forgotPassword'
      | 'generatedCode'
      | 'verifyCode'
      | 'changePassword'
      | 'LoginFurther',
  ) {
    this.currentStep = step;
    this.apiError = '';
    this.otpError = '';
    this.isLoading = false;
  }

  email: string = '';
  password: string = '';
  isLoading: boolean = false;
  isLoadingReset: boolean = false;
  isLoadingForgot: boolean = false;
  apiError: string = '';
  loginError: string = '';
  otpError: string = '';
  resendSuccess: boolean = false;
  otpValue: string = ''; // assembled 6-digit string
  /* ── Field-level validation errors ─────────────────────────────── */
  fieldErrors: Record<string, string> = {};

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

  /* ══════════════════════════════════════════════════════════════════
  MAIN STEP NAVIGATION (progress bar clicks)
  ══════════════════════════════════════════════════════════════════ */

  /** Maps outer progress-bar step numbers to their routes.
   *  Adjust the route paths to match your actual Angular routing config. */
  private readonly mainStepRoutes: Record<number, string> = {
    1: '/electricity-comparision/register', // Account (adjust if different)
    2: '/electricity-comparision/delivery-address',
    3: '/electricity-comparision/anschlussdaten', // replace with actual path
    4: '/electricity-comparision/zahlungsart', // replace with actual path
    5: '/electricity-comparision/abschluss', // replace with actual path
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

  openPage() {
    this.router.navigate(['/electricity-comparision/delivery-address'], {});
  }

  setAuthMode(
    mode:
      | 'login'
      | 'loggedIn'
      | 'forgotPassword'
      | 'generatedCode'
      | 'verifyCode'
      | 'changePassword'
      | 'LoginFurther',
  ) {
    this.currentStep = mode;
    this.apiError = '';
    this.loginError = '';
  }
  repeatPassword: string = '';
  validatePassword(password: string, repeat: string, email?: string) {
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

  private isPasswordValid(): boolean {
    return this.pw_length && this.pw_case && this.pw_special && this.pw_number && this.pw_noEmail;
  }

  private validateStep1(passwordRepeat: string): boolean {
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
            this.loginError = res.message || 'Anmeldung fehlgeschlagen.';
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.isLoading = false;
          this.loginError =
            err?.error?.message || 'Anmeldung fehlgeschlagen. Bitte erneut versuchen.';
        },
      });
  }

  sendForgotPasswordOtp() {
    this.apiError = '';

    if (!this.email) {
      this.apiError = 'Bitte geben Sie Ihre E-Mail-Adresse ein.';
      return;
    }

    this.isLoadingForgot = true;
    this.cdr.detectChanges();

    this.http
      .post<{
        res: boolean;
        message: string;
        data?: { id: number };
      }>(`${API_BASE}/auth/forget-password`, { email: this.email })
      .subscribe({
        next: (res) => {
          this.isLoadingForgot = false;

          if (res.res) {
            //  store temp user id for OTP verification
            if (res.data?.id) {
              this.authService.setTempUid(res.data.id.toString());
            }

            //  move to OTP screen
            this.goToStep('generatedCode');
            this.cdr.detectChanges();
          } else {
            this.apiError = res.message || 'Fehler beim Senden des Codes.';
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.isLoadingForgot = false;
          this.apiError =
            err?.error?.message || 'Fehler beim Senden des Codes. Bitte erneut versuchen.';
        },
      });
  }
  /* ══════════════════════════════════════════════════════════════════
  OTP INPUT HELPERS
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
  RESEND OTP
  ══════════════════════════════════════════════════════════════════ */

  resendOtp() {
    if (!this.authService.getTempUid()) return;
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
        },
        error: () => {
          this.otpError = 'Code konnte nicht gesendet werden. Bitte erneut versuchen.';
        },
      });
  }

  /* ══════════════════════════════════════════════════════════════════
  VERIFY OTP
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
            this.otpInvalid = false;

            // this.authService.finalizeUser(this.authService.getTempUid()!);

            this.goToStep('changePassword');
            this.cdr.detectChanges();
          } else {
            this.otpError = 'Der eingegebene Code ist ungültig.';
            this.otpInvalid = true;
            this.goToStep('verifyCode');
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
    const isValid = this.validateStep1(this.repeatPassword);

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
        id: this.authService.getTempUid(),
        password: this.password,
      })
      .subscribe({
        next: (res) => {
          this.isLoadingReset = false;

          if (res.res) {
            this.goToStep('login');
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
}
