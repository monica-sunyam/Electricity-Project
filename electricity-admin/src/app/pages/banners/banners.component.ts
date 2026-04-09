import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ApiService } from "../../shared/services/api.service";
import { AuthService } from "../../shared/services/auth.service";
import { environment } from "../../../environments/environment"; // Ensure this is imported

@Component({
  selector: "app-banners",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./banners.component.html",
  styleUrl: "./banners.component.css",
})
export class BannersComponent implements OnInit {
  readonly imgBase = environment.imageBaseUrl;

  // Banner State
  bannerId: number | null = null; // Store the ID for editing
  currentBannerUrl: string | null = null;

  // Upload State
  imageFile: File | null = null;
  imagePreview: string | null = null;

  isLoading = false;
  errorMessage = "";

  constructor(
    private api: ApiService,
    private authService: AuthService,
  ) {}

  ngOnInit() {
    this.fetchCurrentBanner();
  }

  fetchCurrentBanner() {
    // Payload for getting the specific banner type
    const payload = {
      adminId: this.authService.getUserId(),
      type: 3 // Matching the type used in onSubmit
    };

    // Using your get-all endpoint to find the existing banner
    this.api.post("admin/get-all-menu", payload).subscribe({
      next: (res: any) => {
        // If an array is returned, take the first item
        if (res.res && res.data && res.data.length > 0) {
          const banner = res.data[0];
          this.bannerId = banner.id;
          this.currentBannerUrl = this.imgBase + banner.contentUrl;
        }
      },
      error: (err) => console.error("Could not load existing banner", err),
    });
  }

  onFileChange(event: any) {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.type.startsWith("image/")) {
      this.errorMessage = "Nur Bilddateien erlaubt";
      return;
    }

    if (file.size > 2 * 1024 * 1024) {
      this.errorMessage = "Bild muss kleiner als 2MB sein";
      return;
    }

    this.errorMessage = "";
    this.imageFile = file;

    const reader = new FileReader();
    reader.onload = () => {
      this.imagePreview = reader.result as string;
    };
    reader.readAsDataURL(file);
  }

  onSubmit() {
    const adminId = this.authService.getUserId();

    if (!this.imageFile) {
      this.errorMessage = "Bitte wählen Sie ein neues Bild aus";
      return;
    }

    this.isLoading = true;
    this.errorMessage = "";

    const payload: any = {
      adminId: adminId,
      type: 3, 
    };

    // If bannerId exists, add it to the payload so the backend updates the existing record
    if (this.bannerId) {
      payload.id = this.bannerId;
    }

    const formData = new FormData();
    formData.append("file", this.imageFile);
    formData.append("data", JSON.stringify(payload));

    this.api.post("admin/add-menu", formData).subscribe({
      next: (res: any) => {
        this.isLoading = false;
        alert("✅ Banner erfolgreich aktualisiert");
        
        // Refresh the view
        this.fetchCurrentBanner();
        
        // Reset upload slot
        this.imageFile = null;
        this.imagePreview = null;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = "Fehler beim Hochladen des Banners";
      },
    });
  }
}