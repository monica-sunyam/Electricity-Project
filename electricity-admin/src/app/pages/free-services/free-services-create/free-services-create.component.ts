import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { RouterModule, Router } from "@angular/router";
import { ApiService } from "../../../shared/services/api.service";

@Component({
  selector: "app-free-services-create",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: "./free-services-create.component.html",
  styleUrl: "./free-services-create.component.css",
})
export class FreeServicesCreateComponent {
  serviceName: string = "";
  isLoading: boolean = false;
  errorMessage: string = "";

  constructor(
    private api: ApiService,
    private router: Router,
  ) {}

  onSubmit() {
    if (!this.serviceName.trim()) {
      this.errorMessage = "Bitte geben Sie einen Namen ein.";
      return;
    }

    this.isLoading = true;
    const payload = { name: this.serviceName };

    this.api.post("admin/free-services/create", payload).subscribe({
      next: (res: any) => {
        this.router.navigate(["/services/free"]);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = "Fehler beim Speichern des Services.";
      },
    });
  }
}
