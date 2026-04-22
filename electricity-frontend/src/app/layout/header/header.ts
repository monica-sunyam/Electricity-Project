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
import { ActivatedRoute, Router } from '@angular/router';

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
  ],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  isLoggedIn = computed(() => !!this.authService.currentUser()?.user_id);

  constructor(
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
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
}
