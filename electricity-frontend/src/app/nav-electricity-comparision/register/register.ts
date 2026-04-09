import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ContactPerson } from '../../layout/contact-person/contact-person';
import { NeedSupport } from '../../layout/need-support/need-support';

const API_BASE = 'http://192.168.0.155:8080';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ContactPerson, NeedSupport],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  /* ── Auth mode ─────────────────────────────────────────────────── */
  authMode: 'register' | 'login' = 'register';

  /* ── Step control ──────────────────────────────────────────────── */
  currentStep: number = 1;

  /* ── Loading / error state ─────────────────────────────────────── */
  isLoading: boolean = false;
  apiError: string = '';
  loginError: string = '';
  otpError: string = '';
  resendSuccess: boolean = false;

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
  ) {}

  /* ══════════════════════════════════════════════════════════════════
     AUTH MODE
  ══════════════════════════════════════════════════════════════════ */

  setAuthMode(mode: 'register' | 'login') {
    this.authMode = mode;
    this.apiError = '';
    this.loginError = '';
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
      .post<{ res: boolean; message: string }>(`${API_BASE}/customer/login`, { email, password })
      .subscribe({
        next: (res) => {
          this.isLoading = false;
          if (res.res) {
            // Successful login — navigate or emit event as needed
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
     STEP NAVIGATION
  ══════════════════════════════════════════════════════════════════ */

  goToStep(step: number) {
    this.currentStep = step;
    this.apiError = '';
    this.otpError = '';
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
    if (!password) {
      this.pw_length = this.pw_case = this.pw_special = this.pw_number = this.pw_noEmail = false;
      this.passwordMismatch = false;
      return;
    }

    this.pw_length = password.length >= 8 && password.length <= 50;
    this.pw_case = /[a-z]/.test(password) && /[A-Z]/.test(password);
    this.pw_special = /[!@\$%\^&\*\+#]/.test(password);
    this.pw_number = /[0-9]/.test(password);

    if (email) {
      this.pw_noEmail = !password.toLowerCase().includes(email.toLowerCase());
    } else {
      this.pw_noEmail = true;
    }

    if (repeat) {
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
      }>(`${API_BASE}/customer/signup`, payload)
      .subscribe({
        next: (res) => {
          this.isLoading = false;
          if (res.res && res.data) {
            this.registeredCustomerId = res.data.id;
            this.currentStep = 2; // This updates the variable
            this.apiError = '';

            // FORCE THE UI TO SEE THE CHANGE
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
    if (!this.registeredCustomerId) return;
    this.resendSuccess = false;
    this.otpError = '';

    this.http
      .post<{
        res: boolean;
        message: string;
      }>(`${API_BASE}/customer/resend-otp`, { id: this.registeredCustomerId })
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

  verifyOtp() {
    this.collectOtp();
    if (this.otpValue.length < 6) {
      this.otpError = 'Bitte alle 6 Stellen eingeben.';
      return;
    }
    if (!this.registeredCustomerId) {
      this.otpError = 'Sitzung abgelaufen. Bitte neu registrieren.';
      return;
    }

    this.isLoading = true;
    this.otpError = '';

    this.http
      .post<{
        res: boolean;
        message: string;
      }>(`${API_BASE}/customer/verify-otp`, { id: this.registeredCustomerId, otp: this.otpValue })
      .subscribe({
        // inside subscribe next block for verify-otp
        next: (res) => {
          this.isLoading = false;
          if (res.res) {
            this.currentStep = 3; // Update the step to 3

            // FORCE THE UI TO SEE THE CHANGE
            this.cdr.detectChanges();
            console.log('Step changed to 3. UI should update now.');
          } else {
            this.otpError = 'Der eingegebene Code ist ungültig.';
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
    if (!this.registeredCustomerId) {
      this.apiError = 'Sitzung abgelaufen. Bitte neu registrieren.';
      return;
    }

    this.isLoading = true;
    this.apiError = '';

    this.http
      .post<any>(`${API_BASE}/customer/mark-terms`, { id: this.registeredCustomerId })
      .subscribe({
        next: (res) => {
          this.isLoading = false;
          if (res.res) {
            // Add logic here to move to Step 4 or the next page
            console.log('Registration fully completed');
            // Example: this.currentStep = 4;
            // this.cdr.detectChanges();
          }
        },
      });
  }
}
