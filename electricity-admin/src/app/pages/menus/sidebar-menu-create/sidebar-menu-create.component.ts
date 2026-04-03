import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ApiService } from "../../../shared/services/api.service";
import { RouterModule } from '@angular/router';

@Component({
  selector: "app-sidebar-menu-create",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: "./sidebar-menu-create.component.html",
  styleUrl: "./sidebar-menu-create.component.css",
})
export class SidebarMenuCreateComponent {
  title = "";
  imageFile: File | null = null;
  imagePreview: string | null = null;

  isLoading = false;
  errorMessage = "";

  constructor(private api: ApiService) {}

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

  onSubmit() {
    if (!this.title || !this.imageFile) {
      this.errorMessage = "Title and Image are required";
      return;
    }

    this.isLoading = true;
    this.errorMessage = "";

    const payload = {
      title: this.title,
    };

    const formData = new FormData();
    formData.append("image", this.imageFile);
    formData.append("data", JSON.stringify(payload)); // 👈 JSON inside multipart

    this.api.post("admin/navigation/create", formData).subscribe({
      next: (res) => {
        this.isLoading = false;

        /* reset form */
        this.title = "";
        this.imageFile = null;
        this.imagePreview = null;

        alert("✅ Menu created successfully");
      },
      error: (err) => {
        this.isLoading = false;

        if (err?.error?.res === false) {
          this.errorMessage = err.error.errorMessage;
        } else {
          this.errorMessage = "Something went wrong";
        }
      },
    });
  }
}
