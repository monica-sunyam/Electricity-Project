import { CommonModule } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { ApiService } from "../../../shared/services/api.service";
import { AuthService } from "../../../shared/services/auth.service";
import { FormsModule } from "@angular/forms";

@Component({
  selector: "app-content-pdfs",
  imports: [CommonModule, FormsModule],
  templateUrl: "./content-pdfs.component.html",
  styleUrl: "./content-pdfs.component.css",
})
export class ContentPDFsComponent implements OnInit {
  customers: any[] = [];

  fileSizeError: boolean = false;
  isLoading = false;
  errorMessage = "";
  hasMoreData = true;

  private readonly PAGE_LIMIT = 5;

  currentPage = 1;
  totalPage: number | null = null;

  isModalOpen = false;
  selectedFile: File | null = null;
  isUploading: boolean = false;
  selectedDoc: any = null;

  documentCategory: string = "";

  constructor(
    private api: ApiService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.fetchDocuments();
  }

  /* ---------------- OPEN MODAL ---------------- */
  openModal(doc?: any) {
    // reset previous state first
    this.selectedFile = null;
    this.fileSizeError = false;

    this.selectedDoc = doc || null;

    // prefill category while editing
    this.documentCategory = doc?.documentCategory || "";

    this.isModalOpen = true;
  }

  /* ---------------- CLOSE MODAL ---------------- */
  closeModal(): void {
    this.isModalOpen = false;

    this.selectedFile = null;
    this.selectedDoc = null;
    this.documentCategory = "";

    this.fileSizeError = false;
    this.isUploading = false;
  }

  /* ---------------- FILE SELECT ---------------- */
  onFileSelected(event: any) {
    const file = event.target.files?.[0];

    if (!file) {
      return;
    }

    const maxSize = 5 * 1024 * 1024;

    if (file.size > maxSize) {
      this.selectedFile = null;
      this.fileSizeError = true;
      return;
    }

    this.fileSizeError = false;
    this.selectedFile = file;
  }

  /* ---------------- FETCH DOCUMENTS ---------------- */
  fetchDocuments() {
    this.isLoading = true;
    this.errorMessage = "";

    const payload = {
      adminId: 1,
      page: this.currentPage,
      size: this.PAGE_LIMIT,
    };

    this.api.post("admin/fetch-admin-documents", payload).subscribe({
      next: (res: any) => {
        console.log("Documents response:", res);

        if (res?.res) {
          this.customers = res.data || [];
          this.totalPage = Number(res.totalPage ?? 1);
          this.hasMoreData =
            this.totalPage > 0 && this.currentPage < this.totalPage;
        } else {
          this.customers = [];
          this.totalPage = 1;
          this.hasMoreData = false;
          this.errorMessage = "Dokumente konnten nicht geladen werden.";
        }

        this.isLoading = false;
      },
      error: (err) => {
        console.error("Fetch documents error:", err);

        this.customers = [];
        this.totalPage = 1;
        this.hasMoreData = false;
        this.errorMessage = "Etwas ist schiefgelaufen.";
        this.isLoading = false;
      },
    });
  }

  /* ---------------- PAGINATION ---------------- */
  nextPage() {
    if (this.currentPage < (this.totalPage || 0)) {
      this.currentPage++;
      this.fetchDocuments();
    }
  }

  prevPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.fetchDocuments();
    }
  }

  /* ---------------- CREATE / EDIT PDF ---------------- */
  submitPdf() {
    if (this.fileSizeError) {
      return;
    }

    if (!this.documentCategory.trim()) {
      return;
    }

    /*
      Create mode:
      Must have file

      Edit mode:
      File optional
    */
    if (!this.selectedDoc && !this.selectedFile) {
      return;
    }

    const payload: any = {
      adminId: 1,
      documentCategory: this.documentCategory.trim(),
    };

    // edit mode
    if (this.selectedDoc?.adminDocId) {
      payload.adminDocId = this.selectedDoc.adminDocId;
    }

    const formData = new FormData();
    formData.append("data", JSON.stringify(payload));

    // append file only if new file selected
    if (this.selectedFile) {
      formData.append("file", this.selectedFile);
    }

    this.isUploading = true;

    this.api.post("admin/add-doc", formData).subscribe({
      next: (res: any) => {
        console.log("Upload/Edit success:", res);

        this.isUploading = false;

        this.closeModal();

        // refresh updated list
        this.fetchDocuments();
      },
      error: (err) => {
        console.error("Upload/Edit failed:", err);
        this.isUploading = false;
      },
    });
  }

  /* ---------------- VIEW PDF ---------------- */
  openPdf(filePath: string) {
    const baseUrl = "http://192.168.0.155:8080/assets/super-admin/";

    window.open(baseUrl + filePath, "_blank");
  }
}
