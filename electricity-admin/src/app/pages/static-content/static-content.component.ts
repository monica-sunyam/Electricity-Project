import { CommonModule } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { ApiService } from "../../shared/services/api.service";
import { AuthService } from "../../shared/services/auth.service";
import { FormsModule } from "@angular/forms";
import { CKEditorModule } from "@ckeditor/ckeditor5-angular";
import ClassicEditor from "@ckeditor/ckeditor5-build-classic";

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
  ) {}

  ngOnInit(): void {
    this.fetchDocuments();
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
    if (confirm('Möchten Sie diesen Inhalt wirklich löschen?')) {
      this.customContents = this.customContents.filter(c => c.adminDocId !== doc.adminDocId);
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
    if (!text) return 'Keine Beschreibung verfügbar.';
    const plainText = text.replace(/<[^>]*>/g, '');
    const words = plainText.split(/\s+/);
    if (words.length > 100) {
      return words.slice(0, 100).join(' ') + '...';
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

    this.api.post("admin/fetch-admin-documents", payload).subscribe({
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

    this.api.post("admin/add-doc", formData).subscribe({
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
    const currentDateTime = new Date().toLocaleString('de-DE', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });

    const finishSubmit = (logoUrl: string | null) => {
        this.savedBannerUrl = logoUrl;
        this.savedImageTitle = this.imageTitle;
        this.savedPopupContent = this.popupContent;
        
        if (this.editingDocId) {
            // Update existing row
            const index = this.customContents.findIndex(c => c.adminDocId === this.editingDocId);
            if (index !== -1) {
                this.customContents[index].documentCategory = this.imageTitle;
                this.customContents[index].description = this.popupContent;
                if (logoUrl) {
                    this.customContents[index].logoUrl = logoUrl;
                }
            }
        } else {
            // Add new row to the table list!
            this.customContents.unshift({
                adminDocId: Math.floor(Math.random() * 1000) + 100,
                documentCategory: this.imageTitle || 'Unbenanntes Dokument',
                originalFileName: 'Manuell hinzugefügt',
                description: this.popupContent,
                expireTime: currentDateTime, // Set upload time as expire time per user request
                logoUrl: logoUrl,
                isCustom: true
            });
        }
        
        this.closeModal();
    };

    if (this.bannerFile) {
      const reader = new FileReader();
      reader.onload = (e) => {
        finishSubmit(e.target?.result as string);
      };
      reader.readAsDataURL(this.bannerFile);
    } else {
      finishSubmit(null);
    }
  }

  /* ---------------- VIEW PDF ---------------- */
  openPdf(filePath: string) {
    const baseUrl = "http://192.168.0.155:8080/assets/super-admin/";
    window.open(baseUrl + filePath, "_blank");
  }
}
