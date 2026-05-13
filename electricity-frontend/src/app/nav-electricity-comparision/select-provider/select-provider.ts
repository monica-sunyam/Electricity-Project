import {
  ChangeDetectorRef,
  Component,
  computed,
  ElementRef,
  HostListener,
  NgZone,
  OnInit,
  ViewChild,
} from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { ContactPerson } from '../../layout/contact-person/contact-person';
import { NeedSupport } from '../../layout/need-support/need-support';
import { AddressService } from '../../services/address.service';
import { AuthService } from '../../services/auth.service';
import { debounceTime, switchMap, of } from 'rxjs';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { Registration } from '../../layout/registration/registration';
import { NgSelectModule } from '@ng-select/ng-select';

export interface Rate {
  rateId: number;
  rateName: string;
  providerName: string;
  providerSVG: string;
  providerSVGPath: string;
  basePriceYear: number;
  basePriceMonth: number;
  workPrice: number;
  workPriceNt: number;
  totalPrice: number;
  totalPriceMonth: number;
  optEco: boolean;
  providerChangeFast: boolean;
  providerDigitalSigned: boolean;
  selfPayment: boolean;
  requiredEmail: boolean;
  partialPayment: number;
  cancel: number;
  cancelType: number;
  termBeforeNew: number;
  termBeforeNewType: string;
  termBeforeNewMaxDate: string;
  termAfterNew: number;
  termAfterNewMaxDate: string;
  optBonus: number;
  optBonusInstant: number;
  optBonusLoyalty: number;
  optGuarantee: string;
  optGuaranteeType: string;
  optTerm: string;
  recommended: boolean;
  savingPerYear: number;
  branch: string;
  type: string;
  rateChangeType: string[];
  commission: {
    type: string;
    amount: number;
    validFrom?: string;
    validTo?: string;
    requiredOrderState?: string;
  }[];
  uiExpanded?: boolean;
}

export interface RatesResponse {
  rates: {
    result: Rate[];
    total: number;
  };
  baseProvider: {
    result: {
      providerId: number;
      providerName: string;
      rates: {
        rateId: number;
        rateName: string;
        basePriceYear: number;
        basePriceMonth: number;
        workPrice: number;
        workPriceNt: number;
      }[];
    }[];
  };
  res: boolean;
}

interface SelectProviderState {
  priceDisplayMonthly: boolean;
  kundenPrivat: boolean;
  alleTarife: boolean;
  maxTermEgal: boolean;
  maxTerm24: boolean;
  maxTerm12: boolean;
  minGuaranteeEgal: boolean;
  minGuarantee24: boolean;
  minGuarantee12: boolean;
  minGuarantee6: boolean;
  selectedOption: string;
  isOpen: boolean;
}

@Component({
  selector: 'app-select-provider',
  standalone: true,
  imports: [
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    CommonModule,
    FormsModule,
    RouterModule,
    MatDialogModule,
    ContactPerson,
    NeedSupport,
    NgSelectModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatAutocompleteModule,
  ],
  templateUrl: './select-provider.html',
  styleUrl: './select-provider.css',
})
export class SelectProvider implements OnInit {
  private readonly providerStateStorageKey = 'select_provider_state';
  zip = '01067';
  city = 'Dresden';
  street = 'Adlergasse';
  houseNumber = '6';
  consum = 2510;
  type = 'private';
  branch = 'electric';

  isOpen = false;
  isLoading = false;
  hasLoadedRates = false;
  isDropdownOpen = false;
  isInfoOpen = false;

  priceDisplayMonthly = true;
  kundenPrivat = true;
  alleTarife = true;
  maxTermEgal = true;
  maxTerm24 = false;
  maxTerm12 = false;
  minGuaranteeEgal = true;
  minGuarantee24 = false;
  minGuarantee12 = false;
  minGuarantee6 = false;

  allRates: Rate[] = [];
  filteredRates: Rate[] = [];
  totalCount = 0;
  baseProvider: any = null;
  baseRate: any = null;
  addressForm!: FormGroup;
  cityOptions: { city: string; city_id: string }[] = [];
  streetOptions: { street: string; street_id: string }[] = [];
  isRestoring = false;
  selectedPersons = 2;
  // consumption = 2510;

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

  selectedOption = 'Sortieren nach: Beste Treffer';
  activeTabMap: { [rateId: number]: string } = {};

  baseProviders: any[] = [];

  selectedProviderId!: number;
  selectedRateId!: number;

  selectedProvider: any = null;
  selectedRate: any = null;

  selectedProviderRates: any[] = [];

