import { Component } from '@angular/core';
import { MatInputModule } from '@angular/material/input';
import { CommonModule } from '@angular/common';
import { FormsModule, NgModel } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
@Component({
  selector: 'app-checkout-page',
  imports: [RouterModule, CommonModule],
  templateUrl: './checkout-page.html',
  styleUrl: './checkout-page.css',
})
export class CheckoutPage {
  constructor(
              private router: Router,
              private route: ActivatedRoute
  ) {}

  showConfirmation = false;

  openPage() {
    this.showConfirmation = true;
  }
}
