import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
@Component({
  selector: 'app-email-requests',
  imports: [FormsModule, CommonModule],
  standalone: true,
  templateUrl: './email-requests.component.html',
  styleUrl: './email-requests.component.css',
})
export class EmailRequestsComponent {

  selectedCategory: string = '';
  title: string = '';
  subtitle: string = '';
  emailContent: string = '';

  pdfList: any[] = [];
  selectedPdfs: any[] = [];
  selectedPdfValue: String = '';
  message: string = '';
  isError: boolean = false;

  categories: any[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get("http://localhost:8080/email-category/all")
    .subscribe((res: any) => {
      console.log(res);
      this.categories = res;
 
    })

    this.loadPdfs();
  }
  
  loadPdfs() {

    const payload = {
      adminId: 1,
      page: 0,
      size: 100
    };
    this.http.post<any>('http://192.168.155:8080/admin/fetch-admin-documents',
      payload
    )
    .subscribe({

      next: (res) => {
        console.log(res);
        this.pdfList = res.data || res.content || res;
      },

      error: (err) => {
        console.log(err);
      }
    });
  }

  addPdf(pdfId: any) {

    const pdf = this.pdfList.find(
      p => p.adminDocId == pdfId
    );
      if (
      pdf &&
      !this.selectedPdfs.some ( p => p.adminDocId == pdf.adminDocId
        )
      ) {
          this.selectedPdfs.push(pdf);
        }
        this.selectedPdfValue = '';
  }

  removePdf(index: number) {
    this.selectedPdfs.splice(index, 1);
  }

  submitForm() {

      if (
        !this.selectedCategory ||
        !this.title.trim() ||
        !this.subtitle.trim() ||
        !this.emailContent.trim()
      ) {

        this.message = 'Please fill all mandatory fields';
        this.isError = true;

        setTimeout(() => {
          this.message = '';
        }, 3000);

        return;
      }

      const body = {

        title: this.title,
        subtitle: this.subtitle,
        emailContent: this.emailContent,
        createdBy: 'Admin',

        cateId: this.selectedCategory,

        pdfIds: this.selectedPdfs.map(pdf => pdf.adminDocId)

      };

      this.http.post(
        'http://localhost:8080/email-management/save',
        body
      ).subscribe({

        next: (res) => {

          this.message = 'Successfully Submitted';
          this.isError = false;

          this.title = '';
          this.subtitle = '';
          this.emailContent = '';
          this.selectedCategory = '';
          this.selectedPdfs = [];

          setTimeout(() => {
            this.message = '';
          }, 3000);
        },

        error: (err) => {

          console.log(err);

          this.message = 'Submission Failed';
          this.isError = true;

          setTimeout(() => {
            this.message = '';
          }, 3000);
        }

      });

    }

    cancelForm() {

      this.selectedCategory = '';
      this.title = '';
      this.subtitle = '';
      this.emailContent = '';

      this.message = '';
    }
}
