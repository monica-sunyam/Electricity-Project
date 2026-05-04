import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ApiService } from "../../../shared/services/api.service";
import { AuthService } from "../../../shared/services/auth.service";
import { FormsModule } from "@angular/forms";

export interface PasswordHistory {
  email: string;
  changeRequestSubmittedOn: number;
  codeSendOn: number | null;
  codeVerifiedOn: number | null;
  passwordChangedOn: number | null;
  confirmationSendOn: number | null;
  otp: string | null;
  customerId: number;
  adminId: number | null;
}

export interface Attorney {
  attornyId: number;
  salutation: string | null;
  title: string | null;
  firstName: string | null;
  lastName: string | null;
  userType: string | null;
  zip: string | null;
  city: string | null;
  street: string | null;
  houseNumber: string | null;
  companyName: string | null;
  legalRepresentativeFirstName: string | null;
  legalRepresentativeLastName: string | null;
  uniqueAttornyId: string;
  submittedOn: number;
  approvalStatus: number; // 0 = pending, 1 = approved, 2 = rejected
  approvedOn: number | null;
  rejectedOn: number | null;
  isRevoked: boolean;
  revokedOn: number | null;
  customerSignaturePath: string | null;
  placeAndDate: string | null;
  isProcessing?: boolean;
}

export type AdminCustomer = {
  id: number | string;
  email: string | null;
  firstName: string | null;
  lastName: string | null;
  mobileNumber: string | null;
  telephone?: string | null;
  userType: string | null;
  title: string | null;
  salutation: string | null;
  companyName: string | null;
  isVerified: boolean;
  verifiedOn: number | null;
  isAcknowledged: boolean;
  joinedOn: number;
  uniqueCustomerId: string;
  status: boolean;
  changePasswordHistory: PasswordHistory[];
  address: {
    id?: number;
    zip?: string;
    city?: string;
    street?: string;
    houseNumber?: string;
    customerId?: number | null;
  } | null;
  attornies: Attorney[];
};

@Component({
  selector: "app-customer-list",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./customer-list.component.html",
  styleUrl: "./customer-list.component.css",
})
export class CustomerListComponent implements OnInit {
  customers: AdminCustomer[] = [];
  isLoading = false;
  errorMessage = "";

  isNoteModalOpen = false;
  noteCustomer: AdminCustomer | null = null;
  noteText = "";
  isSavingNote = false;

  /** Stores one admin note per customer id */
  customerNotes: Record<string | number, string> = {};

  /** Currently selected customer shown in the sidebar */
  selectedCustomer: AdminCustomer | null = null;

  hasMoreData = true;
  private readonly PAGE_LIMIT = 20;
  currentPage = 1;
  totalPage: number | null = null;

  searchTerm: string = "";
  selectedUserType: string = "";
  selectedVerifiedStatus: string = "";

  /**
   * Tracks GDPR contact status per customer id.
   * Key: customer.id — Value: boolean (true = GDPR contact enabled)
   */
  gdprContactStatus: Record<string | number, boolean> = {};

  /**
   * Tracks in-flight GDPR API calls per customer id
   * to prevent double-clicks.
   */
  gdprLoading: Record<string | number, boolean> = {};

  constructor(
    private api: ApiService,
    private authService: AuthService,
  ) { }

  ngOnInit(): void {
    this.fetchCustomers();
  }

  onFilterChange(): void {
    this.fetchCustomers(1);
  }

  /** Open sidebar with selected customer */
  selectCustomer(customer: AdminCustomer): void {
    this.selectedCustomer =
      this.selectedCustomer?.id === customer.id ? null : customer;
  }

  /** Close the sidebar */
  closeSidebar(): void {
    this.selectedCustomer = null;
  }

  /** Open note modal for a customer */
  openNoteModal(event: Event, customer: AdminCustomer): void {
    event.stopPropagation();
    this.noteCustomer = customer;
    this.noteText = this.customerNotes[customer.id] ?? "";
    this.isNoteModalOpen = true;
  }

  /** Close note modal and reset form state */
  closeNoteModal(): void {
    this.isNoteModalOpen = false;
    this.noteCustomer = null;
    this.noteText = "";
    this.isSavingNote = false;
  }

  /** Save note for current customer */
  saveCustomerNote(): void {
    if (!this.noteCustomer || this.isSavingNote) return;

    const trimmedNote = this.noteText.trim();
    if (!trimmedNote) return;

    this.isSavingNote = true;

    const payload = {
      adminId: this.authService.getUserId(),
      customerId: this.noteCustomer.id,
      note: trimmedNote,
    };

    this.api.post("admin/add-customer-note", payload).subscribe({
      next: () => {
        this.customerNotes[this.noteCustomer!.id] = trimmedNote;
        this.closeNoteModal();
      },
      error: () => {
        // Keep modal open so admin can retry without losing note text.
        this.isSavingNote = false;
      },
    });
  }

