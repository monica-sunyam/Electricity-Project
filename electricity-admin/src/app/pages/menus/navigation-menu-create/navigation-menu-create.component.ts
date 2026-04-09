import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ApiService } from "../../../shared/services/api.service";
import { RouterModule, ActivatedRoute, Router } from "@angular/router";
import { AuthService } from "../../../shared/services/auth.service";
import { environment } from "../../../../environments/environment";

@Component({
  selector: "app-navigation-menu-create",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: "./navigation-menu-create.component.html",
  styleUrl: "./navigation-menu-create.component.css",
})
export class NavigationMenuCreateComponent implements OnInit {
  menuId: string | null = null;
  isEditMode = false;
  
  title = "";
  subtitle = "";
  imageFile: File | null = null;
  imagePreview: string | null = null;

  isLoading = false;
  errorMessage = "";

  constructor(
    private api: ApiService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    // Check for ID in the route parameters
    this.menuId = this.route.snapshot.paramMap.get("id");
    if (this.menuId) {
      this.isEditMode = true;
      this.loadMenuData();
    }
  }

  loadMenuData() {
    const payload = {
      adminId: this.authService.getUserId(),
      id: parseInt(this.menuId!),
    };
    
    this.api.post("admin/fetch-menu", payload).subscribe({
      next: (res) => {
        if (res?.res) {
          this.title = res.data.heading;
          this.subtitle = res.data.subHeading;
          // Pre-fill preview with existing image URL
          this.imagePreview = environment.imageBaseUrl + res.data.contentUrl;
        }
      },
      error: () => (this.errorMessage = "Fehler beim Laden der Daten")
    });
  }

  /* ================= FILE HANDLING ================= */

  onFileChange(event: any) {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.type.startsWith("image/")) {
      this.errorMessage = "Nur Bilder sind erlaubt";
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

  /* ================= SUBMIT (Add & Edit) ================= */

  onSubmit() {
    const adminId = this.authService.getUserId();

    // Validation: Image is only mandatory for NEW menus
    if (!this.title || (!this.imageFile && !this.isEditMode)) {
      this.errorMessage = "Titel und Bild sind erforderlich";
      return;
    }

    this.isLoading = true;
    this.errorMessage = "";

    const payload: any = {
      adminId: adminId,
      heading: this.title,
      subHeading: this.subtitle || "",
      type: 1,
    };

    // If editing, include the ID so the backend knows to update instead of create
    if (this.isEditMode) {
      payload.id = parseInt(this.menuId!);
    }

    const formData = new FormData();
    // Only append file if a new one was selected
    if (this.imageFile) {
      formData.append("file", this.imageFile);
    }
    formData.append("data", JSON.stringify(payload));

    // Both use the same endpoint as per your requirement
    this.api.post("admin/add-menu", formData).subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res?.res) {
          alert(this.isEditMode ? "✅ Menü aktualisiert" : "✅ Menü erstellt");
          this.router.navigate(["/menus/navigation"]);
        } else {
          this.errorMessage = res?.errorMessage || "Fehler beim Speichern";
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = "Etwas ist schiefgelaufen";
      },
    });
  }
}