import { ChangeDetectorRef, Component, computed, effect, OnDestroy, OnInit } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { CommonModule } from '@angular/common';
import { Observable, Subscription } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { NavItem } from '../navigation/navigation';
import { ContentService } from '../../services/content.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    MatFormFieldModule,
    MatInputModule,
    CommonModule,
    RouterModule,
  ],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  isLoggedIn = computed(() => !!this.authService.currentUser()?.user_id);
  navItems: NavItem[] = [];
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
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    public contentService: ContentService,
    private router: Router,
    private route: ActivatedRoute,
  ) {
    effect(() => {
      const state = this.isLoggedIn();
      console.log('UI Signal changed to:', state);

      this.cdr.detectChanges();
    });
  }

  logout() {
    this.authService.logout();
  }

  redirect() {
    if (this.isLoggedIn()) {
      this.router.navigate(['/customer'], { relativeTo: this.route });
    } else {
      this.router.navigate(['/electricity-comparision/register'], { relativeTo: this.route });
    }
  }
  isMenuOpen = false;
  showMobileSearch = false;

  toggleSearch(): void {
    this.showMobileSearch = !this.showMobileSearch;
    if (this.showMobileSearch) {
      document.body.classList.add('mobile-search-open');
    } else {
      document.body.classList.remove('mobile-search-open');
    }
  }
  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
  }

  closeMenu(): void {
    this.isMenuOpen = false;
  }

  getRouterLink(fileName: string): string {
    return this.ROUTE_MAP[fileName] || '#';
  }

  getImageUrl(url: string | null): string {
    return this.contentService.getImageUrl(url);
  }

  getIconClass(fileName: string): string {
    return this.CLASS_MAP[fileName] || 'nav-icon-default';
  }

  ngOnInit(): void {
    this.contentService.getNav().subscribe({
      next: (data) => {
        this.navItems = data;
      },
      error: () => {
        this.cdr.detectChanges();
      },
    });
  }
}
