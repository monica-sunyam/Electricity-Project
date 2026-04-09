import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';

export interface NavItem {
  id: number;
  type: number;
  contentUrl: string;
  originalFileName: string;
  saving: string | null;
  savingPriceDetail: string | null;
  createdOn: number;
  updatedOn: number | null;
  heading: string | null;
  subHeading: string | null;
  order: number;
}

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  templateUrl: './navigation.html',
  styleUrl: './navigation.css',
})
export class Navigation implements OnInit {
  private readonly BASE_IMAGE_URL = 'http://192.168.0.155:8080/assets/super-admin/';
  navItems: NavItem[] = [];
  isLoading = true;
  isMenuOpen = false;

  // Restore the original design by mapping filenames to CSS classes
  private readonly CLASS_MAP: Record<string, string> = {
    'Stromvergleich.png': 'nav-icon-strom',
    'Gasvergleich.png': 'nav-icon-gas',
    'Gewerbestrom.png': 'nav-icon-gewerbestrom',
    'Gewerbegas.png': 'nav-icons-outlined',
    'Grosskunde_2.png': 'nav-icons-grosskunde',
    '360_Vergleich.png': 'nav-icon-360',
    'Tarifwecker_weiss.png': 'nav-icons-outlined',
  };

  private readonly ROUTE_MAP: Record<string, string> = {
    'Stromvergleich.png': '/electricity-comparision',
    'Gasvergleich.png': '/gas-comparision',
    'Gewerbestrom.png': '/commercial-electricity',
  };

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.http.post<any>('http://192.168.0.155:8080/api/content', {}).subscribe({
      next: (data) => {
        if (data?.res && data?.menu?.nav) {
          this.navItems = [...data.menu.nav].sort((a, b) => a.order - b.order);
        }
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  getImageUrl(contentUrl: string): string {
    return `${this.BASE_IMAGE_URL}${contentUrl}`;
  }

  getIconClass(fileName: string): string {
    return this.CLASS_MAP[fileName] || 'nav-icon-default';
  }

  getRouterLink(fileName: string): string {
    return this.ROUTE_MAP[fileName] || '#';
  }

  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
  }
  closeMenu() {
    this.isMenuOpen = false;
  }
}
