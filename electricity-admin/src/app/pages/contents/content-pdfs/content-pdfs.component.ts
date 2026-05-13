import { CommonModule } from "@angular/common";
import { Component } from "@angular/core";
import { ApiService } from "../../../shared/services/api.service";
import { AuthService } from "../../../shared/services/auth.service";

@Component({
  selector: "app-content-pdfs",
  imports: [CommonModule],
  templateUrl: "./content-pdfs.component.html",
  styleUrl: "./content-pdfs.component.css",
})
export class ContentPDFsComponent {
  customers = [];
  fileSizeError: boolean = false;
  isLoading = false;
  errorMessage = "";
  hasMoreData = true;
  private readonly PAGE_LIMIT = 20;
  currentPage = 1;
  totalPage: number | null = null;
  isModalOpen = false;
  selectedFile: File | null = null;
  isUploading: boolean = false;
  selectedDoc: any = null;

  constructor(
    private api: ApiService,
    private authService: AuthService,
  ) {}

  nextPage(): void {
    if (this.currentPage < this.totalPage!) {
      // this.fetchCustomers(this.currentPage + 1);
    }
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      // this.fetchCustomers(this.currentPage - 1);
    }
  }

  openModal(doc?: any) {
    this.selectedDoc = doc || null;
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.selectedFile = null;
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];

    if (!file) return;

    const maxSize = 5 * 1024 * 1024; // 5MB

    if (file.size > maxSize) {
      this.selectedFile = null;
      this.fileSizeError = true;
      return;
    }

    this.fileSizeError = false;
    this.selectedFile = file;
  }

  submitPdf() {
    if (!this.selectedFile || this.fileSizeError) {
      return;
    }

    const payload: any = {
      adminId: 1,
      documentCategory: "Privacy",
    };

    // Add this only if replacing existing document
    if (this.selectedDoc?.id) {
      payload.adminDocId = this.selectedDoc.id;
    }

    const formData = new FormData();

    formData.append("data", JSON.stringify(payload));
    formData.append("file", this.selectedFile);

    this.isUploading = true;

    this.api.post("admin/add-doc", formData).subscribe({
      next: (res: any) => {
        console.log("Upload success:", res);

        this.isUploading = false;
        this.closeModal();

        this.selectedFile = null;
        this.fileSizeError = false;
      },
      error: (err) => {
        console.error("Upload failed:", err);
        this.isUploading = false;
      },
    });
  }
}
