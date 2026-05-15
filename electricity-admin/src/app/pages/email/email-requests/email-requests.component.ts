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
  uploadFields: number[] = [0];
  selectedFiles : any[] = [];
  message: string = '';
  isError: boolean = false;

  categories: any[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get("http://localhost:8080/email-category/all")
    .subscribe((res: any) => {
      this.categories = res;
    })
  }

  addUploadField() {
    if ( this.uploadFields.length < 5) {
      this.uploadFields.push(this.uploadFields.length);
    } else {
      this.message = "Maximum 5 files allowed";
      this.isError = true;
      setTimeout(() => {
        this.message = "";
      }, 2000);
    }
  }

  removeUploadField(index: number) {
    this.uploadFields.splice(index, 1);
    this.selectedFiles.splice(index, 1);
  }

  onFileSelected(event: any, index: number) {
    this.selectedFiles[index] = event.target.files[0];
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

        category: {
          cateId: this.selectedCategory
        }
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

          setTimeout(() => {
            this.message = '';
          }, 3000);
        },

        error: (err) => {

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
