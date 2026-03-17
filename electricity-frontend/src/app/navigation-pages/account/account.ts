import { Component } from '@angular/core';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { FormsModule, NgModel } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-account',
  imports: [MatInputModule,  MatIconModule, CommonModule, FormsModule, RouterModule],
  templateUrl: './account.html',
  styleUrl: './account.css',
})
export class Account {

  constructor(
              private router: Router,
              private route: ActivatedRoute
  ) {}

  openPage() {
    this.router.navigate(['/electricity-comparision/checkout'], {});
  }
}
