import { Component } from "@angular/core";
import { DropdownComponent } from "../../ui/dropdown/dropdown.component";
import { CommonModule } from "@angular/common";
import { Router } from "@angular/router";
import { RouterModule } from "@angular/router";
import { AuthService } from "../../../services/auth.service";

@Component({
  selector: "app-user-dropdown",
  standalone: true,
  templateUrl: "./user-dropdown.component.html",
  imports: [CommonModule, RouterModule, DropdownComponent],
})
export class UserDropdownComponent {
  isOpen = false;

  constructor(
    public authService: AuthService,
    private router: Router,
  ) {}

  toggleDropdown() {
    this.isOpen = !this.isOpen;
  }

  closeDropdown() {
    this.isOpen = false;
  }

  onLogout() {
    this.closeDropdown();

    this.authService.logoutApi().subscribe({
      next: () => {
        this.finishLogout();
      },
      error: () => {
        /* even if API fails, still logout locally */
        this.finishLogout();
      },
    });
  }

  private finishLogout() {
    this.authService.logout(); // clear local storage
    this.router.navigate(["/signin"], { replaceUrl: true });
  }
}
