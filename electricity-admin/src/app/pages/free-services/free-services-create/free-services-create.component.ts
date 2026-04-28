import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { RouterModule, Router, ActivatedRoute } from "@angular/router";
import { ApiService } from "../../../shared/services/api.service";
import { AuthService } from "../../../shared/services/auth.service";
import { environment } from "../../../../environments/environment";

@Component({
  selector: "app-free-services-create",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: "./free-services-create.component.html",
  styleUrl: "./free-services-create.component.css",
})
export class FreeServicesCreateComponent {
  serviceId: string | null = null;
  isEditMode = false;

  title: string = "";
  description: string = "";
  isFreeService: boolean = true;
  isHighlighted: boolean = false;

  imageFile: File | null = null;
  imagePreview: string | null = null;

  hasRedirect: boolean = false;
  redirectUrl: string = "";

  isLoading: boolean = false;
  errorMessage: string = "";

  constructor(
    private api: ApiService,
    private router: Router,
    private authService: AuthService,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.serviceId = this.route.snapshot.paramMap.get("id");
    if (this.serviceId) {
      this.isEditMode = true;
      this.loadServiceData();
    }
  }

  loadServiceData(): void {
    const payload = {
      adminId: this.authService.getUserId(),
      id: parseInt(this.serviceId!),
    };

    this.api.post("admin/fetch-service-menu", payload).subscribe({
      next: (res: any) => {
        if (res?.res && res.data) {
          this.title = res.data.heading;
          this.description = res.data.subheading;
          this.isFreeService = res.data.type === 1;
          this.isHighlighted = res.data.highlight === 1;
          this.hasRedirect = res.data.isRedirect;
          this.redirectUrl = res.data.link || "";
          if (res.data.contentUrl) {
            this.imagePreview = environment.imageBaseUrl + res.data.contentUrl;
          }
        }
      },
      error: () => (this.errorMessage = "Fehler beim Laden der Daten"),
    });
  }

  // Logic to handle the switch change
  onTypeChange() {
    // If it's NOT a free service (meaning it's 'Other')
    if (!this.isFreeService) {
      // Highlight is not allowed for 'Other' services
      this.isHighlighted = false;
    } else {
      // If switching back to 'Free', clear the image
      this.imageFile = null;
      this.imagePreview = null;
    }
  }
  onFileChange(event: any) {
    const file = event.target.files[0];
    if (file) {
      if (file.size > 2 * 1024 * 1024) {
        this.errorMessage = "Bild muss kleiner als 2MB sein";
        return;
      }
      this.imageFile = file;
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  onSubmit() {
    const adminId = this.authService.getUserId();

    if (!this.title.trim()) {
      this.errorMessage = "Titel ist erforderlich";
      return;
    }

    if (this.hasRedirect && !this.redirectUrl?.trim()) {
      this.errorMessage =
        "Bitte geben Sie eine gültige Weiterleitungs-URL ein.";
      return;
    }

    // Adjust validation: Image required for 'Other' ONLY if not in Edit Mode or if new file selected
    if (!this.isFreeService && !this.imageFile && !this.isEditMode) {
      this.errorMessage = "Ein Bild ist für 'Andere Services' erforderlich";
      return;
    }

    this.isLoading = true;
    const payload: any = {
      adminId: adminId,
      heading: this.title,
      subheading: this.description,
      type: this.isFreeService ? 1 : 2,
      highlight: this.isHighlighted ? 1 : 0,
      isRedirect: this.hasRedirect,
      link: this.hasRedirect ? this.redirectUrl : null,
    };

    // Add ID if editing
    if (this.isEditMode) {
      payload.id = parseInt(this.serviceId!);
    }

    const formData = new FormData();
    formData.append("data", JSON.stringify(payload));
    if (this.imageFile) {
      formData.append("file", this.imageFile);
    }

    this.api.post("admin/add-service-menu", formData).subscribe({
      next: (res: any) => {
        this.isLoading = false;
        if (res?.res) {
          alert(
            this.isEditMode
              ? "✅ Service aktualisiert!"
              : "✅ Service erstellt!",
          );
          this.router.navigate(["/services/free"]);
        } else {
          this.errorMessage = res?.errorMessage || "Fehler beim Speichern";
        }
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = "Fehler beim Speichern";
      },
    });
  }
}
