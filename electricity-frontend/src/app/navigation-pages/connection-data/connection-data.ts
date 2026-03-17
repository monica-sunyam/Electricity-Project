import { Component, AfterViewInit, ViewChild, ElementRef, Inject, PLATFORM_ID, afterNextRender } from '@angular/core';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { FormsModule, NgModel } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';

import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { Routes } from '@angular/router';

@Component({
  selector: 'app-connection-data',
  imports: [ MatInputModule, MatNativeDateModule, MatIconModule, CommonModule, FormsModule, RouterModule, MatDatepickerModule],
  templateUrl: './connection-data.html',
  styleUrl: './connection-data.css',
})
export class ConnectionData {

  constructor(
              private router: Router,
              private route: ActivatedRoute
  ) {}

  openPage() {
    this.router.navigate(['/electricity-comparision/payment-method'], {});
  }

   selection: string = 'no';

  selectOption(value: string) {
    this.selection = value;
  }


}
