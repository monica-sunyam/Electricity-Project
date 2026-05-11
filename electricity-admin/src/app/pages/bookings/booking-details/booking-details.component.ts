import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ActivatedRoute, Router, RouterModule } from "@angular/router";
import { ApiService } from "../../../shared/services/api.service";
import { AuthService } from "../../../shared/services/auth.service";
import { ApiBooking } from "../booking-list/booking-list.component";

type OrderInfo = {
  customerOrderId?: number | null;
  orderId?: number | null;
  orderStatus?: number | null;
  adminPlacedOrder?: boolean | null;
  adminOrderPlacedOn?: number | null;
  expiryOn?: number | null;
  lastDateOfCancellation?: number | null;
  bookingDocId?: string | null;
};

type ApiBookingWithOrder = ApiBooking & { order?: OrderInfo | null };

@Component({
  selector: "app-booking-detail",
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: "./booking-details.component.html",
  styleUrl: "./booking-details.component.css",
})
export class BookingDetailComponent implements OnInit {
  booking: ApiBookingWithOrder | null = null;
  isLoading = false;
  errorMessage = "";

  isPlacingOrder = false;
  orderPlacedMessage = "";
  orderPlacedNumber: number | null = null;
  orderError = "";

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
    private route: ActivatedRoute,
    private router: Router,
    private api: ApiService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    const deliveryId = this.route.snapshot.paramMap.get("id");
    if (deliveryId) {
      this.fetchBooking(Number(deliveryId));
    } else {
      this.errorMessage = "Keine Buchungs-ID angegeben.";
    }
  }

  fetchBooking(deliveryId: number): void {
    this.isLoading = true;
    this.errorMessage = "";
    const payload = { adminId: this.authService.getUserId(), deliveryId };
    this.api.post("admin/fetch-deliveries", payload).subscribe({
      next: (res: any) => {
        this.isLoading = false;
        this.booking = this.extractBooking(res);
        if (!this.booking) this.errorMessage = "Buchung nicht gefunden.";
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = "Fehler beim Laden der Buchung.";
        console.error("Booking detail fetch error:", err);
      },
    });
  }

  /** True when the order object exists and has a non-null orderId */
  get hasOrderId(): boolean {
    return !!this.booking?.order?.orderId;
  }

  placeOrder(): void {
    if (!this.booking?.order?.orderId) return;
    this.isPlacingOrder = true;
    this.orderError = "";
    this.orderPlacedMessage = "";
    this.orderPlacedNumber = null;

    const payload = {
      customerOrderId: this.booking.order.orderId,
      adminId: this.authService.getUserId(),
    };

    this.api.post("admin/place-order", payload).subscribe({
      next: (res: any) => {
        this.isPlacingOrder = false;
        if (res?.res) {
          this.orderPlacedMessage =
            res.message ?? "Bestellung erfolgreich aufgegeben.";
          this.orderPlacedNumber = res["Order no"] ?? null;
          // Reflect the placed state locally without a full refetch
          if (this.booking?.order) {
            this.booking.order.adminPlacedOrder = true;
            this.booking.order.adminOrderPlacedOn = Math.floor(
              Date.now() / 1000,
            );
          }
          if (this.booking) this.booking.orderPlaced = true;
        } else {
          this.orderError =
            res?.message ?? "Unbekannter Fehler beim Aufgeben der Bestellung.";
        }
      },
      error: (err) => {
        this.isPlacingOrder = false;
        this.orderError = "Fehler beim Aufgeben der Bestellung.";
        console.error("Place order error:", err);
      },
    });
  }

  editBooking(): void {
    if (!this.booking) return;
    this.router.navigate(["bookings/change", this.booking.deliveryId, "edit"], {
      state: { booking: this.booking },
    });
  }

  changeProvider(): void {
    if (!this.booking) return;
    this.router.navigate(
      ["bookings", this.booking.deliveryId, "change-provider"],
      { state: { booking: this.booking } },
    );
  }

  goBack(): void {
    this.router.navigate(["/bookings"]);
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  fullName(): string {
    if (!this.booking) return "";
    return [this.booking.title, this.booking.firstName, this.booking.lastName]
      .filter(Boolean)
      .join(" ");
  }

  initials(): string {
    if (!this.booking) return "?";
    const f = this.booking.firstName?.[0] ?? "";
    const l = this.booking.lastName?.[0] ?? "";
    return (f + l).toUpperCase() || "?";
  }

  formatAddress(
    addr?: {
      zip?: string;
      city?: string;
      street?: string;
      houseNumber?: string;
    } | null,
  ): string {
    if (!addr) return "—";
    return `${addr.street ?? ""} ${addr.houseNumber ?? ""}, ${addr.zip ?? ""} ${addr.city ?? ""}`.trim();
  }

  dayLabel(key?: string | null): string {
    return key ? (this.dayLabels[key] ?? key) : "—";
  }

  timeLabel(key?: string | null): string {
    return key ? (this.timeLabels[key] ?? key) : "—";
  }

  formatIban(iban?: string | null): string {
    if (!iban) return "—";
    return iban.replace(/(.{4})/g, "$1 ").trim();
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

  formatDateTime(value?: number | string | null): string {
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
    }).format(new Date(ms));
  }

  private extractBooking(response: any): ApiBookingWithOrder | null {
    if (response?.data && !Array.isArray(response.data)) return response.data;
    if (response?.booking) return response.booking;
    const list: ApiBookingWithOrder[] = Array.isArray(response?.data)
      ? response.data
      : Array.isArray(response?.bookings)
        ? response.bookings
        : Array.isArray(response)
          ? response
          : [];
    return list[0] ?? null;
  }
}
