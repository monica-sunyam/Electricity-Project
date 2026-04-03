import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ApiService } from "../../../shared/services/api.service";
import { RouterModule } from '@angular/router';


@Component({
  selector: "app-navigation-menu-list",
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: "./navigation-menu-list.component.html",
})
export class NavigationMenuListComponent implements OnInit {
  menus: any[] = [];
  isLoading = false;
  errorMessage = "";

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.fetchMenus();
  }

  fetchMenus() {
    this.isLoading = true;
    this.errorMessage = "";

    this.api.get("admin/navigation/list").subscribe({
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
}
