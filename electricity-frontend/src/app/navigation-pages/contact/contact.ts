import { ChangeDetectorRef, Component, ElementRef, Inject, PLATFORM_ID } from '@angular/core';
import { ContactPerson } from '../../layout/contact-person/contact-person';
import { NeedSupport } from '../../layout/need-support/need-support';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-contact',
  imports: [
    ContactPerson,
    NeedSupport,
    CommonModule,
    CommonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    FormsModule,
  ],
  templateUrl: './contact.html',
  styleUrl: './contact.css',
})
export class Contact {
  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    private eRef: ElementRef,
    private route: ActivatedRoute,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object,
  ) {}

  showDropdown = false;

  categories = [
    { serviceName: 'Allgemeine Frage' },
    { serviceName: 'Tarifvergleich / Anbieter' },
    { serviceName: 'Kooperation / Partnerschaft' },
    { serviceName: 'Sonstiges' },
  ];
  selectedCategory: any = null;

  toggleDropdown(event: Event) {
    event.stopPropagation();
    this.showDropdown = !this.showDropdown;
  }

  selectCategory(item: any, event: Event) {
    event.stopPropagation();

    this.selectedCategory = item;
    this.showDropdown = false;
  }
  formData = {
    salutation: '',
    title: '',
    firstName: '',
    lastName: '',
    email: '',
    contactNumber: '',
    customerId: '',
    inquiry: '',
  };

  fieldErrors: any = {};

  validate(): boolean {
    this.fieldErrors = {};

    if (!this.formData.salutation.trim()) {
      this.fieldErrors['salutation'] = 'Bitte Anrede eingeben';
    }

    if (!this.formData.firstName.trim()) {
      this.fieldErrors['firstName'] = 'Bitte Vorname eingeben';
    }

    if (!this.formData.lastName.trim()) {
      this.fieldErrors['lastName'] = 'Bitte Nachname eingeben';
    }

    if (!this.formData.email.trim()) {
      this.fieldErrors['email'] = 'Bitte E-Mail eingeben';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.formData.email)) {
      this.fieldErrors['email'] = 'Ungültige E-Mail-Adresse';
    }

    if (!this.formData.contactNumber.trim()) {
      this.fieldErrors['contactNumber'] = 'Bitte Telefonnummer eingeben';
    }

    if (!this.formData.customerId.trim()) {
      this.fieldErrors['customerId'] = 'Bitte Kundennummer eingeben';
    }

    if (!this.selectedCategory) {
      this.fieldErrors['category'] = 'Bitte Betreff auswählen';
    }

    if (!this.formData.inquiry.trim()) {
      this.fieldErrors['inquiry'] = 'Bitte Nachricht eingeben';
    }

    return Object.keys(this.fieldErrors).length === 0;
  }

  submitForm() {
    if (!this.validate()) return;

    const payload = {
      salutation: this.formData.salutation,
      title: this.formData.title,
      firstName: this.formData.firstName,
      lastName: this.formData.lastName,
      email: this.formData.email,
      contactNumber: this.formData.contactNumber,
      customerId: this.authService.getUserId(),
      inquiry: this.formData.inquiry,
      categoryId: this.selectedCategory?.id,
    };

    console.log('Contact Form Payload:', payload);

    // API call later
  }
}
