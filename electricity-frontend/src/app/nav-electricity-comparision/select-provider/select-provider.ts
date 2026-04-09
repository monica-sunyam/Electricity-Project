import { ChangeDetectorRef, Component, ElementRef, HostListener, OnInit, ViewChild } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { ContactPerson } from '../../layout/contact-person/contact-person';
import { NeedSupport } from '../../layout/need-support/need-support';

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
  uiExpanded?: boolean;
}

export interface RatesResponse {
  result: Rate[];
  total: number;
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
  ],
  templateUrl: './select-provider.html',
  styleUrl: './select-provider.css',
})
export class SelectProvider implements OnInit {
  zip = '01067';
  city = 'Dresden';
  street = 'Adlergasse';
  houseNumber = '6';
  consum = 2500;
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

  selectedOption = 'Sortieren nach: Beste Treffer';
  activeTabMap: { [rateId: number]: string } = {};
  @ViewChild('popoverContainer', { static: false }) popoverContainer!: ElementRef;
 

  constructor(
    private cdr: ChangeDetectorRef,
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute,
    public dialog: MatDialog,
    private eRef: ElementRef,
  ) {}

  ngOnInit(): void {}

  private fetchRates(): void {
    this.isLoading = true;
    this.hasLoadedRates = false;
    this.allRates = [];
    this.filteredRates = [];

    const body = {
      zip: this.zip,
      city: this.city,
      street: this.street,
      houseNumber: this.houseNumber,
      Country: '81',
      consum: this.consum,
      type: this.type,
      branch: this.branch,
    };

    this.http.post<RatesResponse>('http://192.168.0.155:8080/get-rates', body).subscribe({
      next: (res) => {
        this.allRates = (res?.result ?? []).map((rate) => ({
          ...rate,
          uiExpanded: true,
        }));
        this.totalCount = res?.total ?? this.allRates.length;

        this.allRates.forEach((r) => {
          if (!this.activeTabMap[r.rateId]) {
            this.activeTabMap[r.rateId] = 'overview';
          }
        });

        this.applyFiltersAndSort();

        /* expand AFTER filtering */
        this.expandVisibleRates();

        /* force dropdown open */
        this.isDropdownOpen = true;
        this.hasLoadedRates = true;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('API Error:', err);
        this.isLoading = false;
        this.hasLoadedRates = true;
      },
    });
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
  }

  private expandVisibleRates(): void {
    this.filteredRates.forEach((rate) => {
      rate.uiExpanded = true;
    });
  }

  toggleDiv(): void {
    if (!this.isOpen) {
      this.fetchRates();
    }
    this.isOpen = !this.isOpen;
  }

  openPage(): void {
    this.router.navigate(['login'], { relativeTo: this.route });
  }

  toggleDropdown(): void {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  selectOption(option: string): void {
    this.selectedOption = option;
    this.isDropdownOpen = false;
    this.applyFiltersAndSort();
    this.expandVisibleRates();
  }

  onFilterChange(): void {
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
    return this.priceDisplayMonthly ? rate.totalPriceMonth : rate.totalPrice;
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

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.isInfoOpen) return;
    if (!this.popoverContainer?.nativeElement.contains(event.target)) {
      this.isInfoOpen = false;
    }
  }

  @HostListener('document:keydown.escape')
  handleEscape(): void {
    this.isInfoOpen = false;
    this.isDropdownOpen = false;
  }
}
