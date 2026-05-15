import { CommonModule } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { CKEditorModule } from "@ckeditor/ckeditor5-angular";
import ClassicEditor from "@ckeditor/ckeditor5-build-classic";
import { ApiService } from "../../../shared/services/api.service";
import { AuthService } from "../../../shared/services/auth.service";
import { HttpClient, HttpHeaders } from "@angular/common/http";

@Component({
  selector: "app-static-content",
  standalone: true,
  imports: [CommonModule, FormsModule, CKEditorModule],
  templateUrl: "./static-content.component.html",
  styleUrl: "./static-content.component.css",
})
export class StaticContentComponent implements OnInit {
  customers: any[] = [];
  customContents: any[] = [];
  editingDocId: number | null = null;

  fileSizeError: boolean = false;
  isLoading = false;
  errorMessage = "";
  hasMoreData = true;

  private readonly PAGE_LIMIT = 5;

  currentPage = 1;
  totalPage: number | null = null;

  isModalOpen = false;
  isTemplateModalOpen = false;
  isContentModalOpen = false;
  isPreviewModalOpen = false;

  selectedFile: File | null = null;
  bannerFile: File | null = null;
  isUploading: boolean = false;
  selectedDoc: any = null;

  documentCategory: string = "";
  imageTitle: string = "";
  popupContent: string = "";

  savedBannerUrl: string | ArrayBuffer | null = null;
  savedImageTitle: string = "";
  savedPopupContent: string = "";

  public Editor = ClassicEditor;

  constructor(
    private api: ApiService,
    private authService: AuthService,
    private http: HttpClient,
  ) {}

  ngOnInit(): void {
    this.fetchDocuments();
    this.fetchCustomContents();
  }

  /* ---------------- OPEN MODAL ---------------- */
  openModal(doc?: any) {
    this.selectedFile = null;
    this.fileSizeError = false;
    this.selectedDoc = doc || null;
    this.documentCategory = doc?.documentCategory || "";
    this.isModalOpen = true;
  }

  /* ---------------- OPEN TEMPLATE MODAL ---------------- */
  openTemplateModal() {
    this.selectedFile = null;
    this.fileSizeError = false;
    this.selectedDoc = null;
    this.documentCategory = "";
    this.isTemplateModalOpen = true;
  }

  /* ---------------- OPEN CONTENT MODAL ---------------- */
  openContentModal() {
    this.editingDocId = null;
    this.bannerFile = null;
    this.imageTitle = "";
    this.popupContent = "";
    this.isContentModalOpen = true;
  }

  /* ---------------- EDIT CONTENT ---------------- */
  editContent(doc: any) {
    this.editingDocId = doc.adminDocId;
    this.imageTitle = doc.documentCategory;
    this.popupContent = doc.description;
    this.bannerFile = null;
    this.isContentModalOpen = true;
  }

  /* ---------------- DELETE CONTENT ---------------- */
  deleteContent(doc: any) {
    if (confirm("Möchten Sie diesen Inhalt wirklich löschen?")) {
      this.http.post(`admin/static-content/delete/${doc.adminDocId}`, null).subscribe({
        next: (res: any) => {
          this.fetchCustomContents(); // Refresh table to remove deleted row
        },
        error: (err: any) => {
          console.error("Delete Failed", err);
        },
      });
    }
  }

  /* ---------------- OPEN PREVIEW MODAL ---------------- */
  openPreviewModal(doc?: any) {
    if (doc && doc.isCustom) {
      this.savedBannerUrl = doc.logoUrl;
      this.savedImageTitle = doc.documentCategory;
      this.savedPopupContent = doc.description;
    }
    this.isPreviewModalOpen = true;
  }

  /* ---------------- TRUNCATE WORDS ---------------- */
  truncateWords(text: string | undefined): string {
    if (!text) return "Keine Beschreibung verfügbar.";
    const plainText = text.replace(/<[^>]*>/g, "");
    const words = plainText.split(/\s+/);
    if (words.length > 100) {
      return words.slice(0, 100).join(" ") + "...";
    }
    return plainText;
  }