  fetchCustomers(page: number = 1): void {
    this.currentPage = page;
    const payload = {
      adminId: this.authService.getUserId(),
      page: this.currentPage,
      search: this.searchTerm,
      userType: this.selectedUserType,
      isVerified: this.selectedVerifiedStatus,
    };

    this.isLoading = true;
    this.errorMessage = "";
    this.selectedCustomer = null;

    this.api.post("admin/fetch-customer-details", payload).subscribe({
      next: (res: any) => {
        this.isLoading = false;
        const newData = this.extractList(res);
        this.customers = newData;
        this.hasMoreData = newData.length === this.PAGE_LIMIT;
        this.totalPage = res?.totalPage ?? null;

        // Initialise GDPR state for any new customers (preserve existing toggles)
        newData.forEach((c) => {
          if (!(c.id in this.gdprContactStatus)) {
            this.gdprContactStatus[c.id] = false;
          }

          if (!(c.id in this.customerNotes)) {
            this.customerNotes[c.id] = "";
          }
        });
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = "Fehler beim Laden der Kundenliste.";
      },
    });
  }

  nextPage(): void {
    if (this.hasMoreData) {
      this.fetchCustomers(this.currentPage + 1);
    }
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.fetchCustomers(this.currentPage - 1);
    }
  }

  /**
   * Toggle GDPR contact permission for a customer.
   * Calls the mock endpoint admin/update-gdpr-contact-status.
   * Stops row-click propagation so the sidebar does not open/close.
   */
  toggleGdprContact(event: Event, customer: AdminCustomer): void {
    event.stopPropagation();

    if (this.gdprLoading[customer.id]) return;

    const newStatus = !this.gdprContactStatus[customer.id];
    this.gdprLoading[customer.id] = true;

    const payload = {
      adminId: this.authService.getUserId(),
      customerId: customer.id,
      gdprContactAllowed: newStatus,
    };

    this.api.post("admin/update-gdpr-contact-status", payload).subscribe({
      next: () => {
        this.gdprContactStatus[customer.id] = newStatus;
        this.gdprLoading[customer.id] = false;
      },
      error: () => {
        // Revert optimistic update on error
        this.gdprLoading[customer.id] = false;
      },
    });
  }

  /** Approve an attorney */
  approveAttorney(customer: AdminCustomer, attorney: Attorney): void {
    if (attorney.isProcessing) return;
    attorney.isProcessing = true;

    const payload = {
      adminId: this.authService.getUserId(),
      customerId: customer.id,
      attornyId: attorney.attornyId,
      status: 1,
    };

    this.api.post("admin/update-attorny-status", payload).subscribe({
      next: () => {
        attorney.approvalStatus = 1;
        attorney.approvedOn = Math.floor(Date.now() / 1000);
        attorney.isProcessing = false;
      },
      error: () => {
        attorney.isProcessing = false;
      },
    });
  }

  /** Reject an attorney */
  rejectAttorney(customer: AdminCustomer, attorney: Attorney): void {
    if (attorney.isProcessing) return;
    attorney.isProcessing = true;

    const payload = {
      adminId: this.authService.getUserId(),
      customerId: customer.id,
      attornyId: attorney.attornyId,
      status: 2,
    };

    this.api.post("admin/update-attorny-status", payload).subscribe({
      next: () => {
        attorney.approvalStatus = 2;
        attorney.rejectedOn = Math.floor(Date.now() / 1000);
        attorney.isProcessing = false;
      },
      error: () => {
        attorney.isProcessing = false;
      },
    });
  }

  /** Safely construct full name */
  fullName(customer: AdminCustomer): string {
    const first = (customer.firstName || "").trim();
    const last = (customer.lastName || "").trim();
    const value = [first, last].filter(Boolean).join(" ").trim();
    return value || "Keine Angabe";
  }

  /** Private mapper to ensure the UI data structure is consistent */
  private extractList(response: any): AdminCustomer[] {
    const list = Array.isArray(response?.data) ? response.data : [];

    return list.map((item: any) => ({
      id: item.id,
      email: item.email ?? null,
      firstName: item.firstName ?? null,
      lastName: item.lastName ?? null,
      mobileNumber: item.mobileNumber ?? null,
      telephone: item.telephone ?? null,
      userType: item.userType ?? "PRIVATE",
      title: item.title ?? "",
      salutation: item.salutation ?? "",
      companyName: item.companyName ?? null,
      isVerified: !!item.isVerified,
      verifiedOn: item.verifiedOn ?? null,
      isAcknowledged: !!item.isAcknowledged,
      joinedOn: item.joinedOn ?? 0,
      uniqueCustomerId: item.uniqueCustomerId ?? "-",
      status: !!item.status,
      changePasswordHistory: Array.isArray(item.changePasswordHistory)
        ? item.changePasswordHistory
        : [],
      address: item.address ? { ...item.address } : null,
      attornies: Array.isArray(item.attornies)
        ? item.attornies.map((a: any) => ({ ...a, isProcessing: false }))
        : [],
    }));
  }
}