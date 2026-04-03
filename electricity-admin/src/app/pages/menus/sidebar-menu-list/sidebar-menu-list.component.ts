import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../../shared/services/api.service';

@Component({
  selector: 'app-sidebar-menu-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar-menu-list.component.html',
  styleUrl: './sidebar-menu-list.component.css',
})
export class SidebarMenuListComponent implements OnInit {
  menus: any[] = [];
  isLoading = false;
  errorMessage = '';

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.fetchSidebarMenus();
  }

  fetchSidebarMenus(): void {
    this.isLoading = true;
    this.errorMessage = '';

    // Adjust the endpoint string to match your backend API
    this.api.get('admin/sidebar-menus').subscribe({
      next: (res: any) => {
        if (res.res && res.data) {
          this.menus = res.data;
        } else {
          this.menus = [];
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        // German error message for the UI
        this.errorMessage = 'Fehler beim Laden der Menüs';
        console.error('Fetch error:', err);
      }
    });
  }
}