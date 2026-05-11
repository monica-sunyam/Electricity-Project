import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ApiService } from "../../../shared/services/api.service";
import { AuthService } from "../../../shared/services/auth.service";
import { Router } from "@angular/router";
import { RouterModule } from "@angular/router";

type CustomerAddress = {
  zip?: string;
  city?: string;
  street?: string;
  houseNumber?: string;
};

type BillingAddress = CustomerAddress & {
  isDifferent?: boolean;
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
};

type Provider = {
  rateId?: number | null;
  rateName?: string | null;
  providerId?: number | null;
  providerName?: string | null;
  providerSVG?: string | null;
  basePriceYear?: number | null;
  basePriceMonth?: number | null;
  workPrice?: number | null;
  totalPrice?: number | null;
  totalPriceMonth?: number | null;
  branch?: string | null;
  optEco?: boolean | null;
};

type Payment = {
  id?: number | null;
  paymentMethod?: string | null;
  iban?: string | null;
  firstName?: string | null;
  lastName?: string | null;
  sepaConsent?: boolean | null;
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
  deliveryDate?: number | null;
  orderPlacedOn?: number | null;
  orderPlaced?: boolean | null;
  customerAddress?: CustomerAddress | null;
  billingAddress?: BillingAddress | null;
  provider?: Provider | null;
  connection?: Connection | null;
  payment?: Payment | null;
  contactSchedule?: ContactSchedule | null;
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

  hasMoreData = true;
  currentPage = 1;
  private readonly PAGE_LIMIT = 20;

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
    }).format(new Date(ms));
  }

  private extractList(response: any): ApiBooking[] {
    if (Array.isArray(response?.data)) return response.data;
    if (Array.isArray(response?.bookings)) return response.bookings;
    if (Array.isArray(response)) return response;
    return [];
  }
}