  isGrossPrice = true;
  basePriceMode: 'month' | 'year' = 'month';
  abschlagCount = 12;
  adjustedBaseYearlyPrice = 0;
  originalBaseRate: any = null;
  originalEditableWorkPrice = 0;
  originalEditableBasePrice = 0;
  originalIsGrossPrice = true;
  originalBasePriceMode: 'month' | 'year' = 'month';
  editableWorkPrice = 0;
  editableBasePrice = 0;
  editableMonthlyPrice = 0;
  abschlagOptions = [12, 11, 10, 9, 8, 7, 6];
  baseAbschlagPrice = 0;
  savedActiveTab: 'price' | 'abschlag' = 'price';
  savedAbschlagCount = 12;
  savedEditableMonthlyPrice = 0;
  fieldErrors: any = {};

  streetSearch = '';
  filteredStreetOptions: any[] = [];
  showDropdown = false;
  lastValidCity: { city: string; city_id: string } | null = null;
  lastValidStreet: string | null = null;
  hasAddress = false;
  isEditMode = false;
  isModalOpen = false;

  @ViewChild('popoverContainer', { static: false }) popoverContainer!: ElementRef;

  constructor(
    private cdr: ChangeDetectorRef,
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute,
    public dialog: MatDialog,
    private eRef: ElementRef,
    private addressService: AddressService,
    private authService: AuthService,
    private ngZone: NgZone,
    private fb: FormBuilder,
  ) {}
  isLoggedIn = computed(() => !!this.authService.currentUser()?.user_id);

