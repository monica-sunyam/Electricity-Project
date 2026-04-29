import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ApiService } from "../../../shared/services/api.service";
import { RouterModule } from "@angular/router";
import { AuthService } from "../../../shared/services/auth.service";

interface Holiday {
  id?: number;
  date: string;        // ISO yyyy-MM-dd
  name: string;
  type: "public" | "optional" | "restricted";
  rangeId?: string;    // shared key for back-to-back days in same range
}

@Component({
  selector: "app-holiday-marker",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: "./holiday-markers.component.html",
  styleUrl: "./holiday-markers.component.css",
})
export class HolidayMarkerComponent implements OnInit {
  /* ── Calendar state ───────────────────────────── */
  today = new Date();
  viewYear = this.today.getFullYear();
  viewMonth = this.today.getMonth();
  weeks: (Date | null)[][] = [];

  readonly MONTH_NAMES = [
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
  ];
  readonly DAY_LABELS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
  readonly HOLIDAY_TYPES: Holiday["type"][] = ["public", "optional", "restricted"];

  /* ── Holidays ─────────────────────────────────── */
  holidays: Holiday[] = [];
  isLoading = false;
  isSaving = false;
  errorMessage = "";
  successMessage = "";

  /* ── Range-selection state ────────────────────── */
  rangeStart: Date | null = null;
  hoverDate: Date | null = null;

  /* ── Modal ────────────────────────────────────── */
  showModal = false;
  modalStartDate: string | null = null;
  modalEndDate: string | null = null;
  modalHolidayName = "";
  modalHolidayType: Holiday["type"] = "public";
  editingRangeId: string | null = null;
  editingSingleId: number | null = null;

  constructor(
    private api: ApiService,
    private authService: AuthService
  ) { }

  ngOnInit() {
    this.buildCalendar();
    this.loadHolidays();
  }

  /* ── Calendar builder ─────────────────────────── */
  buildCalendar() {
    const firstDay = new Date(this.viewYear, this.viewMonth, 1).getDay();
    const daysInMonth = new Date(this.viewYear, this.viewMonth + 1, 0).getDate();
    const cells: (Date | null)[] = [
      ...Array(firstDay).fill(null),
      ...Array.from({ length: daysInMonth }, (_, i) =>
        new Date(this.viewYear, this.viewMonth, i + 1)
      ),
    ];
    while (cells.length % 7 !== 0) cells.push(null);
    this.weeks = [];
    for (let i = 0; i < cells.length; i += 7) this.weeks.push(cells.slice(i, i + 7));
  }

  prevMonth() {
    if (this.viewMonth === 0) { this.viewMonth = 11; this.viewYear--; }
    else this.viewMonth--;
    this.buildCalendar();
    this.cancelRange();
  }

  nextMonth() {
    if (this.viewMonth === 11) { this.viewMonth = 0; this.viewYear++; }
    else this.viewMonth++;
    this.buildCalendar();
    this.cancelRange();
  }

