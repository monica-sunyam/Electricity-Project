import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ApiService } from "../../shared/services/api.service";
import { AuthService } from "../../shared/services/auth.service";


// ── Nested types ────────────────────────────────────────────────

type ComparisonCustomer = {
  id?: number | null;
  email?: string | null;
  firstName?: string | null;
  lastName?: string | null;
  userType?: string | null;
  title?: string | null;
  salutation?: string | null;
};

type BaseRate = {
  rateId?: number | null;
  rateName?: string | null;
  workPrice?: number | null;
  workPriceNt?: number | null;
  basePriceYear?: number | null;
  basePriceMonth?: number | null;
};

type BaseProvider = {
  providerId?: number | null;
  providerName?: string | null;
  rates?: BaseRate[] | null;
};

type EnergyRate = {
  rateId?: number | null;
  rateName?: string | null;
  providerId?: number | null;
  providerName?: string | null;
  providerSVG?: string | null;
  providerSVGPath?: string | null;
  branch?: string | null;
  type?: string | null;
  workPrice?: number | null;
  workPriceNt?: number | null;
  basePriceYear?: number | null;
  basePriceMonth?: number | null;
  totalPrice?: number | null;
  totalPriceMonth?: number | null;
  savingPerYear?: number | null;
  optEco?: boolean | null;
  optBonus?: number | null;
  optTerm?: string | null;
  optGuarantee?: string | null;
  optGuaranteeType?: string | null;
  cancel?: number | null;
  recommended?: boolean | null;
  selfPayment?: boolean | null;
  requiredEmail?: boolean | null;
  partialPayment?: number | null;
  rateChangeType?: string[] | null;
  termBeforeNewType?: string | null;
  termBeforeNewMaxDate?: string | null;
  netzProviderId?: number | null;
};

type BaseProviderResponse = {
  result?: BaseProvider[] | null;
};

type EnergyRateResponse = {
  total?: number | null;
  result?: EnergyRate[] | null;
};

export type ComparisonEntry = {
  id?: number | null;
  zip?: string | null;
  city?: string | null;
  street?: string | null;
  houseNumber?: string | null;
  consumption?: string | null;
  consumerType?: string | null;
  branch?: string | null;
  customer?: ComparisonCustomer | null;
  comparedOn?: number | null;
  requestIp?: string | null;
  requestDeviceDetails?: string | null;
  baseProviderResponse?: BaseProviderResponse | null;
  energyRateResponse?: EnergyRateResponse | null;
  adminId?: number | null;
};

@Component({
  selector: "app-comparison-list",
  standalone: true,
  imports: [CommonModule],
  templateUrl: "./customer-comparison.component.html",
  styleUrl: "./customer-comparison.component.css",
})
export class ComparisonListComponent implements OnInit {
  comparisons: ComparisonEntry[] = [];
  isLoading = false;
  errorMessage = "";
  selectedEntry: ComparisonEntry | null = null;
  isSidebarOpen = false;
  activeRateTab: "base" | "results" = "results";

  hasMoreData = true;
  currentPage = 1;
  private readonly PAGE_LIMIT = 20;

  constructor(
    private api: ApiService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.fetchComparisons();
  }

  fetchComparisons(page: number = 1): void {
    this.currentPage = page;
    const payload = {
      adminId: this.authService.getUserId(),
      page: this.currentPage,
    };
    this.isLoading = true;
    this.errorMessage = "";
    this.closeSidebar();

    this.api.post("admin/fetch-customer-comparisons", payload).subscribe({
      next: (res: any) => {
        this.isLoading = false;
        const newData = this.extractList(res);
        this.comparisons = newData;
        this.hasMoreData = newData.length === this.PAGE_LIMIT;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = "Fehler beim Laden der Vergleichsliste.";
        console.error("Comparison list fetch error:", err);
      },
    });
  }

  nextPage(): void {
    if (this.hasMoreData) this.fetchComparisons(this.currentPage + 1);
  }

  prevPage(): void {
    if (this.currentPage > 1) this.fetchComparisons(this.currentPage - 1);
  }

