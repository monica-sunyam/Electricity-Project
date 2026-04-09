import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ApiService } from "../../../shared/services/api.service";
import { RouterModule } from "@angular/router";
import { CKEditorModule } from "@ckeditor/ckeditor5-angular";
import ClassicEditor from "@ckeditor/ckeditor5-build-classic";
import { AuthService } from "../../../shared/services/auth.service";
import { ActivatedRoute, Router } from "@angular/router";
import { environment } from "../../../../environments/environment.development";

@Component({
  selector: "app-sidebar-menu-create",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, CKEditorModule],
  templateUrl: "./sidebar-menu-create.component.html",
  styleUrl: "./sidebar-menu-create.component.css",
})
export class SidebarMenuCreateComponent implements OnInit {
  public Editor: any = ClassicEditor;
  menuId: string | null = null;
  isEditMode = false;

  title = "";
  imageFile: File | null = null;
  imagePreview: string | null = null;
  savings = "";
  popupContent = "";

  isLoading = false;
  errorMessage = "";

  constructor(
    private api: ApiService,
    private authService: AuthService,
    private route: ActivatedRoute, // Inject ActivatedRoute
    private router: Router, // Inject Router
  ) {}

  ngOnInit(): void {
    // Check if ID exists in URL
    this.menuId = this.route.snapshot.paramMap.get("id");
    if (this.menuId) {
      this.isEditMode = true;
      this.loadMenuData();
    }
  }

  loadMenuData(): void {
    const payload = {
      adminId: this.authService.getUserId(),
      id: parseInt(this.menuId!),
    };

    // Using the common fetch endpoint
    this.api.post("admin/fetch-menu", payload).subscribe({
      next: (res: any) => {
        if (res?.res && res.data) {
          // Use logical OR to catch different naming conventions from the API
          this.title = res.data.title || res.data.heading || "";
          this.savings = res.data.saving || "";
          this.popupContent = res.data.savingDetail || "";

          if (res.data.contentUrl) {
            this.imagePreview = environment.imageBaseUrl + res.data.contentUrl;
          }
        }
      },
    });
  }

  /* ================= FILE ================= */

  onFileChange(event: any) {
    const file = event.target.files[0];

    if (!file) return;

    /* 🔒 validation */
    if (!file.type.startsWith("image/")) {
      this.errorMessage = "Only image files allowed";
      return;
    }

    if (file.size > 2 * 1024 * 1024) {
      this.errorMessage = "Image must be less than 2MB";
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

  /* ================= SUBMIT ================= */

  /* ================= SUBMIT ================= */

  onSubmit() {
    const adminId = this.authService.getUserId();

    // FIX: In Edit Mode, imageFile is optional because we keep the existing one
    if (!this.title || (!this.imageFile && !this.isEditMode) || !this.savings) {
      this.errorMessage = "Titel, Bild und Ersparnis sind erforderlich";
      return;
    }

    this.isLoading = true;
    this.errorMessage = "";

    const payload: any = {
      adminId: adminId,
      heading: this.title, // Ensure the backend expects 'heading' or 'title'
      saving: this.savings,
      savingDetail: this.popupContent,
      type: 2,
    };

    // IMPORTANT: Add the ID to the payload if we are editing
    if (this.isEditMode) {
      payload.id = parseInt(this.menuId!);
    }

    const formData = new FormData();

    // Only append the file if the user actually picked a new one
    if (this.imageFile) {
      formData.append("file", this.imageFile);
    }

    formData.append("data", JSON.stringify(payload));

    this.api.post("admin/add-menu", formData).subscribe({
      next: (res: any) => {
        this.isLoading = false;

        if (res?.res) {
          alert(
            this.isEditMode
              ? "✅ Menü erfolgreich aktualisiert"
              : "✅ Menü erfolgreich erstellt",
          );
          this.router.navigate(["/menus/sidebar"]);
        } else {
          this.errorMessage = res?.errorMessage || "Fehler beim Speichern";
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = "Etwas ist schiefgelaufen";
        console.error(err);
      },
    });
  }
}
