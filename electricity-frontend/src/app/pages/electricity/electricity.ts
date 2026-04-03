import { Component, OnInit } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, switchMap, of } from 'rxjs';
import { AddressService } from './../../services/address.service';
import { Registration } from '../../layout/registration/registration';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatFormFieldModule } from '@angular/material/form-field';
import { NgZone } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-electricity',
  imports: [
    Registration,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    CommonModule,
    MatDialogModule,
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatFormFieldModule,
  ],
  templateUrl: './electricity.html',
  styleUrl: './electricity.css',
})
export class Electricity implements OnInit {
  addressForm!: FormGroup;
  cityOptions: { city: string; city_id: string }[] = [];
  streetOptions: { street: string; street_id: string }[] = [];

  constructor(
    public dialog: MatDialog,
    private fb: FormBuilder,
    private addressService: AddressService,
    private ngZone: NgZone,
    private cdr: ChangeDetectorRef,
  ) {}

  discountinfo = `<p> <strong>So haben wir gerechnet </strong> </p>
      <p> Wohnort: <i> Dortmund, 44141 </i>
       Jahresverbrauch: <i> 4.000 kWh </i> </p>
      <p> Günstigster Tarif: immergrün! Spar Smart FairMax, Kosten im ersten Jahr: 920,84 Euro </p>
      <p> Grundversorgungstarif: Dortmunder Energie- und Wasserversorgung GmbH Unser Strom.standard, Kosten: 1.828,72 Euro </p>
      <p><strong>Einsparung: 907,88 Euro</strong> <p>
      <p>(Stand: 16.02.2026) </p> `;

  activeInfo: 'discountinfo' | null = null;

  selectedPersons = 2;
  consumption = 2510;

  showCustomInput = false;

  customPersons: number | null = null;

  baseConsumptions: Record<number, number> = {
    1: 1600,
    2: 2510,
    3: 3500,
  };

  extraPerPerson = 850;

  ngOnInit(): void {
    this.addressForm = this.fb.group({
      postalCode: ['', [Validators.required, Validators.pattern(/^\d{5}$/)]],

      // city: [{ value: '', disabled: true }, Validators.required],
      city: [{ value: null, disabled: true }, Validators.required],

      street: [{ value: null, disabled: true }, Validators.required],

      houseNumber: [
        { value: '', disabled: true },
        [Validators.required, Validators.maxLength(6), Validators.pattern(/^[a-zA-Z0-9\s\/]*$/)],
      ],
    });

    this.handlePostalCodeChanges();
    this.handleCityChanges();
    this.handleStreetChanges();
  }

  private handlePostalCodeChanges() {
    this.addressForm
      .get('postalCode')
      ?.valueChanges.pipe(
        debounceTime(500),
        switchMap((zip) => {
          this.resetCity();
          this.resetStreet();
          this.resetHouseNumber();

          if (zip && zip.length === 5) {
            return this.addressService.getCitiesByZip(zip);
          }

          return of([]);
        }),
      )
      .subscribe((cities) => {
        console.log('Cities:', cities);

        this.cityOptions = cities;

        if (cities.length > 0) {
          this.addressForm.get('city')?.enable();

          // const firstCity = cities[0].city_id;
          // this.addressForm.get('city')?.setValue(firstCity);
        }
      });
  }

  private handleCityChanges() {
    this.addressForm
      .get('city')
      ?.valueChanges.pipe(debounceTime(300))
      .subscribe((placeId) => {
        if (!placeId) return;

        this.resetStreet();
        this.resetHouseNumber();

        this.addressService.getStreetsByCity(placeId).subscribe((streets) => {
          this.ngZone.run(() => {
            this.streetOptions = streets;
            this.cdr.detectChanges();
            
            if (streets.length > 0) {
              this.addressForm.get('street')?.enable();
            }
          });
        });
      });
  }

  private handleStreetChanges() {
    this.addressForm.get('street')?.valueChanges.subscribe((street) => {
      if (!street) return;

      this.addressForm.get('houseNumber')?.enable();
    });
  }

  private resetCity() {
    this.cityOptions = [];
    this.addressForm.get('city')?.reset();
    this.addressForm.get('city')?.disable();
  }

  private resetStreet() {
    this.streetOptions = [];
    this.addressForm.get('street')?.reset();
    this.addressForm.get('street')?.disable();
  }

  private resetHouseNumber() {
    this.addressForm.get('houseNumber')?.reset();
    this.addressForm.get('houseNumber')?.disable();
  }

  trackByStreet(index: number, item: any) {
    return item.street;
  }

  selectPersons(persons: number) {
    if (persons === 999) {
      // mehr button trigger
      this.showCustomInput = true;
      return;
    }
    this.showCustomInput = false;
    this.selectedPersons = persons;
    this.consumption = this.calculateConsumption(persons);
  }

  selectMehr() {
    this.showCustomInput = true;
    this.consumption = 0;
    this.selectedPersons = 0;
  }

  onCustomPersonsChange(value: string) {
    const persons = Number(value);
    if (!persons || persons < 1) {
      this.consumption = 0;
      return;
    }
    this.customPersons = persons;
    this.selectedPersons = persons;
    this.consumption = this.calculateConsumption(persons);
  }

  calculateConsumption(persons: number): number {
    if (persons <= 3) {
      return this.baseConsumptions[persons];
    }

    const base = this.baseConsumptions[3];
    return base + (persons - 3) * this.extraPerPerson;
  }

  closeCustomInput() {
    this.showCustomInput = false;
    this.selectedPersons = 2;
    this.consumption = this.calculateConsumption(this.selectedPersons);
    this.customPersons = null;
  }

  currentDialogText = '';

  openInfo(template: any, text: string) {
    this.currentDialogText = text;
    this.dialog.open(template, { width: '200px', maxWidth: '80vw' });
  }
}
