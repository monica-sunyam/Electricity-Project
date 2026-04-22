import {
  Component,
  OnInit,
  ChangeDetectorRef,
  NgZone,
  ViewChild,
  HostListener,
  ElementRef,
  computed,
} from '@angular/core';
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
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NgSelectModule } from '@ng-select/ng-select';

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
    NgSelectModule,
  ],
  templateUrl: './electricity.html',
  styleUrl: './electricity.css',
})
export class Electricity implements OnInit {
  addressForm!: FormGroup;
  cityOptions: { city: string; city_id: string }[] = [];
  streetOptions: { street: string; street_id: string }[] = [];
  isRestoring = false;

  isLoggedIn = computed(() => !!this.authService.currentUser()?.user_id);

  constructor(
    public dialog: MatDialog,
    private fb: FormBuilder,
    private addressService: AddressService,
    private ngZone: NgZone,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private authService: AuthService,
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
  streetDropdownKey = 0;
  isStreetLoading = false;
  customPersonsValue: number = 0;
  citySearch = '';
  filteredCityOptions: any[] = [];
  showCityDropdown = false;

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
    if (this.isLoggedIn()) {
      this.authService.fetchCustomer();
    }

    this.authService.getCustomerData().subscribe((data) => {
      if (!data?.address) return;

      const saved = data.address;

      console.log('Prefill Address:', saved);

      this.isRestoring = true;

      this.addressForm.patchValue({
        postalCode: saved.zip,
      });

      // this.selectedPersons = saved.persons;
      // this.consumption = saved.consumption;

      // if (this.selectedPersons > 4) {
      //   this.showCustomInput = true;
      //   this.customPersonsValue = this.selectedPersons;
      //   this.onCustomPersonsChange(this.customPersonsValue.toString());
      // }
      // else {
      //   this.showCustomInput = false;
      // }
      this.addressService.getCitiesByZip(saved.zip).subscribe((cities) => {
        this.cityOptions = cities;
        this.filteredCityOptions = [...cities];

        this.addressForm.get('city')?.enable();

        const matchedCity = cities.find((c) => c.city === saved.city);

        if (!matchedCity) {
          this.isRestoring = false;
          return;
        }

        this.citySearch = matchedCity.city;
        this.lastValidCity = matchedCity;

        this.cdr.detectChanges();

        // this.addressForm.get('city')?.setValue(matchedCity.city_id, {
        //   emitEvent: false,
        // });

        // if (matchedCity) {
        this.addressForm.get('city')?.setValue(matchedCity.city_id);

        this.isStreetLoading = true;
        this.addressForm.get('street')?.enable();

        this.addressService.getStreetsByCity(matchedCity.city_id).subscribe((streets) => {
          this.streetOptions = streets;
          this.filteredStreetOptions = [...streets];

          this.addressForm.get('street')?.enable();

          const matchedStreet = streets.find(
            (s) => s.street.trim().toLowerCase() === (saved.street ?? '').trim().toLowerCase(),
          );

          if (matchedStreet) {
            this.streetSearch = matchedStreet.street;
            this.lastValidStreet = matchedStreet.street;

            this.addressForm.get('street')?.setValue(matchedStreet.street, {
              emitEvent: false,
            });
            this.isStreetLoading = false;
          }

          // this.addressForm.patchValue(
          //   {
          //     street: saved.street,
          //   },
          //   { emitEvent: false },
          // );

          this.addressForm.get('houseNumber')?.enable();
          this.addressForm.patchValue({
            houseNumber: saved.houseNumber,
          });
          this.cdr.detectChanges();
        });
        this.showCityDropdown = false;
        this.showDropdown = false;
      });
    });
  }

  streetSearch = '';
  filteredStreetOptions: any[] = [];
  showDropdown = false;
  lastValidCity: { city: string; city_id: string } | null = null;
  lastValidStreet: string | null = null;