  /* ---------------- CLOSE MODAL ---------------- */
  closeModal(): void {
    this.isModalOpen = false;
    this.isTemplateModalOpen = false;
    this.isContentModalOpen = false;
    this.isPreviewModalOpen = false;

    this.selectedFile = null;
    this.bannerFile = null;
    this.selectedDoc = null;
    this.documentCategory = "";
    this.imageTitle = "";
    this.popupContent = "";
    this.editingDocId = null;

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

  onBannerSelected(event: any) {
    const file = event.target.files?.[0];
    if (file) {
      this.bannerFile = file;
    }
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

    this.http.post("http://localhost:8080/admin/fetch-admin-documents", payload).subscribe({
      next: (res: any) => {
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
      error: (err: any) => {
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

  /* ---------------- CREATE / EDIT PDF OR TEMPLATE ---------------- */
  submitPdf() {
    if (this.fileSizeError) {
      return;
    }

    if (!this.documentCategory.trim()) {
      return;
    }

    if (!this.selectedDoc && !this.selectedFile) {
      return;
    }

    const payload: any = {
      adminId: 1,
      documentCategory: this.documentCategory.trim(),
    };

    if (this.selectedDoc?.adminDocId) {
      payload.adminDocId = this.selectedDoc.adminDocId;
    }

    const formData = new FormData();
    formData.append("data", JSON.stringify(payload));

    if (this.selectedFile) {
      formData.append("file", this.selectedFile);
    }

    this.isUploading = true;

    this.http.post("admin/add-doc", formData).subscribe({
      next: (res: any) => {
        this.isUploading = false;
        this.closeModal();
        this.fetchDocuments();
      },
      error: (err: any) => {
        this.isUploading = false;
      },
    });
  }

  /* ---------------- SUBMIT CONTENT ---------------- */
  submitContent() {
    this.isUploading = true;

    // 1. Create FormData instead of a JSON payload
    const formData = new FormData();
    formData.append("title", this.imageTitle);
    formData.append("description", this.popupContent);

    // 2. Append the actual File object directly (no Base64 conversion needed!)
    if (this.bannerFile) {
      formData.append("file", this.bannerFile);
    }

    if (this.editingDocId) {
      // Update Existing Content (POST)
      this.http
        .post(`http://localhost:8080/admin/static-update/${this.editingDocId}`, formData)
        .subscribe({
          next: (res: any) => {
            this.isUploading = false;
            this.closeModal();
            this.fetchCustomContents(); // Automatically refresh table
          },
          error: (err: any) => {
            this.isUploading = false;
            console.error("Update Failed", err);
          },
        });
    } else {
      // Add New Content (POST)
      this.http.post("http://localhost:8080/admin/static-add", formData).subscribe({
        next: (res: any) => {
          this.isUploading = false;
          this.closeModal();
          this.fetchCustomContents(); // Automatically refresh table
        },
        error: (err: any) => {
          this.isUploading = false;
          console.error("Add Failed", err);
        },
      });
    }
  }

  /* ---------------- VIEW PDF ---------------- */
  openPdf(filePath: string) {
    const baseUrl = "http://192.168.0.155:8080/assets/super-admin/";
    window.open(baseUrl + filePath, "_blank");
  }

  /* ---------------- FETCH ALL CONTENT FROM BACKEND ---------------- */
  fetchCustomContents() {
    this.isLoading = true;
    this.api.get("admin/static-content/all").subscribe({
      next: (res: any[]) => {
        this.isLoading = false;

        // Map the backend names to our frontend variables
        this.customContents = res.map((item: any) => ({
          adminDocId: item.id,
          documentCategory: item.title,
          description: item.description,
          logoUrl: item.logoUrl || item.logo,
          expireTime: item.uploadTime,
          isCustom: true, // keeps the preview modal 'eye' button working
        }));
      },
      error: (err: any) => {
        this.isLoading = false;
        console.error("Failed to load backend data", err);
      },
    });
  }
}
