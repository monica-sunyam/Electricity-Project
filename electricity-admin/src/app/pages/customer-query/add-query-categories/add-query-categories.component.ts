import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../shared/services/api.service';
import { AuthService } from '../../../shared/services/auth.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-customer-service-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-query-categories.component.html'
})
export class CustomerServiceFormComponent implements OnInit {

  serviceName: string = '';
  serviceId: number | null = null;

  isLoading = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private api: ApiService,
    private authService: AuthService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.serviceId = Number(id);
      this.fetchServiceDetails();
    }
  }

  // 🔍 Fetch data for edit
  fetchServiceDetails(): void {
    this.isLoading = true;

    const payload = {
      adminId: this.authService.getUserId(),
      serviceId: this.serviceId
    };

    this.api.post('admin/get-service-by-id', payload).subscribe({
      next: (res: any) => {
        this.isLoading = false;

        if (res?.res) {
          this.serviceName = res.data?.serviceName || '';
        } else {
          this.errorMessage = res.message || 'Failed to load service';
        }
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Error fetching service details';
      }
    });
  }

  // 💾 Submit (Create + Update)
  onSubmit(): void {
    if (!this.serviceName.trim()) {
      this.errorMessage = 'Please enter service name';
      return;
    }

    this.isLoading = true;
    this.successMessage = '';
    this.errorMessage = '';

    const payload: any = {
      serviceName: this.serviceName.trim(),
      serviceType: 'ALL',
      adminId: this.authService.getUserId()
    };

    // ✅ Only add serviceId if editing
    if (this.serviceId) {
      payload.serviceId = this.serviceId;
    }

    this.api.post('admin/add-customer-service', payload).subscribe({
      next: (res: any) => {
        this.isLoading = false;

        if (res?.res) {
          this.successMessage = res.message || 'Saved successfully';

          if (!this.serviceId) {
            this.resetForm();
          }
        } else {
          this.errorMessage = res.message || 'Something went wrong';
        }
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Server error';
      }
    });
  }

  resetForm(): void {
    this.serviceName = '';
    this.errorMessage = '';
  }
}