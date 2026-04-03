import { Component } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { RouterModule } from "@angular/router";
import { InputFieldComponent } from "../../shared/components/form/input/input-field.component";
import { LabelComponent } from "../../shared/components/form/label/label.component";
import { ButtonComponent } from "../../shared/components/ui/button/button.component";
import { AuthService } from "../../shared/services/auth.service";
import { Router } from "@angular/router";

@Component({
  selector: "app-sign-in",
  standalone: true, // ✅ IMPORTANT
  imports: [
    RouterModule,
    LabelComponent,
    ButtonComponent,
    InputFieldComponent,
    FormsModule,
  ],
  templateUrl: "./sign-in.component.html",
})
export class SignInComponent {
  showPassword = false;

  email = "";
  password = "";
  errorMessage = "";
  isLoading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  onSignIn() {
    console.log(this.email, this.password);

    if (!this.email || !this.password) {
      console.warn("Email & Password required");
      return;
    }

    this.isLoading = true;

    const payload = {
      email: this.email,
      password: this.password,
    };

    this.authService.login(payload).subscribe({
      next: (res) => {
        // res.data contains the admin object from your API response
        this.authService.setLoggedIn(res.data);
        this.router.navigate(["/"]);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage =
          err?.error?.errMessage || "Anmeldung fehlgeschlagen";
      },
    });
  }
}