  openSidebar(entry: ComparisonEntry): void {
    if (this.selectedEntry?.id === entry.id) {
      this.closeSidebar();
      return;
    }
    this.selectedEntry = entry;
    this.isSidebarOpen = true;
    this.activeRateTab = "results";
  }

  closeSidebar(): void {
    this.isSidebarOpen = false;
    this.selectedEntry = null;
  }

  isSelected(entry: ComparisonEntry): boolean {
    return this.selectedEntry?.id === entry.id;
  }

  setTab(tab: "base" | "results"): void {
    this.activeRateTab = tab;
  }

  // ── Customer helpers ────────────────────────────────────────

  isLoggedIn(entry: ComparisonEntry): boolean {
    return !!entry.customer?.id && entry.customer.id !== 0;
  }

  customerFullName(c?: ComparisonCustomer | null): string {
    if (!c) return "Gast";
    return [c.title, c.firstName, c.lastName].filter(Boolean).join(" ").trim() || "Gast";
  }

  customerInitials(c?: ComparisonCustomer | null): string {
    if (!c) return "G";
    const f = c.firstName?.[0] ?? "";
    const l = c.lastName?.[0] ?? "";
    return (f + l).toUpperCase() || "G";
  }

  // ── Device / IP helpers ─────────────────────────────────────

  parseDevice(raw?: string | null): string {
    if (!raw) return "—";
    // Format: "{os=Windows, device=Other, browser=Chrome}"
    return raw.replace(/[{}]/g, "").split(",").map(p => p.split("=")[1]?.trim()).filter(Boolean).join(" · ");
  }

  // ── Address helpers ─────────────────────────────────────────

  formatAddress(entry: ComparisonEntry): string {
    const parts = [
      entry.street && entry.houseNumber ? `${entry.street} ${entry.houseNumber}` : entry.street,
      entry.zip && entry.city ? `${entry.zip} ${entry.city}` : entry.city,
    ].filter(Boolean);
    return parts.join(", ") || "—";
  }

  // ── Provider helpers ────────────────────────────────────────

  baseProviders(entry: ComparisonEntry): BaseProvider[] {
    return entry.baseProviderResponse?.result ?? [];
  }

  energyRates(entry: ComparisonEntry): EnergyRate[] {
    return entry.energyRateResponse?.result ?? [];
  }

  rateCount(entry: ComparisonEntry): number {
    return entry.energyRateResponse?.total ?? entry.energyRateResponse?.result?.length ?? 0;
  }

  bestRate(entry: ComparisonEntry): EnergyRate | null {
    const rates = this.energyRates(entry);
    if (!rates.length) return null;
    return [...rates].sort((a, b) => (a.totalPrice ?? 9999) - (b.totalPrice ?? 9999))[0];
  }

  rateChangeTypeLabel(types?: string[] | null): string {
    if (!types?.length) return "—";
    const map: Record<string, string> = { NEW: "Neu", CHANGE: "Wechsel", MODIFICATION: "Änderung" };
    return types.map(t => map[t] ?? t).join(", ");
  }

  // ── Date helpers ────────────────────────────────────────────

  formatDate(value?: number | string | null): string {
    if (value === null || value === undefined || value === "") return "—";
    const num = typeof value === "number" ? value : Number(value);
    if (Number.isNaN(num)) return String(value);
    const ms = num < 1_000_000_000_000 ? num * 1000 : num;
    return new Intl.DateTimeFormat("de-DE", {
      day: "2-digit", month: "2-digit", year: "numeric",
    }).format(new Date(ms));
  }

  formatDateTime(value?: number | string | null): string {
    if (value === null || value === undefined || value === "") return "—";
    const num = typeof value === "number" ? value : Number(value);
    if (Number.isNaN(num)) return String(value);
    const ms = num < 1_000_000_000_000 ? num * 1000 : num;
    return new Intl.DateTimeFormat("de-DE", {
      day: "2-digit", month: "2-digit", year: "numeric",
      hour: "2-digit", minute: "2-digit",
    }).format(new Date(ms));
  }

  private extractList(response: any): ComparisonEntry[] {
    if (Array.isArray(response?.data)) return response.data;
    if (Array.isArray(response)) return response;
    return [];
  }
}