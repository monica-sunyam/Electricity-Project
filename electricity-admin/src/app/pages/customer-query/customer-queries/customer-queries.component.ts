import { Component, OnInit } from "@angular/core";
import { CommonModule, DatePipe, UpperCasePipe } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ApiService } from "../../../shared/services/api.service";
import { AuthService } from "../../../shared/services/auth.service";

export interface ChatMessage {
  message: string;
  chatUser: "CUSTOMER" | "ADMIN";
  sendOn: number;
}

export interface ServiceRequest {
  serviceRequestId: number;
  title: string;
  createdOn: number;
  isOpen: boolean;
  inProgress: boolean;
  isClosed: boolean;
  requestClosedOn: number | null;
  requestReopenedOn: number | null;
  serviceName: string | null;
  ticketNumber: string;
  fistName: string | null;
  lastName: string | null;
  email: string | null;
  messages?: ChatMessage[];
}

export interface ServiceRequestResponse {
  data: {
    all: ServiceRequest[];
    open: ServiceRequest[];
    inProgress: ServiceRequest[];
    closed: ServiceRequest[];
    totalPage: number;
    page: number;
  };
  res: boolean;
}

type StatusTab = "all" | "open" | "inProgress" | "closed";

@Component({
  selector: "app-customer-queries",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./customer-queries.component.html",
  styleUrl: "./customer-queries.component.css",
})
export class CustomerQueriesComponent implements OnInit {
  // ── Data ─────────────────────────────────────────────────────────────────
  allRequests: ServiceRequest[] = [];
  openRequests: ServiceRequest[] = [];
  inProgressRequests: ServiceRequest[] = [];
  closedRequests: ServiceRequest[] = [];

  // ── UI state ─────────────────────────────────────────────────────────────
  isLoading = false;
  errorMessage = "";
  expandedRow: number | null = null;
  closingId: number | null = null;
  isDarkMode = false;

  // ── Pagination ────────────────────────────────────────────────────────────
  currentPage = 1;
  totalPages = 1;
  private readonly PAGE_SIZE = 20;

  // ── Tab filter ────────────────────────────────────────────────────────────
  selectedTab: StatusTab = "all";

  get statusTabs(): { value: StatusTab; label: string; count: number | null }[] {
    return [
      { value: "all", label: "Alle", count: this.allRequests.length },
      { value: "open", label: "Offen", count: this.openRequests.length },
      { value: "inProgress", label: "In Bearbeitung", count: this.inProgressRequests.length },
      { value: "closed", label: "Geschlossen", count: this.closedRequests.length },
    ];
  }

  get filteredRequests(): ServiceRequest[] {
    switch (this.selectedTab) {
      case "open": return this.openRequests;
      case "inProgress": return this.inProgressRequests;
      case "closed": return this.closedRequests;
      default: return this.allRequests;
    }
  }

  // ── Chat panel ────────────────────────────────────────────────────────────
  chatOpen = false;
  activeChatRequest: ServiceRequest | null = null;
  chatMessages: ChatMessage[] = [];
  replyMessage = "";
  isSendingReply = false;

  // ── Confirm: Send Reply ───────────────────────────────────────────────────
  showSendConfirm = false;
  pendingReplyText = "";

  // ── Confirm: Close Ticket ─────────────────────────────────────────────────
  showCloseConfirm = false;
  pendingCloseRequest: ServiceRequest | null = null;

  constructor(
    private api: ApiService,
    private authService: AuthService,
  ) { }

  ngOnInit(): void {
    this.fetchRequests(1);
  }

  // ── Dropdown filter ───────────────────────────────────────────────────────
  dropdownOpen = false;

  toggleDropdown(): void {
    this.dropdownOpen = !this.dropdownOpen;
  }

  closeDropdown(): void {
    this.dropdownOpen = false;
  }

  // Update onTabChange to also close dropdown
  onTabChange(tab: StatusTab): void {
    this.selectedTab = tab;
    this.expandedRow = null;
    this.dropdownOpen = false; // close after selection
  }

