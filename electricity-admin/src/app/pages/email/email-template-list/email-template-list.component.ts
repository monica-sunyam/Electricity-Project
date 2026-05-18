import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-email-template-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './email-template-list.component.html',
})

export class EmailTemplateListComponent implements OnInit {

  emailList: any[] = [];
  isLoading = false;
  errorMessage = '';
  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.fetchTemplates();
  }

  fetchTemplates(): void {
    this.isLoading = true;
    this.http.get<any[]>(
      'http://localhost:8080/email-management/all'
    )

    .subscribe({

      next: (res) => {
        this.isLoading = false;
        this.emailList = res || [];
      },

      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Something went wrong';
      }

    });
  }
}