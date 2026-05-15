import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
@Component({
  selector: 'app-email-category',
  imports: [FormsModule],
  standalone: true,
  templateUrl: './email-category.component.html',
  styleUrl: './email-category.component.css',
})
export class EmailCategoryComponent {
  categoryName: string = "";
  successMessage: string = '';
  isError: boolean = false;
  constructor(private http: HttpClient) {}
  saveCategory() {
    if (!this.categoryName || !this.categoryName.trim()) {
      this.successMessage = 'Field is empty';
      this.isError = true;
      setTimeout(() => {
        this.successMessage = '';
      }, 3000)
      return;
    }
    const body = {
      name: this.categoryName,
      createdBy: 'Admin'
    };
    this.http.post('http://localhost:8080/email-category/save', body)
      .subscribe({
        next: (res) => {
          console.log(res);
          this.successMessage = 'Saved Successfully';
          this.isError = false;
          setTimeout(() => {
            this.successMessage = '';
          }, 3000)
          this.categoryName = '';
        },
        error: (err) => {
          console.log(err);
          alert('Something went wrong');
        }
      });

  }

  cancelCategoryForm() {
    this.categoryName = '';
    this.successMessage = '';
  }
}