  ngOnInit(): void {
    this.restoreViewState();
    const data = this.authService.getAddressData();

    console.log('Received:', data);

    if (data && data.zip && data.city && data.street) {
      this.zip = data.zip;
      this.city = data.city;
      this.street = data.street;
      this.houseNumber = data.houseNumber;
      this.consum = data.consumption;
      this.selectedPersons = data.persons;
      this.hasAddress = true;
      // this.isOpen = true;
      // this.isOpen = false;
      // this.toggleDiv();
    } else {
      this.hasAddress = false;
    }
    this.isOpen = false;
    this.addressForm = this.fb.group({
      postalCode: ['', [Validators.required, Validators.pattern(/^\d{5}$/)]],

      // city: [{ value: '', disabled: true }, Validators.required],
      city: [{ value: null, disabled: true }, Validators.required],

      street: [{ value: null, disabled: true }, Validators.required],

      houseNumber: [
        { value: '', disabled: true },
        [Validators.required, Validators.maxLength(6), Validators.pattern(/^[a-zA-Z0-9\s\/]*$/)],
      ],
      consum: [null, [Validators.required, Validators.min(1)]],
    });

    this.handlePostalCodeChanges();
    this.handleCityChanges();
    this.handleStreetChanges();

    // if (this.isLoggedIn()) {
    //   this.authService.fetchCustomer();
    // }

    // this.authService.getCustomerData().subscribe((data) => {

    //   if (!data?.address) return;
    //   this.hasAddress = true;
    //   this.isOpen = false;
    //   // this.toggleDiv();
    //   const saved = data.address;
    const saved = this.authService.getAddressData();
    if (saved) {
      console.log('Prefill Address:', saved);

      this.isRestoring = true;

      this.addressForm.patchValue({
        postalCode: saved.zip,
        consum: saved.consumption,
      });

      // this.selectedPersons = saved.persons;
      // this.consum = saved.consumption;

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

        this.addressForm.get('city')?.setValue(matchedCity.city_id);

        this.isStreetLoading = true;
        this.addressForm.get('street')?.enable();

        this.addressService.getStreetsByCity(saved.zip, matchedCity.city).subscribe((streets) => {
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

          this.addressForm.get('houseNumber')?.enable();

          this.addressForm.patchValue(
            {
              houseNumber: saved.houseNumber,
            },
            { emitEvent: false },
          );
        });
        this.showCityDropdown = false;
        this.showDropdown = false;

        // this.isRestoring = false;
      });
    }
    // });
  }

  onCityInput(event: any) {
    if (this.addressForm.get('city')?.disabled) return;
    this.closeAllDropdowns();
    const value = event.target.value.trim().toLowerCase();
    this.citySearch = value;

    this.filteredCityOptions = this.cityOptions.filter((c) => c.city.toLowerCase().includes(value));

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
  goBack() {
    this.router.navigate(['/home/electricity']);
  }

  searchMode() {
    this.isEditMode = true;
  }

  searchApply() {
    console.log('Search Apply');
    this.isEditMode = false;
    this.addressForm.get('consum')?.setValue(this.consum);
    if (this.addressForm.invalid) {
      this.addressForm.markAllAsTouched();
      console.log('form not valid');
      return;
    }

    const selectedCityId = this.addressForm.value.city || this.lastValidCity?.city_id;
    console.log('Selected City ID:', selectedCityId);

    const selectedCityObj =
      this.cityOptions.find((c) => c.city_id === selectedCityId) || this.lastValidCity;
    if (!selectedCityObj) {
      console.error('Selected city not found');
      return; // or show error
    }

    const data = {
      zip: this.addressForm.value.postalCode,
      city: selectedCityObj.city,
      city_id: selectedCityObj.city_id,
      street: this.addressForm.value.street,
      houseNumber: this.addressForm.value.houseNumber,
      persons: this.selectedPersons,
      consumption: this.addressForm.value.consum,
    };
    this.zip = data.zip;
    this.city = data.city;
    this.street = data.street;
    this.houseNumber = data.houseNumber;
    this.selectedPersons = data.persons;

    this.authService.setAddressData(data);
    this.hasAddress = true;
    this.cdr.detectChanges();
    console.log('Address Data Set:', data);
    // this.fetchRates();
    // this.isOpen = true;
  }

  closeAllDropdowns() {
    this.showCityDropdown = false;
    this.showDropdown = false;
  }

  trackByStreet(index: number, item: any) {
    return item.street;
  }

  onCustomPersonsChange(value: string) {
    const persons = Number(value);
    if (!persons || persons < 1) {
      this.consum = 0;
      this.updateConsumptionUI();
      return;
    }
    this.customPersonsValue = persons;
    this.customPersons = persons;
    this.selectedPersons = persons;
    this.consum = this.calculateConsumption(persons);
    this.addressForm.get('consum')?.setValue(this.consum);
    this.updateConsumptionUI();
  }

  calculateConsumption(persons: number): number {
    if (persons <= 3) {
      return this.baseConsumptions[persons] || 0;
    }

    const base = this.baseConsumptions[3] || 0;
    return base + (persons - 3) * this.extraPerPerson;
  }

  updateConsumptionUI() {
    const formatted = this.consum.toLocaleString();

    const el1 = document.getElementById('consumptionValue');
    if (el1) el1.innerText = formatted;

    const el2 = document.getElementById('consumptionValueMore');
    if (el2) el2.innerText = formatted;
  }

  filterStreets(event: any) {
    const value = event.target.value.toLowerCase();

    this.filteredStreetOptions = this.streetOptions.filter((s) =>
      s.street.toLowerCase().includes(value),
    );
  }

  private handlePostalCodeChanges() {
    this.addressForm
      .get('postalCode')
      ?.valueChanges.pipe(
        debounceTime(500),
        switchMap((zip) => {
          const isValidZip = /^\d{5}$/.test(zip);
          // if (this.isRestoring) return of([]);
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
          if (cities.length === 1) {
            const city = cities[0];

            this.citySearch = city.city;
            this.lastValidCity = city;

            this.addressForm.get('city')?.setValue(city.city_id, {
              emitEvent: true,
            });

            this.showCityDropdown = false;

            this.cdr.detectChanges();
          }
        } else {
          this.addressForm.get('city')?.disable();
        }
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
        const selectedCity = this.cityOptions.find((c) => c.city_id === placeId);
        if (!selectedCity) return;

        this.citySearch = selectedCity.city;

        this.addressService.getStreetsByCity(zip, selectedCity.city).subscribe((streets) => {
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

            this.showDropdown = true;
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
    this.lastValidCity = null;
    this.showCityDropdown = false;

    const control = this.addressForm.get('city');
    control?.reset(null, { emitEvent: false });
    control?.disable();
  }

  private resetStreet() {
    this.streetOptions = [];
    this.filteredStreetOptions = [];
    this.streetSearch = '';
    this.lastValidStreet = null;
    this.showDropdown = false;

    const control = this.addressForm.get('street');
    control?.reset(null, { emitEvent: false });
    control?.disable();
  }

  private resetHouseNumber() {
    this.addressForm.get('houseNumber')?.reset();
    this.addressForm.get('houseNumber')?.disable();
  }

  private fetchRates(): void {
    if (!this.hasAddress) {
      // this.isOpen = false;
      this.isOpen = !this.isOpen;
      this.isLoading = false;
      this.hasLoadedRates = false;
      this.filteredRates = [];

      alert('Please select an address before compare');
      this.cdr.detectChanges();
      return;
    }

    this.isLoading = true;
    this.hasLoadedRates = false;
    this.allRates = [];
    this.filteredRates = [];
    const customerId = this.authService.getUserId() || 0;

    const body = {
      zip: this.zip,
      city: this.city,
      street: this.street,
      houseNumber: this.houseNumber,
      // Country: '81',
      consum: this.consum,
      type: this.type,
      branch: this.branch,
      customerId: Number(customerId),
      adminId: 1,
    };

    this.http.post<RatesResponse>('http://192.168.0.155:8080/api/get-rates', body).subscribe({
      next: (res) => {
        if (!res?.res) {
          console.error('Invalid response');
          return;
        }

        const rates = res.rates?.result || [];
        const total = res.rates?.total || rates.length;
        const baseProviderData = res.baseProvider?.result?.[0] || null;

        this.allRates = rates
          .map((rate: Rate) => ({
            ...rate,
            uiExpanded: true,
            finalPrice: this.getDisplayPrice(rate),
          }))
          .sort((a, b) => b.finalPrice - a.finalPrice);

        this.totalCount = total;

        this.baseProvider = baseProviderData;
        this.baseRate = baseProviderData?.rates?.[0] || null;

        this.allRates.forEach((r) => {
          if (!this.activeTabMap[r.rateId]) {
            this.activeTabMap[r.rateId] = 'overview';
          }
        });

        this.applyFiltersAndSort();
        this.expandVisibleRates();

        // this.isDropdownOpen = true;
        this.hasLoadedRates = true;
        this.isLoading = false;

        this.persistViewState();

        this.baseProviders = res.baseProvider?.result || [];

        if (this.baseProviders.length > 0) {
          this.selectedProvider = this.baseProviders[0];

          this.selectedProviderId = this.selectedProvider.providerId;

          this.selectedProviderRates = this.selectedProvider.rates || [];

          if (this.selectedProviderRates.length > 0) {
            this.selectedRate = this.selectedProviderRates[0];

            this.selectedRateId = this.selectedRate.rateId;

            this.updateEditableValues();
          }
        }

        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('API Error:', err);
        this.isLoading = false;
        this.hasLoadedRates = true;
      },
    });
  }

  onProviderChange(): void {
    this.selectedProvider = this.baseProviders.find((p) => p.providerId == this.selectedProviderId);

    this.selectedProviderRates = this.selectedProvider?.rates || [];

    if (this.selectedProviderRates.length > 0) {
      this.selectedRate = this.selectedProviderRates[0];

      this.selectedRateId = this.selectedRate.rateId;

      this.updateEditableValues();
    }
  }

  applyComparisonPrice(): void {
    this.fieldErrors = {};
    // Arbeitspreis validation
    if (!this.editableWorkPrice || this.editableWorkPrice <= 0) {
      this.fieldErrors['workPrice'] = 'Der Mindestverbrauch beträgt 1 Cent/kWh';
    }

    // Grundpreis validation
    if (!this.editableBasePrice || this.editableBasePrice <= 0) {
      this.fieldErrors['basePrice'] = 'Bitte geben Sie einen gültigen Grundpreis ein';
    }

    // Abschlag validation
    if (
      this.activeTab === 'abschlag' &&
      (!this.editableMonthlyPrice || this.editableMonthlyPrice <= 0)
    ) {
      this.fieldErrors['monthlyPrice'] = 'Bitte geben Sie einen gültigen Abschlag ein';
    }

    // stop submit if errors
    if (Object.keys(this.fieldErrors).length > 0) {
      return;
    }

    if (!this.baseRate) return;

    // WORK PRICE
    if (this.isGrossPrice) {
      this.baseRate.workPrice = Number(this.editableWorkPrice.toFixed(2));
    } else {
      this.baseRate.workPrice = Number((this.editableWorkPrice * 1.19).toFixed(2));
    }

    // BASE PRICE
    if (this.basePriceMode === 'month') {
      this.baseRate.basePriceMonth = Number(this.editableBasePrice.toFixed(2));

      this.baseRate.basePriceYear = Number((this.editableBasePrice * 12).toFixed(2));
    } else {
      this.baseRate.basePriceYear = Number(this.editableBasePrice.toFixed(2));

      this.baseRate.basePriceMonth = Number((this.editableBasePrice / 12).toFixed(2));
    }

    // ABSCHLAG
    if (this.activeTab === 'abschlag') {
      this.baseRate.totalPriceMonth = Number(this.editableMonthlyPrice.toFixed(2));

      this.baseRate.totalPrice = Number((this.editableMonthlyPrice * 12).toFixed(2));
    }

    // refresh ui
    this.baseRate = {
      ...this.baseRate,
    };

    this.filteredRates = [...this.filteredRates];

    // save popup state
    this.savedActiveTab = this.activeTab;

    this.savedAbschlagCount = this.abschlagCount;

    this.savedEditableMonthlyPrice = this.editableMonthlyPrice;

    // save state
    this.originalBaseRate = {
      ...this.baseRate,
    };

    // close
    this.showEditSection = false;

    this.isModalOpen = false;
  }

  onAbschlagInputChange(value: number): void {
    this.baseAbschlagPrice = Number(value);
  }

  getAbschlagPrice(): number {
    // original monthly average
    const monthlyAverage = this.getPopupYearlyPrice() / 12;

    // scale by selected count
    const value = (monthlyAverage * this.abschlagCount) / 12;

    return Number(value.toFixed(2));
  }

  changeAbschlagCount(count: number): void {
    this.abschlagCount = count;

    this.editableMonthlyPrice = Number(((this.baseAbschlagPrice * count) / 12).toFixed(2));
  }

  getPopupYearlyPrice(): number {
    const yearlyBasePrice =
      this.basePriceMode === 'month' ? this.editableBasePrice * 12 : this.editableBasePrice;

    let bruttoWorkPrice = this.editableWorkPrice;

    // netto -> brutto
    if (!this.isGrossPrice) {
      bruttoWorkPrice = this.editableWorkPrice * 1.19;
    }

    const yearlyWorkPrice = (this.consum * bruttoWorkPrice) / 100;

    const yearlyTotal = yearlyBasePrice + yearlyWorkPrice;

    return Number(yearlyTotal.toFixed(2));
  }
  // ======================
  // RATE CHANGE
  // ======================

  onRateChange(): void {
    this.selectedRate = this.selectedProviderRates.find((r) => r.rateId == this.selectedRateId);

    this.updateEditableValues();
  }

  // ======================
  // GROSS / NET
  // ======================

  setGrossMode(isGross: boolean): void {
    // current displayed value
    let currentPrice = Number(this.editableWorkPrice);

    // brutto -> netto
    if (!isGross && this.isGrossPrice) {
      currentPrice = currentPrice / 1.19;
    }

    // netto -> brutto
    if (isGross && !this.isGrossPrice) {
      currentPrice = currentPrice * 1.19;
    }

    this.isGrossPrice = isGross;

    this.editableWorkPrice = Number(currentPrice.toFixed(2));
  }

  // ======================
  // BASE PRICE MODE
  // ======================

  changeBasePriceMode(mode: 'month' | 'year'): void {
    // month -> year
    if (this.basePriceMode === 'month' && mode === 'year') {
      this.editableBasePrice = Number((this.editableBasePrice * 12).toFixed(2));
    }

    // year -> month
    if (this.basePriceMode === 'year' && mode === 'month') {
      this.editableBasePrice = Number((this.editableBasePrice / 12).toFixed(2));
    }

    this.basePriceMode = mode;
  }
  onBasePriceInputChange(): void {
    this.editableBasePrice = Number(this.editableBasePrice.toFixed(2));
  }
  // ======================
  // UPDATE INPUT VALUES
  // ======================

  updateEditableValues(): void {
    if (!this.selectedRate) return;

    // API workPrice = brutto
    if (this.isGrossPrice) {
      this.editableWorkPrice = this.selectedRate.workPrice;
    } else {
      // brutto -> netto
      this.editableWorkPrice = this.selectedRate.workPrice / 1.19;
    }

    // base price
    this.editableBasePrice =
      this.basePriceMode === 'month'
        ? this.selectedRate.basePriceMonth
        : this.selectedRate.basePriceYear;

    // average
    this.editableMonthlyPrice = this.getPopupAveragePrice();
  }

  // ======================
  // POPUP PRICE
  // ======================
  getPopupAveragePrice(): number {
    if (!this.selectedRate) return 0;

    // base price yearly
    const yearlyBasePrice =
      this.basePriceMode === 'month' ? this.editableBasePrice * 12 : this.editableBasePrice;

    let bruttoWorkPrice = this.editableWorkPrice;

    // netto -> brutto
    if (!this.isGrossPrice) {
      bruttoWorkPrice = this.editableWorkPrice * 1.19;
    }

    // yearly work price
    const yearlyWorkPrice = (this.consum * bruttoWorkPrice) / 100;

    // yearly total
    const yearlyTotal = yearlyWorkPrice + yearlyBasePrice;

    // result
    const finalPrice = this.priceDisplayMonthly ? yearlyTotal / 12 : yearlyTotal;

    return Number(finalPrice.toFixed(2));
  }

  applyFiltersAndSort(): void {
    let rates = [...this.allRates];

    if (!this.alleTarife) {
      rates = rates.filter((r) => r.optEco);
    }

    if (!this.maxTermEgal) {
      const max = this.maxTerm12 ? 12 : this.maxTerm24 ? 24 : Infinity;
      rates = rates.filter((r) => (parseInt(r.optTerm, 10) || 0) <= max);
    }

    if (!this.minGuaranteeEgal) {
      const min = this.minGuarantee24 ? 24 : this.minGuarantee12 ? 12 : this.minGuarantee6 ? 6 : 0;
      rates = rates.filter((r) => (parseInt(r.optGuarantee, 10) || 0) >= min);
    }

    switch (this.selectedOption) {
      case 'Niedrigster Preis':
        rates.sort((a, b) => a.totalPrice - b.totalPrice);
        break;
      case 'Niedrigster Arbeitspreis':
        rates.sort((a, b) => a.workPrice - b.workPrice);
        break;
      case 'Niedrigster Grundpreis':
        rates.sort((a, b) => a.basePriceYear - b.basePriceYear);
        break;
      default:
        rates.sort(
          (a, b) =>
            (b.recommended ? 1 : 0) - (a.recommended ? 1 : 0) || a.totalPrice - b.totalPrice,
        );
    }

    this.filteredRates = rates;
    this.persistViewState();
  }

  private expandVisibleRates(): void {
    this.filteredRates.forEach((rate) => {
      rate.uiExpanded = true;
    });
  }

  toggleDiv(): void {
    console.log('isopen value', this.isOpen);
    if (!this.isOpen) {
      this.fetchRates();
    }
    this.isOpen = !this.isOpen;
    this.persistViewState();
  }

  openPage(selectedRate: Rate): void {
    this.authService.setSelectedProvider(selectedRate);
    this.authService.setAllProviders(this.allRates);

    this.router.navigate(['register'], { relativeTo: this.route });
    this.cdr.detectChanges();
    // const customerId = this.authService.getUserId() || 0;

    // const body = {
    //   zip: this.zip,
    //   city: this.city,
    //   street: this.street,
    //   houseNumber: this.houseNumber,
    //   deliveryType: this.branch == 'electric' ? 'electricity' : this.branch,
    //   customerId: Number(customerId),
    //   adminId: 1,
    // };

    // this.http
    //   .post<RatesResponse>('http://192.168.0.155:8080/customer/check-booking', body)
    //   .subscribe({
    //     next: (res) => {
    //       if (res?.res === true) {
    //         this.authService.setSelectedProvider(selectedRate);
    //         this.router.navigate(['register'], { relativeTo: this.route });
    //       } else {
    //         alert('Für diese Adresse besteht bereits ein aktiver Stromvertrag.');
    //       }

    //       this.cdr.detectChanges();
    //     },

    //     error: (err) => {
    //       console.error('API Error:', err);
    //       this.isLoading = false;
    //       this.hasLoadedRates = true;

    //       // alert('Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.');
    //     },
    //   });
  }

  toggleDropdown(): void {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  selectOption(option: string): void {
    this.selectedOption = option;
    this.isDropdownOpen = false;
    this.applyFiltersAndSort();
    this.expandVisibleRates();
    this.persistViewState();
  }

  onFilterChange(): void {
    this.persistViewState();
    if (this.allRates.length) {
      this.applyFiltersAndSort();
      this.expandVisibleRates();
    }
  }

  toggleCard(rate: Rate): void {
    rate.uiExpanded = !rate.uiExpanded;

    if (rate.uiExpanded && !this.activeTabMap[rate.rateId]) {
      this.activeTabMap[rate.rateId] = 'overview';
    }
  }

  setTab(rateId: number, tab: string): void {
    this.activeTabMap[rateId] = tab;
  }

  getActiveTab(rateId: number): string {
    return this.activeTabMap[rateId] ?? 'overview';
  }

  getDisplayPrice(rate: Rate): number {
    if (!rate) return 0;

    const commissionTotal = this.getCommissionTotal(rate);

    let basePrice = 0;

    // monthly
    if (this.priceDisplayMonthly) {
      if (rate.totalPriceMonth) {
        basePrice = rate.totalPriceMonth;
      } else {
        basePrice = this.getYearlyPrice(rate) / 12;
      }

      return Number((basePrice + commissionTotal).toFixed(2));
    }

    // yearly
    if (rate.totalPrice) {
      basePrice = rate.totalPrice;
    } else {
      basePrice = this.getYearlyPrice(rate);
    }

    return Number((basePrice + commissionTotal).toFixed(2));
  }

  private getCommissionTotal(rate: Rate): number {
    if (!rate?.commission?.length) return 0;

    const today = new Date();

    return rate.commission.reduce((sum, c) => {
      if (!c.amount) return sum;

      // check validity window if exists
      if (c.validFrom && new Date(c.validFrom) > today) return sum;
      if (c.validTo && new Date(c.validTo) < today) return sum;

      // optional: filter by order state
      if (c.requiredOrderState && c.requiredOrderState !== 'completed') {
        return sum;
      }

      return sum + Number(c.amount);
    }, 0);
  }
  getBasePrice(rate: Rate): number {
    if (!rate) return 0;

    const yearlyTotal = rate.basePriceYear + (this.consum * rate.workPrice) / 100;

    return this.priceDisplayMonthly
      ? yearlyTotal / 12 // monthly
      : yearlyTotal; // yearly
  }

  getAveragePrice(rate: Rate): number {
    if (!rate) return 0;

    // priority edited values
    if (this.priceDisplayMonthly && rate.totalPriceMonth) {
      return Number(rate.totalPriceMonth.toFixed(2));
    }

    if (!this.priceDisplayMonthly && rate.totalPrice) {
      return Number(rate.totalPrice.toFixed(2));
    }

    const yearlyWorkPrice = (this.consum * rate.workPrice) / 100;

    const yearlyTotal = yearlyWorkPrice + rate.basePriceYear;

    return this.priceDisplayMonthly
      ? Number((yearlyTotal / 12).toFixed(2))
      : Number(yearlyTotal.toFixed(2));
  }

  getYearlyPrice(rate: Rate): number {
    if (!rate) return 0;

    // edited yearly value
    if (rate.totalPrice) {
      return Number(rate.totalPrice.toFixed(2));
    }
    const commissionTotal = this.getCommissionTotal(rate);

    const yearlyWorkPrice = (this.consum * rate.workPrice) / 100;

    return Number((yearlyWorkPrice + rate.basePriceYear).toFixed(2));
  }

  getSavingPerYear(rate: Rate): number {
    if (!rate) return 0;
    const commissionTotalYearly = this.getCommissionTotal(rate) * 12;

    const baseYearly =
      this.adjustedBaseYearlyPrice > 0
        ? this.adjustedBaseYearlyPrice
        : this.getYearlyPrice(this.baseRate);

    console.log(
      'Base Yearly:',
      baseYearly,
      'Rate Yearly:',
      this.getYearlyPrice(rate),
      'Commission:',
      commissionTotalYearly,
    );

    return Number((baseYearly - (this.getYearlyPrice(rate) + commissionTotalYearly)).toFixed(2));
  }

  getDisplayPriceSuffix(): string {
    return this.priceDisplayMonthly ? 'Ø pro Monat' : 'pro Jahr';
  }

  getWorkPriceAnnual(rate: Rate): number {
    return (rate.workPrice * this.consum) / 100;
  }

  getGuaranteeLabel(rate: Rate): string {
    const months = parseInt(rate.optGuarantee, 10) || 0;
    if (!months) return '–';
    const typeMap: Record<string, string> = {
      energyPrice: 'Energiepreisgarantie',
      limitedEnergyPrice: 'Nettopreisgarantie',
      totalPrice: 'Gesamtpreisgarantie',
    };
    return `${months} Monate ${typeMap[rate.optGuaranteeType] ?? 'Preisgarantie'}`;
  }

  getTermLabel(rate: Rate): string {
    const months = parseInt(rate.optTerm, 10) || 0;
    return months ? `${months} Monate` : 'Monatlich kündbar';
  }

  getCancelLabel(rate: Rate): string {
    if (!rate.cancel) return '–';
    const unit = rate.cancelType === 1 ? 'Monat' : 'Woche';
    return `${rate.cancel} ${unit}`;
  }

  getLowestPrice(): number {
    if (!this.filteredRates.length) return 0;
    return Math.min(
      ...this.filteredRates.map((r) =>
        this.priceDisplayMonthly ? r.totalPriceMonth : r.totalPrice,
      ),
    );
  }

  toggleInfo(event: MouseEvent): void {
    event.stopPropagation();
    this.isInfoOpen = !this.isInfoOpen;
  }

  openModal(): void {
    this.isModalOpen = true;

    // backup current saved state
    this.originalBaseRate = {
      ...this.baseRate,
    };

    this.originalEditableWorkPrice = this.editableWorkPrice;

    this.originalEditableBasePrice = this.editableBasePrice;

    this.originalIsGrossPrice = this.isGrossPrice;

    this.originalBasePriceMode = this.basePriceMode;

    // restore saved popup state
    this.activeTab = this.savedActiveTab;

    this.abschlagCount = this.savedAbschlagCount;

    // restore saved monthly value
    if (this.savedEditableMonthlyPrice > 0) {
      this.editableMonthlyPrice = this.savedEditableMonthlyPrice;
    } else {
      this.baseAbschlagPrice = this.getPopupAveragePrice();

      this.editableMonthlyPrice = this.baseAbschlagPrice;
    }
  }

  @ViewChild('abschlagScroll')
  abschlagScroll!: ElementRef;

  scrollAbschlag(direction: 'left' | 'right'): void {
    const container = this.abschlagScroll.nativeElement;

    const scrollAmount = 120;

    container.scrollBy({
      left: direction === 'right' ? scrollAmount : -scrollAmount,
      behavior: 'smooth',
    });
  }
  closeModal(): void {
    // restore original values
    if (this.originalBaseRate) {
      this.baseRate = {
        ...this.originalBaseRate,
      };
    }
    this.fieldErrors = {};
    this.editableWorkPrice = this.originalEditableWorkPrice;

    this.editableBasePrice = this.originalEditableBasePrice;

    this.isGrossPrice = this.originalIsGrossPrice;

    this.basePriceMode = this.originalBasePriceMode;

    // restore saved popup state
    this.activeTab = this.savedActiveTab;

    this.abschlagCount = this.savedAbschlagCount;

    this.editableMonthlyPrice = this.savedEditableMonthlyPrice;

    // refresh ui
    this.filteredRates = [...this.filteredRates];

    this.showEditSection = false;

    this.isModalOpen = false;
  }

  apply() {
    this.closeModal();
  }
  showEditSection = false;

  toggleEditSection() {
    this.showEditSection = !this.showEditSection;
    this.activeTab = 'price';
  }

  activeTab: 'price' | 'abschlag' = 'price';

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
    return this.streetOptions.some((s) => s.street === value);
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

  @HostListener('document:keydown.escape')
  handleEscape(): void {
    this.isInfoOpen = false;
    this.isDropdownOpen = false;
  }

  private isBrowser(): boolean {
    return typeof window !== 'undefined' && typeof localStorage !== 'undefined';
  }

  private persistViewState(): void {
    if (!this.isBrowser()) {
      return;
    }

    const state: SelectProviderState = {
      priceDisplayMonthly: this.priceDisplayMonthly,
      kundenPrivat: this.kundenPrivat,
      alleTarife: this.alleTarife,
      maxTermEgal: this.maxTermEgal,
      maxTerm24: this.maxTerm24,
      maxTerm12: this.maxTerm12,
      minGuaranteeEgal: this.minGuaranteeEgal,
      minGuarantee24: this.minGuarantee24,
      minGuarantee12: this.minGuarantee12,
      minGuarantee6: this.minGuarantee6,
      selectedOption: this.selectedOption,
      isOpen: this.isOpen,
    };

    try {
      localStorage.setItem(this.providerStateStorageKey, JSON.stringify(state));
    } catch (error) {
      console.error('Error saving select-provider state:', error);
    }
  }

  private restoreViewState(): void {
    if (!this.isBrowser()) {
      return;
    }

    try {
      const raw = localStorage.getItem(this.providerStateStorageKey);
      if (!raw) {
        return;
      }

      const state = JSON.parse(raw) as Partial<SelectProviderState>;
      this.priceDisplayMonthly = state.priceDisplayMonthly ?? this.priceDisplayMonthly;
      this.kundenPrivat = state.kundenPrivat ?? this.kundenPrivat;
      this.type = this.kundenPrivat ? 'private' : 'business';
      this.alleTarife = state.alleTarife ?? this.alleTarife;
      this.maxTermEgal = state.maxTermEgal ?? this.maxTermEgal;
      this.maxTerm24 = state.maxTerm24 ?? this.maxTerm24;
      this.maxTerm12 = state.maxTerm12 ?? this.maxTerm12;
      this.minGuaranteeEgal = state.minGuaranteeEgal ?? this.minGuaranteeEgal;
      this.minGuarantee24 = state.minGuarantee24 ?? this.minGuarantee24;
      this.minGuarantee12 = state.minGuarantee12 ?? this.minGuarantee12;
      this.minGuarantee6 = state.minGuarantee6 ?? this.minGuarantee6;
      this.selectedOption = state.selectedOption || this.selectedOption;
      this.isOpen = !!state.isOpen;
    } catch (error) {
      console.error('Error restoring select-provider state:', error);
    }
  }
}
