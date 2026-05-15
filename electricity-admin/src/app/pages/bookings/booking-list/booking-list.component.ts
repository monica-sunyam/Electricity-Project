import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ApiService } from "../../../shared/services/api.service";
import { AuthService } from "../../../shared/services/auth.service";
import { Router } from "@angular/router";
import { RouterModule } from "@angular/router";

type Address = {
  zip?: string | null;
  city?: string | null;
  street?: string | null;
  houseNumber?: string | null;
};

type BillingAddress = Address & {
  isDifferent?: boolean | null;
};

type ContactSchedule = {
  id?: number | null;
  dayOfWeek?: string | null;
  timeSlot?: string | null;
  description?: string | null;
};

type Connection = {
  id?: number | null;
  isMovingIn?: boolean | null;
  moveInDate?: number | null;
  submitLater?: boolean | null;
  meterNumber?: string | null;
  marketLocationId?: string | null;
  currentProvider?: string | null;
  autoCancellation?: boolean | null;
  alreadyCancelled?: boolean | null;
  selfCancellation?: boolean | null;
  delivery?: boolean | null;
  desiredDelivery?: number | null;
  customerNumber?: string | null;
};

type Provider = {
  rateId?: number | null;
  rateName?: string | null;
  providerId?: number | null;
  netzProviderId?: number | null;
  providerName?: string | null;
  providerSVG?: string | null;
  providerSVGPath?: string | null;
  consumption?: number | null;

  basePriceYear?: number | null;
  basePriceMonth?: number | null;
  workPrice?: number | null;
  totalPrice?: number | null;
  totalPriceMonth?: number | null;
  savingPerYear?: number | null;
  workPriceNt?: number | null;
  optBonus?: number | null;
  partialPayment?: number | null;

  optGuarantee?: string | null;
  optGuaranteeType?: string | null;
  optTerm?: string | null;
  rateChangeType?: string | null;

  cancel?: number | null;
  cancelType?: number | null;
  termBeforeNewType?: string | null;
  termBeforeNewMaxDate?: string | null;

  selfPayment?: boolean | null;
  requiredEmail?: boolean | null;
  optEco?: boolean | null;
  recommended?: boolean | null;
  commission?: number | null;

  branch?: string | null;
  type?: string | null;
};

type Payment = {
  id?: number | null;
  paymentMethod?: string | null;
  iban?: string | null;
  firstName?: string | null;
  lastName?: string | null;
  sepaConsent?: boolean | null;
};

type Customer = {
  id?: number | null;
  email?: string | null;
  firstName?: string | null;
  lastName?: string | null;
  userType?: string | null;
  title?: string | null;
  salutation?: string | null;
  companyName?: string | null;
  mobileNumber?: string | null;

  isVerified?: boolean | null;
  verifiedOn?: number | null;
  joinedOn?: number | null;
  isAcknowledged?: boolean | null;

  address?: Address | null;

  status?: boolean | null;
  isNotificationEnabled?: boolean | null;
  lexofficeNumber?: string | null;
};

type OrderDoc = {
  bookingDocId?: number | null;
  signedOriginalFileName?: string | null;
  signedFileUrl?: string | null;
  signedDocumentSubmitted?: boolean | null;
  addedOn?: number | null;
  deliveryId?: number | null;
  customer?: Customer | null;
};

type Order = {
  customerOrderId?: number | null;
  orderId?: number | null;
  orderStatus?: number | null;
  adminPlacedOrder?: boolean | null;
  adminOrderPlacedOn?: number | null;

  expiryOn?: number | null;
  isExpired?: boolean | null;

  isCancelled?: boolean | null;
  cancelledOn?: number | null;
  lastDateOfCancellation?: number | null;

  operationPeriod?: number | null;
  bookingDocId?: number | null;

  doc?: OrderDoc | null;
};

export type ApiBooking = {
  deliveryId?: number | null;
  uniqueDeliveryId?: string | null;

  email?: string | null;
  title?: string | null;
  firstName?: string | null;
  lastName?: string | null;
  mobile?: string | null;
  telephone?: string | null;

  dob?: number | null;
  persons?: number | null;
  consumption?: number | null;

  orderPlacedOn?: number | null;
  orderPlaced?: boolean | null;
  expiryOn?: number | null;

  notificationEnabled?: boolean | null;

  customerAddress?: Address | null;
  billingAddress?: BillingAddress | null;

  provider?: Provider | null;
  connection?: Connection | null;
  payment?: Payment | null;
  contactSchedule?: ContactSchedule | null;

  customer?: Customer | null;
  order?: Order | null;
};

