import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../shared/services/api.service';
import { AuthService } from '../../../shared/services/auth.service';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

export interface ServiceCategory {
  serviceId: number;
  serviceName: string;
  serviceType: string;
}

@Component({
  selector: 'app-query-categories',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './query-categories.component.html',
  styleUrl: './query-categories.component.css',
})
export class QueryCategoriesComponent implements OnInit {
  categories: ServiceCategory[] = [];
  isLoading = false;
  errorMessage = '';

  constructor(
    private api: ApiService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.fetchCategories();
  }

  fetchCategories(): void {
    this.isLoading = true;
    this.errorMessage = '';

    const payload = {
      adminId: this.authService.getUserId(),
      page: 1 // Passe dies an, falls du hier auch Pagination benötigst
    };

    // HINWEIS: Passe den Endpunkt an, falls es einen separaten Endpunkt für Kategorien gibt.
    // Hier nutze ich den aus dem vorherigen Beispiel bekannten Endpunkt.
    this.api.post('admin/fetch-services', payload).subscribe({
      next: (res: any) => {
        this.isLoading = false;
        if (res && res.res) {
          this.categories = Array.isArray(res.data) ? res.data : [];
        } else {
          this.errorMessage = res.message || 'Fehler beim Laden der Kategorien.';
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Ein Verbindungsfehler ist aufgetreten.';
        console.error('Fetch Categories Error:', err);
      }
    });
  }

  onDelete(id: number): void {
    if (confirm('Are you sure you want to delete this category?')) {
      const payload = {
        adminId: this.authService.getUserId(),
        serviceId: id
      };

      this.api.post('admin/remove-customer-service', payload).subscribe({
        next: (res: any) => {
          if (res?.res) {
            this.categories = this.categories.filter(c => c.serviceId !== id);
          } else {
            alert(res.message || 'Delete failed');
          }
        },
        error: () => alert('Something went wrong')
      });
    }
  }
}