  onCityInput(event: any) {
    if (this.addressForm.get('city')?.disabled) return;
    this.closeAllDropdowns();
    const value = event.target.value.trim().toLowerCase();
    this.citySearch = value;

    this.filteredCityOptions = this.cityOptions.filter((c) => c.city.toLowerCase().includes(value));

    this.showCityDropdown = true;
  }
  onCityFocus(event: Event) {
    event.stopPropagation();
    this.closeAllDropdowns();
    this.showCityDropdown = true;
  }
  selectCity(city: any) {
    this.citySearch = city.city;

    this.addressForm.get('city')?.setValue(city.city_id);
    this.lastValidCity = city;
    this.showCityDropdown = false;

    this.filteredCityOptions = this.cityOptions;
  }

  onStreetInput(event: any) {
    if (this.addressForm.get('street')?.disabled) return;
    const value = event.target.value.trim().toLowerCase();
    this.streetSearch = value;

    this.filteredStreetOptions = this.streetOptions.filter((s) =>
      (s.street ?? '').toLowerCase().includes(value),
    );

    this.showDropdown = true;
  }

  selectStreet(street: any) {
    this.streetSearch = street.street;

    this.addressForm.get('street')?.setValue(street.street);

    this.lastValidStreet = street.street;
    this.showDropdown = false;

    this.filteredStreetOptions = this.streetOptions;
  }

  closeAllDropdowns() {
    this.showCityDropdown = false;
    this.showDropdown = false;
  }

  private handlePostalCodeChanges() {
    this.addressForm
      .get('postalCode')
      ?.valueChanges.pipe(
        debounceTime(500),
        switchMap((zip) => {
          const isValidZip = /^\d{5}$/.test(zip);
          if (this.isRestoring) return of([]);
          this.resetCity();
          this.resetStreet();
          this.resetHouseNumber();
          if (!isValidZip) {
            return of([]);
          }
          if (zip && zip.length === 5) {
            return this.addressService.getCitiesByZip(zip);
          }

          return of([]);
        }),
      )
      .subscribe((cities) => {
        console.log('Cities:', cities);

        this.cityOptions = cities;
        this.filteredCityOptions = cities;

        if (cities.length > 0) {
          this.addressForm.get('city')?.enable();

          // const firstCity = cities[0].city_id;
          // this.addressForm.get('city')?.setValue(firstCity);
          if (cities.length === 1) {
            const city = cities[0];

            this.citySearch = city.city;
            this.lastValidCity = city;
            this.showCityDropdown = false;

            this.addressForm.get('city')?.setValue(city.city_id);
            this.cdr.detectChanges();
          }
        } else {
          this.addressForm.get('city')?.disable();
        }

        this.cdr.detectChanges();
      });
  }

  private handleCityChanges() {
    this.addressForm
      .get('city')
      ?.valueChanges.pipe(debounceTime(300))
      .subscribe((placeId) => {
        const zip = this.addressForm.get('postalCode')?.value;

        if (!placeId || this.isRestoring || !/^\d{5}$/.test(zip)) {
          return;
        }
        this.streetOptions = [];
        this.resetStreet();
        this.resetHouseNumber();
        this.isStreetLoading = true;

        this.addressForm.get('street')?.enable();
        this.addressService.getStreetsByCity(placeId).subscribe((streets) => {
          const currentZip = this.addressForm.get('postalCode')?.value;
          if (!/^\d{5}$/.test(currentZip)) {
            return;
          }
          this.ngZone.run(() => {
            this.streetOptions = streets;

            this.filteredStreetOptions = [...streets];

            this.streetDropdownKey++;
            const streetControl = this.addressForm.get('street');
            streetControl?.setValue(null);

            this.isStreetLoading = false;
            this.cdr.detectChanges();

            if (streets.length > 0) {
              this.addressForm.get('street')?.enable();
            }
            this.streetSearch = '';

            if (!this.isRestoring) {
              this.showDropdown = true;
            }
            this.cdr.detectChanges();
          });
        });
      });
  }

  private handleStreetChanges() {
    this.addressForm.get('street')?.valueChanges.subscribe((street) => {
      if (!street) return;
      this.resetHouseNumber();
      this.addressForm.get('houseNumber')?.enable();
    });
  }

