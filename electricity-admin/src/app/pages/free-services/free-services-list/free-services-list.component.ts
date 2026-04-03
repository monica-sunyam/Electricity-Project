import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterModule } from "@angular/router";
import { ApiService } from "../../../shared/services/api.service";

@Component({
  selector: "app-free-services-list",
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: "./free-services-list.component.html",
  styleUrl: "./free-services-list.component.css",
})
export class FreeServicesListComponent implements OnInit {
  services: any[] = [];
  isLoading = false;
  errorMessage = "";

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.fetchServices();
  }

  fetchServices(): void {
    this.isLoading = true;
    this.errorMessage = "";

    this.api.get("admin/free-services").subscribe({
      next: (res: any) => {
        if (res.res && res.data) {
          this.services = res.data;
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = "Fehler beim Laden der Services.";
        console.error("Fetch error:", err);
      },
    });
  }
}