@Component({
  selector: "app-booking-list",
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: "./booking-list.component.html",
  styleUrl: "./booking-list.component.css",
})
export class BookingListComponent implements OnInit {
  bookings: ApiBooking[] = [];
  isLoading = false;
  errorMessage = "";
  filter = 0;
  searchTerm = "";
  isFilterOpen = false;

  hasMoreData = true;
  currentPage = 1;
  private readonly PAGE_LIMIT = 20;

  filterOptions = [
    { value: 0, label: "Alle" },
    { value: 1, label: "Unvollständig" },
    { value: 2, label: "Ausstehend" },
    { value: 3, label: "Offene Bestellung" },
    { value: 4, label: "Bestellung erstellt" },
    { value: 5, label: "Dokument hochgeladen" },
    { value: 6, label: "Abgelaufen" },
  ];

  readonly dayLabels: Record<string, string> = {
    MONDAY: "Montag",
    TUESDAY: "Dienstag",
    WEDNESDAY: "Mittwoch",
    THURSDAY: "Donnerstag",
    FRIDAY: "Freitag",
    SATURDAY: "Samstag",
    SUNDAY: "Sonntag",
  };

  readonly timeLabels: Record<string, string> = {
    MORNING_08_11: "08–11 Uhr",
    AFTERNOON_11_14: "11–14 Uhr",
    AFTERNOON_14_17: "14–17 Uhr",
    EVENING_17_20: "17–20 Uhr",
  };

  constructor(
    private api: ApiService,
    private authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.fetchBookings();
  }

  /** Navigate to the dedicated booking detail page */
  openDetail(booking: ApiBooking): void {
    this.router.navigate(["bookings", booking.deliveryId]);
  }

  fetchBookings(page: number = 1): void {
    this.currentPage = page;
    const payload = {
      adminId: this.authService.getUserId(),
      page: this.currentPage,
      filter: this.filter,
      search: this.searchTerm,
    };
    this.isLoading = true;
    this.errorMessage = "";

    this.api.post("admin/fetch-deliveries", payload).subscribe({
      next: (res: any) => {
        this.isLoading = false;
        const newData = this.extractList(res);
        this.bookings = newData;
        this.hasMoreData = newData.length === this.PAGE_LIMIT;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = "Fehler beim Laden der Buchungsliste.";
        console.error("Booking list fetch error:", err);
      },
    });
  }

  onFilterChange(filterValue: number): void {
    this.filter = filterValue;
    this.isFilterOpen = false;
    this.currentPage = 1;
    this.fetchBookings(1);
  }

  onSearchChange(term: string): void {
    this.searchTerm = term;
    this.currentPage = 1;
    this.fetchBookings(1);
  }

  getSelectedFilterLabel(): string {
    return (
      this.filterOptions.find((f) => f.value === this.filter)?.label || "Alle"
    );
  }

  nextPage(): void {
    if (this.hasMoreData) {
      this.fetchBookings(this.currentPage + 1);
    }
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.fetchBookings(this.currentPage - 1);
    }
  }

  fullName(booking: ApiBooking): string {
    return [booking.title, booking.firstName, booking.lastName]
      .filter(Boolean)
      .join(" ");
  }

  dayLabel(key?: string | null): string {
    return key ? (this.dayLabels[key] ?? key) : "—";
  }

  timeLabel(key?: string | null): string {
    return key ? (this.timeLabels[key] ?? key) : "—";
  }

  formatDate(value?: number | string | null): string {
    if (value === null || value === undefined || value === "") return "—";

    const num = typeof value === "number" ? value : Number(value);
    if (Number.isNaN(num)) return String(value);

    const ms = num < 1_000_000_000_000 ? num * 1000 : num;

    return new Intl.DateTimeFormat("de-DE", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      hour12: true, // Enables 12-hour format
    }).format(new Date(ms));
  }

  private extractList(response: any): ApiBooking[] {
    if (Array.isArray(response?.data)) return response.data;
    if (Array.isArray(response?.bookings)) return response.bookings;
    if (Array.isArray(response)) return response;
    return [];
  }
}
