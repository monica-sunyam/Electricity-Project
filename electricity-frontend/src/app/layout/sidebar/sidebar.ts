import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-sidebar',
  imports: [
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    MatFormFieldModule,
    MatInputModule,
    CommonModule,
  ],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar implements OnChanges {
  /** Full provider/rate object from the parent (e.g. DeliveryAddress). */
  @Input() providerDetails: any = null;

  // ── Derived display values ──────────────────────────────────────────────

  /** Average monthly cost, e.g. "149,59 €" */
  avgMonthlyPrice: string = '–';

  /** Annual saving (positive = saving, negative = extra cost). */
  savingPerYear: string = '–';
  isSaving: boolean = false;

  /** Tariff / rate name */
  rateName: string = '–';

  /** Whether this is an eco tariff */
  isEco: boolean = false;

  /** Minimum contract term, e.g. "12 Monate" */
  minTerm: string = '–';

  /** Annual consumption in kWh used for the calculation */
  annualUsage: string = '–';

  /** Monthly base price with annual equivalent, e.g. "14,70 €/Monat (176,40 €/Jahr)" */
  basePriceDisplay: string = '–';

  /** Work price in Cent/kWh */
  workPriceDisplay: string = '–';

  /** New-customer bonus (optBonus) */
  bonusDisplay: string | null = null;

  /** Provider SVG markup (sanitised inline) */
  providerSVG: string = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['providerDetails']) {
      this.deriveDisplayValues();
    }
  }

  private deriveDisplayValues(): void {
    const p = this.providerDetails;
    if (!p) return;

    // Monthly price
    this.avgMonthlyPrice = this.formatEuro(p.totalPriceMonth);

    // Saving — the API stores negative saving as a negative number;
    // a positive saving means the user saves money.
    const saving = typeof p.savingPerYear === 'number' ? p.savingPerYear : 0;
    this.isSaving = saving > 0;
    this.savingPerYear = this.formatEuro(Math.abs(saving));

    // Tariff details
    this.rateName = p.rateName ?? '–';
    this.isEco = !!p.optEco;
    this.minTerm = p.optTerm ? `${p.optTerm} Monate` : '–';

    // Tariff overview — the API doesn't return kWh usage directly;
    // we back-calculate it from totalPrice and per-unit prices if needed.
    // Most comparison APIs include a "consumption" or "usage" field; fall back gracefully.
    const usage: number | undefined = p.consumption ?? p.annualUsage ?? p.kWh ?? undefined;
    this.annualUsage = usage != null ? `${this.formatNumber(usage)} kWh` : '2.500 kWh';

    // Base price
    const bpMonth = typeof p.basePriceMonth === 'number' ? p.basePriceMonth : 0;
    const bpYear = typeof p.basePriceYear === 'number' ? p.basePriceYear : bpMonth * 12;
    this.basePriceDisplay = `${this.formatEuro(bpMonth)}/Monat (${this.formatEuro(bpYear)}/Jahr)`;

    // Work price (Cent/kWh)
    this.workPriceDisplay =
      typeof p.workPrice === 'number' ? `${this.formatCent(p.workPrice)} Cent/kWh` : '–';

    // Bonus
    this.bonusDisplay =
      typeof p.optBonus === 'number' && p.optBonus > 0 ? this.formatEuro(p.optBonus) : null;

    // Provider logo SVG
    this.providerSVG = p.providerSVGPath ?? '';

    console.log(this.providerSVG);
  }

  // ── Formatting helpers ──────────────────────────────────────────────────

  private formatEuro(value: number): string {
    return (
      value.toLocaleString('de-DE', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      }) + ' €'
    );
  }

  private formatCent(value: number): string {
    return value.toLocaleString('de-DE', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });
  }

  private formatNumber(value: number): string {
    return value.toLocaleString('de-DE');
  }
}