  private resetCity() {
    this.cityOptions = [];
    this.filteredCityOptions = [];
    this.citySearch = '';
    this.showCityDropdown = false;
    this.lastValidCity = null;

    const control = this.addressForm.get('city');
    control?.reset(null, { emitEvent: false });
    control?.disable();
    // this.addressForm.get('city')?.reset();
    // this.addressForm.get('city')?.disable();
  }

  private resetStreet() {
    this.streetOptions = [];
    this.filteredStreetOptions = [];
    this.streetSearch = '';
    this.showDropdown = false;
    this.lastValidStreet = '';

    const control = this.addressForm.get('street');
    control?.reset(null, { emitEvent: false });
    control?.disable();
    // this.addressForm.get('street')?.reset();
    // this.addressForm.get('street')?.disable();
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
    this.updateConsumptionUI();
  }

  selectMehr() {
    this.showCustomInput = true;
    this.consumption = 0;
    this.selectedPersons = 0;

    setTimeout(() => {
      this.updateConsumptionUI();
    });
  }

  onCustomPersonsChange(value: string) {
    const persons = Number(value);
    if (!persons || persons < 1) {
      this.consumption = 0;
      this.updateConsumptionUI();
      return;
    }
    this.customPersonsValue = persons;
    this.customPersons = persons;
    this.selectedPersons = persons;
    this.consumption = this.calculateConsumption(persons);
    this.updateConsumptionUI();
  }

  updateConsumptionUI() {
    const formatted = this.consumption.toLocaleString();

    const el1 = document.getElementById('consumptionValue');
    if (el1) el1.innerText = formatted;

    const el2 = document.getElementById('consumptionValueMore');
    if (el2) el2.innerText = formatted;
  }

  calculateConsumption(persons: number): number {
    if (persons <= 3) {
      return this.baseConsumptions[persons] || 0;
    }

    const base = this.baseConsumptions[3] || 0;
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

  goToComparison() {
    if (this.addressForm.invalid) {
      console.log('invalid address');
      this.addressForm.markAllAsTouched();
      return;
    }

    const selectedCityId = this.addressForm.value.city;

    const selectedCityObj = this.cityOptions.find((c) => c.city_id === selectedCityId);
    if (!selectedCityObj) {
      return; // or show error
    }

    // const data = {
    //   zip: this.addressForm.value.postalCode,
    //   city: selectedCityObj.city,
    //   city_id: selectedCityObj.city_id,
    //   street: this.addressForm.value.street,
    //   houseNumber: this.addressForm.value.houseNumber,
    //   persons: this.selectedPersons,
    //   consumption: this.consumption,
    // };

    // this.authService.setAddressData(data);

    this.router.navigate(['/electricity-comparision']);
  }

  @ViewChild('cityDropdown') cityDropdown!: ElementRef;
  @ViewChild('streetDropdown') streetDropdown!: ElementRef;

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;

    // CITY
    if (this.cityDropdown && !this.cityDropdown.nativeElement.contains(target)) {
      this.showCityDropdown = false;

      if (!this.isValidCity(this.citySearch)) {
        this.revertCity();
      }
    }

    // STREET
    if (this.streetDropdown && !this.streetDropdown.nativeElement.contains(target)) {
      this.showDropdown = false;

      if (!this.isValidStreet(this.streetSearch)) {
        this.revertStreet();
      }
    }
  }
  private isValidCity(value: string): boolean {
    return this.cityOptions.some((c) => c.city === value);
  }
  private isValidStreet(value: string): boolean {
    const v = (value ?? '').trim().toLowerCase();
    return this.streetOptions.some((s) => (s.street ?? '').trim().toLowerCase() === v);
  }

  private revertCity() {
    if (this.lastValidCity) {
      this.citySearch = this.lastValidCity.city;
      this.addressForm.get('city')?.setValue(this.lastValidCity.city_id);
    } else {
      this.citySearch = '';
      this.addressForm.get('city')?.reset();
    }
  }

  private revertStreet() {
    if (this.lastValidStreet) {
      this.streetSearch = this.lastValidStreet;
      this.addressForm.get('street')?.setValue(this.lastValidStreet);
    } else {
      this.streetSearch = '';
      this.addressForm.get('street')?.reset();
    }
  }
}