  // ── Fetch ─────────────────────────────────────────────────────────────────
  fetchRequests(page: number = 1): void {
    this.currentPage = page;
    this.isLoading = true;
    this.errorMessage = "";
    this.expandedRow = null;

    const payload = {
      adminId: this.authService.getUserId(),
      page: this.currentPage,
      size: this.PAGE_SIZE,
    };

    this.api.post("admin/fetch-service-requests", payload).subscribe({
      next: (res: any) => {
        this.isLoading = false;
        const data = res?.data;
        if (!data) {
          this.errorMessage = "Ungültige Serverantwort.";
          return;
        }

        this.allRequests = Array.isArray(data.all) ? data.all : [];
        this.openRequests = Array.isArray(data.open) ? data.open : [];
        this.inProgressRequests = Array.isArray(data.inProgress) ? data.inProgress : [];
        this.closedRequests = Array.isArray(data.closed) ? data.closed : [];
        this.totalPages = data.totalPage ?? 1;
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = "Fehler beim Laden der Anfragen.";
      },
    });
  }

  // ── Row expand ────────────────────────────────────────────────────────────
  toggleRow(id: number): void {
    this.expandedRow = this.expandedRow === id ? null : id;
  }

  // ── Pagination ────────────────────────────────────────────────────────────
  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.fetchRequests(this.currentPage + 1);
    }
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.fetchRequests(this.currentPage - 1);
    }
  }

  // ── Close Query (with confirm) ────────────────────────────────────────────
  requestCloseQuery(request: ServiceRequest): void {
    this.pendingCloseRequest = request;
    this.showCloseConfirm = true;
  }

  cancelClose(): void {
    this.showCloseConfirm = false;
    this.pendingCloseRequest = null;
  }

  confirmClose(): void {
    if (!this.pendingCloseRequest || this.closingId !== null) return;

    const request = this.pendingCloseRequest;
    this.showCloseConfirm = false;
    this.pendingCloseRequest = null;
    this.closingId = request.serviceRequestId;

    const payload = {
      adminId: this.authService.getUserId(),
      serviceRequestId: request.serviceRequestId,
    };

    this.api.post("admin/close-service-request", payload).subscribe({
      next: () => {
        this.closingId = null;
        request.isClosed = true;
        request.isOpen = false;
        request.inProgress = false;
        request.requestClosedOn = Math.floor(Date.now() / 1000);
        this.fetchRequests(this.currentPage);
      },
      error: () => {
        this.closingId = null;
      },
    });
  }

  // ── Chat ──────────────────────────────────────────────────────────────────
  openChat(request: ServiceRequest): void {
    this.activeChatRequest = request;
    const full = this.allRequests.find(r => r.serviceRequestId === request.serviceRequestId);
    this.chatMessages = (full?.messages ?? request.messages ?? [])
      .slice()
      .sort((a, b) => a.sendOn - b.sendOn);
    this.replyMessage = "";
    this.chatOpen = true;
    this.showSendConfirm = false;
  }

  closeChat(): void {
    this.chatOpen = false;
    this.activeChatRequest = null;
    this.chatMessages = [];
    this.replyMessage = "";
    this.showSendConfirm = false;
  }

  onReplyEnter(event: Event): void {
    const ke = event as KeyboardEvent;
    if (!ke.shiftKey) {
      event.preventDefault();
      this.requestSendReply();
    }
  }

  // Step 1 — show confirm banner
  requestSendReply(): void {
    const text = this.replyMessage.trim();
    if (!text || this.isSendingReply || !this.activeChatRequest) return;
    this.pendingReplyText = text;
    this.showSendConfirm = true;
  }

  cancelSend(): void {
    this.showSendConfirm = false;
    this.pendingReplyText = "";
  }

  // Step 2 — actually send
  confirmSendReply(): void {
    if (!this.pendingReplyText || this.isSendingReply || !this.activeChatRequest) return;

    this.showSendConfirm = false;
    this.isSendingReply = true;

    const text = this.pendingReplyText;
    this.pendingReplyText = "";

    const payload = {
      adminId: this.authService.getUserId(),
      serviceRequestId: this.activeChatRequest.serviceRequestId,
      message: text.trim(),
    };

    this.api.post("admin/add-service-request-response", payload).subscribe({
      next: () => {
        const newMsg: ChatMessage = {
          message: text.trim(),
          chatUser: "ADMIN",
          sendOn: Math.floor(Date.now() / 1000),
        };
        this.chatMessages = [...this.chatMessages, newMsg];
        this.replyMessage = "";
        this.isSendingReply = false;
      },
      error: () => {
        this.isSendingReply = false;
      },
    });
  }
}