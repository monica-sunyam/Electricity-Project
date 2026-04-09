import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterModule } from "@angular/router";
import { ApiService } from "../../../shared/services/api.service";
import { AuthService } from "../../../shared/services/auth.service";
import { environment } from "../../../../environments/environment.development";

@Component({
  selector: "app-sidebar-menu-list",
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: "./sidebar-menu-list.component.html",
  styleUrl: "./sidebar-menu-list.component.css",
})
export class SidebarMenuListComponent implements OnInit {
  readonly imgBase = environment.imageBaseUrl;

  menus: any[] = [];
  isLoading = false;
  errorMessage = "";

  constructor(
    private api: ApiService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.fetchSidebarMenus();
  }

  fetchSidebarMenus(): void {
    const adminId = this.authService.getUserId();

    this.isLoading = true;
    this.errorMessage = "";

    const payload = {
      adminId: adminId,
      type: 2,
    };

    // Adjust the endpoint string to match your backend API
    this.api.post("admin/get-all-menu", payload).subscribe({
      next: (res: any) => {
        if (res.res && res.data) {
          this.menus = res.data;
        } else {
          this.menus = [];
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        // German error message for the UI
        this.errorMessage = "Fehler beim Laden der Menüs";
        console.error("Fetch error:", err);
      },
    });
  }

  onDelete(id: number): void {
    if (confirm("Möchten Sie dieses Sidebar-Menü wirklich löschen?")) {
      const payload = {
        adminId: this.authService.getUserId(),
        id: id,
      };

      // Using the common delete endpoint
      this.api.post("admin/delete-menu", payload).subscribe({
        next: (res: any) => {
          if (res?.res) {
            // Remove from local array to update UI immediately
            this.menus = this.menus.filter((m) => m.id !== id);
          } else {
            alert(res?.errorMessage || "Fehler beim Löschen");
          }
        },
        error: () => alert("Ein Fehler ist aufgetreten"),
      });
    }
  }
}