  /* ── ISO helpers ──────────────────────────────── */
  toIso(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, "0");
    const d = String(date.getDate()).padStart(2, "0");
    return `${y}-${m}-${d}`;
  }

  fromIso(iso: string): Date {
    const [y, m, d] = iso.split("-").map(Number);
    return new Date(y, m - 1, d);
  }

  datesBetween(start: Date, end: Date): string[] {
    const dates: string[] = [];
    const cur = new Date(start);
    while (cur <= end) {
      dates.push(this.toIso(cur));
      cur.setDate(cur.getDate() + 1);
    }
    return dates;
  }

  /* ── Date state helpers ───────────────────────── */
  getHolidayForDate(date: Date): Holiday | undefined {
    return this.holidays.find(h => h.date === this.toIso(date));
  }

  isToday(date: Date): boolean {
    return this.toIso(date) === this.toIso(this.today);
  }

  isInPreviewRange(date: Date): boolean {
    if (!this.rangeStart || !this.hoverDate) return false;
    const start = this.rangeStart <= this.hoverDate ? this.rangeStart : this.hoverDate;
    const end = this.rangeStart <= this.hoverDate ? this.hoverDate : this.rangeStart;
    return date >= start && date <= end;
  }

  isPreviewStart(date: Date): boolean {
    if (!this.rangeStart || !this.hoverDate) return false;
    const start = this.rangeStart <= this.hoverDate ? this.rangeStart : this.hoverDate;
    return this.toIso(date) === this.toIso(start);
  }

  isPreviewEnd(date: Date): boolean {
    if (!this.rangeStart || !this.hoverDate) return false;
    const end = this.rangeStart <= this.hoverDate ? this.hoverDate : this.rangeStart;
    return this.toIso(date) === this.toIso(end);
  }

  /* ── Click handler ────────────────────────────── */
  onDateClick(date: Date) {
    const holiday = this.getHolidayForDate(date);

    // Clicking an existing holiday with no active range selection → edit
    if (holiday && !this.rangeStart) {
      this.openEditModal(holiday, date);
      return;
    }

    // First click
    if (!this.rangeStart) {
      this.rangeStart = date;
      return;
    }

    // Second click → open modal
    const start = this.rangeStart <= date ? this.rangeStart : date;
    const end = this.rangeStart <= date ? date : this.rangeStart;
    this.rangeStart = null;
    this.hoverDate = null;
    this.openAddModal(start, end);
  }

  cancelRange() {
    this.rangeStart = null;
    this.hoverDate = null;
  }

  onDateHover(date: Date) {
    if (this.rangeStart) this.hoverDate = date;
  }

  /* ── Modal helpers ────────────────────────────── */
  openAddModal(start: Date, end: Date) {
    this.modalStartDate = this.toIso(start);
    this.modalEndDate = this.toIso(end);
    this.modalHolidayName = "";
    this.modalHolidayType = "public";
    this.editingRangeId = null;
    this.editingSingleId = null;
    this.errorMessage = "";
    this.showModal = true;
  }

  openEditModal(holiday: Holiday, date: Date) {
    this.modalHolidayName = holiday.name;
    this.modalHolidayType = holiday.type;
    this.errorMessage = "";

    if (holiday.rangeId) {
      const rangeDays = this.holidays
        .filter(h => h.rangeId === holiday.rangeId)
        .map(h => h.date)
        .sort();
      this.modalStartDate = rangeDays[0];
      this.modalEndDate = rangeDays[rangeDays.length - 1];
      this.editingRangeId = holiday.rangeId;
      this.editingSingleId = null;
    } else {
      this.modalStartDate = this.toIso(date);
      this.modalEndDate = this.toIso(date);
      this.editingRangeId = null;
      this.editingSingleId = holiday.id ?? null;
    }
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.modalStartDate = null;
    this.modalEndDate = null;
    this.editingRangeId = null;
    this.editingSingleId = null;
  }

  get modalDateLabel(): string {
    if (!this.modalStartDate || !this.modalEndDate) return "";
    if (this.modalStartDate === this.modalEndDate) return this.modalStartDate;
    return `${this.modalStartDate}  →  ${this.modalEndDate}`;
  }

  get modalDayCount(): number {
    if (!this.modalStartDate || !this.modalEndDate) return 0;
    return this.datesBetween(this.fromIso(this.modalStartDate), this.fromIso(this.modalEndDate)).length;
  }

  setHolidayType(t: string) {
    this.modalHolidayType = t as Holiday["type"];
  }

  /* ── Save ─────────────────────────────────────── */
  saveHoliday() {
    if (!this.modalHolidayName.trim()) {
      this.errorMessage = "Please enter a holiday name";
      return;
    }
    this.isSaving = true;
    this.errorMessage = "";

    const isRange = this.modalStartDate !== this.modalEndDate;
    const rangeId = this.editingRangeId ?? (isRange ? crypto.randomUUID() : undefined);

    const payload: any = {
      adminId: this.authService.getUserId(),
      startDate: this.modalStartDate,
      endDate: this.modalEndDate,
      name: this.modalHolidayName.trim(),
      type: this.modalHolidayType,
      rangeId: rangeId ?? null,
    };
    if (this.editingRangeId) payload.rangeId = this.editingRangeId;
    if (this.editingSingleId) payload.id = this.editingSingleId;

    this.api.post("admin/save-holiday", payload).subscribe({
      next: (res) => {
        this.isSaving = false;
        if (res?.res) {
          // Remove old
          if (this.editingRangeId) {
            this.holidays = this.holidays.filter(h => h.rangeId !== this.editingRangeId);
          } else if (this.editingSingleId) {
            this.holidays = this.holidays.filter(h => h.id !== this.editingSingleId);
          }
          // Insert new entries per day
          const dates = this.datesBetween(
            this.fromIso(this.modalStartDate!),
            this.fromIso(this.modalEndDate!)
          );
          const serverIds: number[] = res.data?.ids ?? [];
          dates.forEach((d, i) => {
            this.holidays.push({
              id: serverIds[i] ?? undefined,
              date: d,
              name: this.modalHolidayName.trim(),
              type: this.modalHolidayType,
              rangeId: isRange ? rangeId : undefined,
            });
          });

          this.showSuccess(
            this.editingRangeId || this.editingSingleId
              ? "Holiday updated!"
              : `${dates.length} day${dates.length > 1 ? "s" : ""} marked!`
          );
          this.closeModal();
        } else {
          this.errorMessage = res?.errorMessage || "Failed to save";
        }
      },
      error: () => {
        this.isSaving = false;
        this.errorMessage = "Something went wrong";
      },
    });
  }

  /* ── Delete ───────────────────────────────────── */
  deleteHoliday() {
    this.isSaving = true;
    const payload: any = { adminId: this.authService.getUserId() };
    if (this.editingRangeId) payload.rangeId = this.editingRangeId;
    if (this.editingSingleId) payload.id = this.editingSingleId;

    this.api.post("admin/delete-holiday", payload).subscribe({
      next: (res) => {
        this.isSaving = false;
        if (res?.res) {
          if (this.editingRangeId) {
            this.holidays = this.holidays.filter(h => h.rangeId !== this.editingRangeId);
          } else {
            this.holidays = this.holidays.filter(h => h.id !== this.editingSingleId);
          }
          this.showSuccess("Holiday removed");
          this.closeModal();
        } else {
          this.errorMessage = res?.errorMessage || "Failed to delete";
        }
      },
      error: () => {
        this.isSaving = false;
        this.errorMessage = "Something went wrong";
      },
    });
  }

  /* ── Styling helpers ──────────────────────────── */
  typeColor(type: Holiday["type"]): string {
    return { public: "bg-red-500", optional: "bg-amber-400", restricted: "bg-blue-400" }[type];
  }

  typeBadgeClass(type: Holiday["type"]): string {
    return {
      public: "bg-red-100 text-red-700 dark:bg-red-900/40 dark:text-red-300",
      optional: "bg-amber-100 text-amber-700 dark:bg-amber-900/40 dark:text-amber-300",
      restricted: "bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300",
    }[type];
  }

  /** Returns bg/rounding classes to visually connect range days */
  rangeCellBg(date: Date): string {
    const holiday = this.getHolidayForDate(date);
    if (!holiday?.rangeId) return "";

    const rangeDates = this.holidays
      .filter(h => h.rangeId === holiday.rangeId)
      .map(h => h.date).sort();

    const iso = this.toIso(date);
    const isFirst = iso === rangeDates[0];
    const isLast = iso === rangeDates[rangeDates.length - 1];

    const bg = {
      public: "bg-red-100 dark:bg-red-900/20",
      optional: "bg-amber-100 dark:bg-amber-900/20",
      restricted: "bg-blue-100 dark:bg-blue-900/20",
    }[holiday.type];

    if (isFirst && isLast) return "";                      // single day
    if (isFirst) return `${bg} rounded-r-none`;
    if (isLast) return `${bg} rounded-l-none`;
    return `${bg} rounded-none`;
  }

  /* ── Sidebar helpers ──────────────────────────── */
  loadHolidays() {
    this.isLoading = true;
    this.api.post("admin/fetch-holidays", {
      adminId: this.authService.getUserId(),
      year: this.viewYear,
    }).subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res?.res) {
          this.holidays = res.data.map((h: any): Holiday => ({
            id: h.id,
            date: h.date,
            name: h.name,
            type: h.type ?? "public",
            rangeId: h.rangeId ?? undefined,
          }));
        }
      },
      error: () => { this.isLoading = false; this.errorMessage = "Failed to load holidays"; },
    });
  }

  private showSuccess(msg: string) {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ""), 3000);
  }

  get sortedHolidays(): Holiday[] {
    return [...this.holidays].sort((a, b) => a.date.localeCompare(b.date));
  }

  get currentMonthHolidays(): Holiday[] {
    const prefix = `${this.viewYear}-${String(this.viewMonth + 1).padStart(2, "0")}`;
    const seen = new Set<string>();
    return this.sortedHolidays.filter(h => {
      if (!h.date.startsWith(prefix)) return false;
      if (h.rangeId) {
        if (seen.has(h.rangeId)) return false;
        seen.add(h.rangeId);
      }
      return true;
    });
  }

  getRangeDayCount(h: Holiday): number {
    if (!h.rangeId) return 1;
    return this.holidays.filter(x => x.rangeId === h.rangeId).length;
  }

  getRangeEndDate(h: Holiday): string | null {
    if (!h.rangeId) return null;
    const dates = this.holidays
      .filter(x => x.rangeId === h.rangeId)
      .map(x => x.date).sort();
    return dates[dates.length - 1] ?? null;
  }
}