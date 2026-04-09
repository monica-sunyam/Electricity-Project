import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ApiService } from "../../../shared/services/api.service";
import { RouterModule } from "@angular/router";
import { AuthService } from "../../../shared/services/auth.service";
import { environment } from "../../../../environments/environment";

@Component({
  selector: "app-navigation-menu-list",
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: "./navigation-menu-list.component.html",
})
export class NavigationMenuListComponent implements OnInit {
  readonly imgBase = environment.imageBaseUrl;

  menus: any[] = [];
  isLoading = false;
  errorMessage = "";

  constructor(
    private api: ApiService,
    private authService: AuthService,
  ) {}

  ngOnInit() {
    this.fetchMenus();
  }

  fetchMenus() {
    const adminId = this.authService.getUserId();

    this.isLoading = true;
    this.errorMessage = "";

    const payload = {
      adminId: adminId,
      type: 1,
    };

    this.api.post("admin/get-all-menu", payload).subscribe({
      next: (res) => {
        this.isLoading = false;

        if (res?.res) {
          this.menus = res.data || [];
        } else {
          this.errorMessage = res?.errorMessage || "Failed to load menus";
        }
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = "Something went wrong";
      },
    });
  }

  onDelete(id: string) {
    if (confirm("Möchten Sie dieses Menü wirklich löschen?")) {
      const payload = {
        adminId: this.authService.getUserId(),
        id: id,
      };

      this.api.post("admin/delete-menu", payload).subscribe({
        next: (res) => {
          if (res?.res) {
            this.menus = this.menus.filter((m) => m.id !== id);
          } else {
            alert(res?.errorMessage || "Löschen fehlgeschlagen");
          }
        },
        error: () => alert("Ein Fehler ist aufgetreten"),
      });
    }
  }
}
