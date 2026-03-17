import { Component, AfterViewInit, ViewChild, ElementRef, Inject, PLATFORM_ID, afterNextRender } from '@angular/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import flatpickr from 'flatpickr';
import { CommonModule } from '@angular/common';
import { FormsModule, NgModel } from '@angular/forms';
import { isPlatformBrowser } from '@angular/common';
import { Instance } from 'flatpickr/dist/types/instance';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { Routes } from '@angular/router';

@Component({
  selector: 'app-delivery-address',
  imports: [MatDatepickerModule, MatInputModule, MatNativeDateModule, MatIconModule, CommonModule, FormsModule, RouterModule],
  templateUrl: './delivery-address.html',
  styleUrl: './delivery-address.css',
})
export class DeliveryAddress implements AfterViewInit {

  constructor(@Inject(PLATFORM_ID) private platformId: Object,
              private router: Router,
              private route: ActivatedRoute
  ) {}


  @ViewChild('dateInput') dateInput!: ElementRef;

  fp!: Instance;

  ngAfterViewInit() {
    if (isPlatformBrowser(this.platformId)) {
      afterNextRender(async () => {
        const flatpickr = (await import('flatpickr')).default;
        flatpickr(this.dateInput.nativeElement, {});
      });
    }
  }


  openPage() {
    this.router.navigate(['/electricity-comparision/connection-data'], {});
  }

}
