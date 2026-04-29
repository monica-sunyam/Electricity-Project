import { ChangeDetectorRef, Component, ElementRef, HostListener, ViewChild } from '@angular/core';
import { ContactPerson } from '../../layout/contact-person/contact-person';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { Router } from 'express';
import { CommonModule } from '@angular/common';
import SignaturePad from 'signature_pad';
import {
  CountdownConfig,
  CountdownEvent,
  CountdownComponent,
  CountdownModule,
} from 'ngx-countdown';
import { environment } from '../../environments/environment';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { Inject, PLATFORM_ID } from '@angular/core';
import { QRCodeComponent } from 'angularx-qrcode';
import { isPlatformBrowser } from '@angular/common';


const API_BASE = 'http://192.168.0.155:8080';
interface Card {
  logo: string;
  title: string;
  deliveryId: number;
  date: string;
  data: {
    label: string;
    value: string;
    icon: string;
  }[];
}

@Component({
  selector: 'app-customer',
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    FormsModule,
    CountdownModule,
  QRCodeComponent
  ],
  templateUrl: './customer.html',
  styleUrl: './customer.css',
})
export class Customer {
   isBrowser = false;

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    private eRef: ElementRef,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }
  tabs: string[] = [
    'Persönliche Daten / Verträge',
    'Mein Haushalt',
    'Zählerstand melden',
    'Wechselinnerung',
    'Serviceanfragen',
    'Beratervollmacht',
    'Dokumentenarchiv',
    'Passwort zurücksetzen',
  ];

  /* ── Tab control ──────────────────────────────────────────────── */
  activeTab: number = 3;
  serviceTab: number = 1;
  documentTab: number = 0;
  /* ── Step control ──────────────────────────────────────────────── */
  currentStep: number = 1;
  /* ── Customer Type (PRIVATE/Business) ──────────────────────────────────────────────── */
  customerType: string = 'PRIVATE';

  fieldErrors: Record<string, string> = {};

  customerData: any = {
    id: null,
    name: '',
    email: '',
    phone: '',
    salutation: '',
    title: '',
    userType: '',
    companyName: '',
    isVerified: false,
    joinedOn: null,
    address: {
      zip: '',
      city: '',
      street: '',
      houseNumber: '',
    },
    deliveryDetails: [],
  };
  isLoading = true;
  isLoadingNewReq: boolean = false;
  isLoadingNewMsg: boolean = false;
  isLoadingReopen: boolean = false;

  setActiveTab(index: number) {
    this.activeTab = index;
    this.currentStep = 1;
    this.serviceTab = 1;
    this.documentTab = 0;
    this.selectedIndex = -1;
    this.resetForm();

    if (this.activeTab == 4) {
      this.fetchServiceCount();
    }
    if (this.activeTab == 5) {
      this.checkAttorneyStatus();
    }
    this.cdr.detectChanges();
  }

  nextStep(step: number) {
    this.currentStep = step;
    if (step === 2 && this.activeTab === 5) {
      setTimeout(() => {
        this.initSignature();
      });
    }

    this.isResendDisabled = true;
    if (step === 2 && this.activeTab === 7) {
      setTimeout(() => {
        this.isResendDisabled = true;
        this.resendSuccess = false;

        if (this.countdown) {
          this.countdown.restart();
        }
      }, 0);
    }
  }

  showLogoutModal: boolean = false;

  openLogoutModal() {
    this.showLogoutModal = true;
  }

  closeLogoutModal() {
    this.showLogoutModal = false;
  }

  logout() {
    this.showLogoutModal = false;

    console.log('Logged out');

    this.authService.logout();
  }

  ngOnInit(): void {
    this.fetchAllRequests();
    this.fetchServiceCount();
    this.fetchCustomer();
    this.fetchCards();
    this.fetchCategories('general');

    this.checkAttorneyStatus();
  }

  /*── Fetch customer details ──*/

  private fetchCustomer(): void {
    const customerId = this.authService.getUserId() || 0;

    const body = {
      id: Number(customerId),
    };

    this.http.post<any>(`${API_BASE}/customer/fetch-customer-detail`, body).subscribe({
      next: (res) => {
        if (!res?.res || !res?.data) {
          console.error('Invalid response');
          // this.isLoading = false;
          return;
        }

        const data = res.data;

        this.customerData = {
          id: data.id,
          name: `${data?.firstName || ''} ${data?.lastName || ''}`.trim(),
          firstName: data.firstName || '',
          lastName: data.lastName || '',
          email: data.email || '',
          phone: data.mobileNumber || '',
          salutation: data.salutation || '',
          title: data.title || '',
          userType: data.userType || '',
          companyName: data.companyName || '',
          isVerified: data.isVerified || false,
          joinedOn: data.joinedOn || null,

          address: {
            zip: data?.address?.zip || '',
            city: data?.address?.city || '',
            street: data?.address?.street || '',
            houseNumber: data?.address?.houseNumber || '',
          },

          deliveryDetails: data.deliveryDetails || [],
        };

        this.customerType = this.customerData.userType;
        console.log('customerData:', this.customerData);

        this.cdr.detectChanges();
        // this.isLoading = false;
      },
      error: (err) => {
        console.error('API Error:', err);
        // this.isLoading = false;
      },
    });
  }

  /* ════════════════════════════════════════════════════════════════════════════════════════════════*/
  selection: string = 'no';
    selectOption(value: string): void {
    this.selection = value;
    if (value !== 'yes') {
      
    }
    if (value !== 'no') {
     
    }
  }


  contracts = [
  {
    logo: 'assets/icons/Icons_energyprovider/eon.png',
       title: 'E.ON ÖkoStrom Extra 12',
    type: 'Strom | Hausstrom',
    meter: 'ZKH-31259147-122',
    name: 'Marie Mustermann',
    address: 'Musterstraße 29, 12345 Musterhausen',
    contractNumber: '0215/123456789',
    duration: '12 Monate',
    startDate: '19.04.2025',
    renewal: '19.04.2026',
    price: '26,80 Ct./kWh',
    basePrice: '14,90 €/Monat',
    monthly: '68,40 €',
    cancelDate: '18.04.2026'
  },
  {
     logo: 'assets/icons/Icons_energyprovider/vattenfall.png',
     title: 'Easy12 Gas',
    type: 'Gas',
    meter: 'ZKH-31259147-122',
    name: 'Marie Mustermann',
    address: 'Musterstraße 29, 12345 Musterhausen',
    contractNumber: '012455-64564564k1245',
    duration: '12 Monate',
    startDate: '28.03.2025',
    renewal: '28.03.2026',
    price: '11,72 Ct./kWh',
    basePrice: '21,90 €/Monat',
    monthly: '151,40 €',
    cancelDate: '27.04.2026'
  }
];

  /*── Service Section Start ──*/

  showList = true;
  showDetails = false;
  confirmationList = false;
  showDropdown = false;
  selectedIndex: number = -1; // -1 = Orange card selected by default
  selectedCategory: any = null;
  selectedDeliveryId: number = 0;
  title: string = '';
  inquiryText: string = '';
  categories: { serviceId: number; serviceName: string; serviceType: string }[] = [];
  messages: any[] = [];
  currentServiceRequestId: number = 0;
  newMessage: string = '';

  openCount: number = 0;
  closedCount: number = 0;
  progressCount: number = 0;

  openRequests: any[] = [];
  inProgressRequests: any[] = [];
  closedRequests: any[] = [];

  fetchAllRequests() {
    const payload = {
      id: this.authService.getUserId(),
    };

    this.isLoading = true;

    this.http.post<any>(`${API_BASE}/customer/fetch-all-requests`, payload).subscribe({
      next: (res) => {
        this.isLoading = false;

        if (!res?.res) {
          console.error('Invalid response');
          this.resetRequests();
          return;
        }

        this.openRequests = this.mapRequests(res.openRequests || []);
        this.inProgressRequests = this.mapRequests(res.inProgressRequets || []);
        this.closedRequests = this.mapRequests(res.closedRequests || []);
        this.cdr.detectChanges();

        console.log('All Requests:', res);
      },
      error: (err) => {
        this.isLoading = false;
        console.error('API Error:', err);
        this.resetRequests();
      },
    });
  }

  mapRequests(list: any[]) {
    return list.map((item: any) => ({
      title: item.title,
      category: item.serviceName,
      ticketNumber: item.ticketNumber,
      createdOn: item.createdOn,
      closedOn: item.requestClosedOn,
      serviceRequestId: item.serviceRequestId,

      // UI helpers
      date: this.formatDateOnly(item.createdOn),
      status: item.isClosed ? 'closed' : item.inProgress ? 'progress' : 'open',
    }));
  }

  resetRequests() {
    this.openRequests = [];
    this.inProgressRequests = [];
    this.closedRequests = [];
  }

  fetchServiceCount() {
    const payload = {
      id: this.authService.getUserId(),
    };

    this.http.post<any>(`${API_BASE}/customer/fetch-service-count`, payload).subscribe({
      next: (res) => {
        if (!res?.res) {
          console.error('Invalid response');
          return;
        }

        this.openCount = res.open ?? 0;
        this.closedCount = res.closed ?? 0;
        this.progressCount = res.progress ?? 0;
        this.cdr.detectChanges();

        console.log('Counts:', res);
      },
      error: (err) => {
        console.error('API Error:', err);
      },
    });
  }

  private fetchCategories(serviceType: string): void {
    this.isLoading = true;
    const body = {
      adminId: 1,
      serviceType: serviceType,
    };
    this.http.post<any>(`${API_BASE}/customer/fetch-cutomer-service`, body).subscribe({
      next: (res) => {
        if (!res?.res || !res?.data) {
          console.error('Invalid response');
          this.categories = [];
          this.isLoading = false;
          return;
        }

        // this.categories = res.data.map((item: any) => item.serviceName || '');

        this.categories = res.data.map((item: any) => ({
          serviceId: item.serviceId,
          serviceName: item.serviceName || '',
        }));

        console.log('categories:', this.categories);

        this.cdr.detectChanges();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('API Error:', err);
        this.categories = [];
        this.isLoading = false;
      },
    });
  }

  selectCard(index: number, deliveryId: number) {
    this.selectedIndex = index;
    this.selectedDeliveryId = deliveryId;
    this.selectedCategory = '';
    this.fetchCategories('delivery');
    this.cdr.detectChanges();
  }

  selectOrangeCard() {
    this.selectedIndex = -1;
    this.selectedDeliveryId = 0;
    this.selectedCategory = '';
    this.fetchCategories('general');
    this.cdr.detectChanges();
  }

  toggleDropdown(event: Event) {
    event.stopPropagation();
    this.showDropdown = !this.showDropdown;
  }

  selectCategory(item: any, event: Event) {
    event.stopPropagation();
    this.selectedCategory = item;
    this.showDropdown = false;
  }

  validateForm(): boolean {
    this.fieldErrors = {};

    let isValid = true;

    if (!this.title || !this.title.trim()) {
      this.fieldErrors['title'] = 'Bitte Titel eingeben';
      isValid = false;
    }

    if (!this.selectedCategory) {
      this.fieldErrors['category'] = 'Bitte Kategorie wählen';
      isValid = false;
    }

    if (!this.inquiryText || !this.inquiryText.trim()) {
      this.fieldErrors['inquiryText'] = 'Bitte Anfragetext eingeben';
      isValid = false;
    }

    return isValid;
  }

  submitRequest() {
    if (!this.validateForm()) return;

    const payload = {
      customerId: this.authService.getUserId(),
      title: this.title,
      serviceId: this.selectedCategory.serviceId,
      message: this.inquiryText,
      serviceRequestType: this.selectedIndex === -1 ? 'general' : 'delivery',
      deliveryId: this.selectedDeliveryId,
    };

    console.log('Final Payload:', payload);

    this.isLoading = true;

    this.http.post<any>(`${API_BASE}/customer/add-service-request`, payload).subscribe({
      next: (res) => {
        this.isLoading = false;

        if (res?.res === true) {
          console.log('Request submitted successfully');

          this.fetchAllRequests();
          this.fetchServiceCount();
          this.toggleService(2);

          this.resetForm();
          this.cdr.detectChanges();
        } else {
          console.error('Invalid response', res);
        }
      },
      error: (err) => {
        this.isLoading = false;
        console.error('API Error:', err);
      },
    });
  }

  resetForm() {
    this.title = '';
    this.selectedCategory = null;
    this.inquiryText = '';
    this.selectedIndex = -1;
    this.selectedDeliveryId = 0;
    this.clearPwdField();
    this.fieldErrors = {};
  }

  @ViewChild('dropdownContainer') dropdownContainer!: ElementRef;

  // Outside click listener
  @HostListener('document:click', ['$event'])
  clickOutside(event: Event) {
    if (
      this.showDropdown &&
      this.dropdownContainer &&
      !this.dropdownContainer.nativeElement.contains(event.target)
    ) {
      this.showDropdown = false;
    }
  }

  openDetails(serviceRequestId: number) {
    this.showList = false;
    this.showDetails = true;
    this.fetchMessages(serviceRequestId);
  }

  isClosed: boolean = false;
  chatCategory: string = '';
  chatTitle: string = '';
  requestClosedOn: number | null = null;
  createdOn: number | null = null;
  reopenReason: string = '';

  fetchMessages(serviceRequestId: number) {
    const payload = {
      serviceRequestId: serviceRequestId,
    };

    this.isLoading = true;

    this.http.post<any>(`${API_BASE}/customer/fetch-request-messages`, payload).subscribe({
      next: ({ res, data }) => {
        this.isLoading = false;

        if (res && data) {
          this.isClosed = data.isClosed;
          this.chatCategory = data.serviceName;
          this.chatTitle = data.title;
          this.createdOn = data.createdOn;
          this.currentServiceRequestId = serviceRequestId;
          this.requestClosedOn = data.requestClosedOn ?? null;
          this.messages = data.messages.map((item: any) => ({
            message: item.message.replace(/\n/g, '<br>'),
            type: item.chatUser === 'CUSTOMER' ? 'customer' : 'admin',
            title: `${
              item.chatUser === 'CUSTOMER'
                ? 'Ihre Nachricht an den Berater vom'
                : 'Antwort vom Berater'
            } • ${this.formatDate(item.sendOn)}`,
          }));

          this.cdr.detectChanges();
          console.log('messages:', this.messages);
        } else {
          this.messages = [];
        }
      },
      error: (err) => {
        this.isLoading = false;
        console.error('API Error:', err);
        this.messages = [];
      },
    });
  }

  sendMessage(serviceRequestId: number) {
    if (!this.newMessage || !this.newMessage.trim()) return;

    const payload = {
      serviceRequestId: serviceRequestId,
      customerId: this.authService.getUserId(),
      message: this.newMessage,
    };

    this.isLoading = true;

    this.http.post<any>(`${API_BASE}/customer/add-service-request`, payload).subscribe({
      next: (res) => {
        this.isLoading = false;

        if (res?.res === true) {
          this.messages.push({
            message: res.messageBody.replace(/\n/g, '<br>'),
            type: 'customer',
            title: `Ihre Nachricht an den Berater vom • ${this.formatDate(res.sendOn)}`,
          });

          this.newMessage = '';
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        this.isLoading = false;
        console.error('API Error:', err);
      },
    });
  }

  validateReopen(): boolean {
    this.fieldErrors = {};

    if (!this.reopenReason || !this.reopenReason.trim()) {
      this.fieldErrors['reopenReason'] = 'Bitte Grund eingeben';
      return false;
    }

    return true;
  }

  clearServiceId() {
    this.currentServiceRequestId = 0;
  }

  reOpenservice(serviceRequestId: number) {
    if (!this.validateReopen()) return;

    const payload = {
      serviceRequestId: serviceRequestId,
      customerId: this.authService.getUserId(),
      message: this.reopenReason,
    };

    this.isLoading = true;

    this.http.post<any>(`${API_BASE}/customer/add-service-request`, payload).subscribe({
      next: (res) => {
        this.isLoading = false;

        if (res?.res) {
          this.reopenReason = '';

          this.toggleService(2);
          this.clearServiceId();
          this.fetchServiceCount();
          this.cdr.detectChanges();

          console.log('Reopened successfully');
        }
      },
      error: (err) => {
        this.isLoading = false;
        console.error('API Error:', err);
      },
    });
  }

  formatDateOnly(timestamp: number): string {
    const date = new Date(timestamp * 1000);

    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();

    return `${day}.${month}.${year}`;
  }

  formatDate(timestamp: number): string {
    const date = new Date(timestamp * 1000);

    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();

    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${day}.${month}.${year} - ${hours}:${minutes} Uhr`;
  }

  toggleService(step: number) {
    this.serviceTab = step;

    this.showList = true;
    this.showDetails = false;
    this.confirmationList = false;
    this.resetForm();
  }

  backToList() {
    this.showList = true;
    this.showDetails = false;
  }

  confirmation(serviceRequestId: number) {
    this.showList = false;
    this.showDetails = false;
    this.confirmationList = true;
  }

  cards: any[] = [];

  private fetchCards(): void {
    const customerId = this.authService.getUserId() || 0;

    const body = {
      id: Number(customerId),
    };

    this.isLoading = true;

    this.http.post<any>(`${API_BASE}/customer/fetch-placed-deliveries`, body).subscribe({
      next: (res) => {
        if (!res?.res || !res?.delivery) {
          console.error('Invalid response');
          this.cards = [];
          this.isLoading = false;
          return;
        }

        this.cards = res.delivery.map((item: any) => {
          const address = item?.customerAddress;

          return {
            logo: item?.provider?.providerSVG || 'assets/default.png',

            title: item?.provider?.rateName || '',
            deliveryId: item?.deliveryId || 0,

            date: item?.deliveryDate
              ? new Date(item.deliveryDate * 1000).toLocaleDateString('de-DE')
              : '',

            data: [
              {
                label: 'Zählernummer:',
                value: item?.connection?.meterNumber || '',
                icon: 'meter',
              },
              {
                label: 'Adresse:',
                value: address
                  ? `${address.street || ''} ${address.houseNumber || ''}, ${address.zip || ''} ${address.city || ''}`
                  : '',
                icon: 'home',
              },
              {
                label: 'Stromtyp:',
                value: item?.provider?.branch || '',
                icon: 'current',
              },
              {
                label: 'Vertragsnummer:',
                value: item?.uniqueDeliveryId || '',
                icon: 'doc',
              },
            ],
          };
        });

        console.log('cards:', this.cards);

        this.cdr.detectChanges();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('API Error:', err);
        this.cards = [];
        this.isLoading = false;
      },
    });
  }

  normalizeIcon(icon: string): string {
    if (!icon) return 'meter';

    icon = icon.toLowerCase();

    if (icon.includes('meter')) return 'meter';
    if (icon.includes('home')) return 'home';
    if (icon.includes('current') || icon.includes('electric')) return 'current';
    if (icon.includes('doc')) return 'doc';

    return 'meter';
  }

  @ViewChild('scrollContainer', { static: false })
  scrollContainer!: ElementRef;

  scrollLeft() {
    this.scrollContainer.nativeElement.scrollBy({
      left: -330,
      behavior: 'smooth',
    });
  }

  scrollRight() {
    this.scrollContainer.nativeElement.scrollBy({
      left: 330,
      behavior: 'smooth',
    });
  }

  /*── Service Section End ──*/

  /* ════════════════════════════════════════════════════════════════════════════════════════════════*/

  /*── Power of Attorney Section Start ──*/
  /* ── Signature ─── */

  legalFirstName: string = '';
  legalLastName: string = '';
  placeAndDate: string = '';
  recordIsPresent: boolean = false;
  approvalStatus: string = '';
  attorneyCreatedOn: string = '';

  @ViewChild('canvas') canvasRef!: ElementRef<HTMLCanvasElement>;
  signaturePad!: SignaturePad;

  initSignature() {
    if (!this.canvasRef) return;

    const canvas = this.canvasRef.nativeElement;

    const ratio = Math.max(window.devicePixelRatio || 1, 1);
    canvas.width = canvas.offsetWidth * ratio;
    canvas.height = canvas.offsetHeight * ratio;
    canvas.getContext('2d')!.scale(ratio, ratio);

    this.signaturePad = new SignaturePad(canvas);
  }

  clear() {
    this.signaturePad.clear();
  }
  // ngAfterViewInit() {
  //   this.initSignature();
  // }

  // ngAfterViewChecked() {
  //   if (this.activeTab === 5 && this.currentStep === 2 && !this.signaturePad) {
  //     this.initSignature();
  //   }
  // }

  checkAttorneyStatus() {
    const customerId = this.authService.getUserId() || 0;
    const payload = { id: customerId };

    this.http.post<any>(`${API_BASE}/customer/check-attorny`, payload).subscribe({
      next: ({ res, recordIsPresent, approvalStatus, createdOn }) => {
        if (res) {
          this.recordIsPresent = recordIsPresent;
          this.approvalStatus = approvalStatus;
          this.attorneyCreatedOn = this.formatAttorneyDate(createdOn);

          if (this.activeTab === 5 && this.approvalStatus === 'PENDING' && this.recordIsPresent) {
            this.nextStep(3);
          }

          this.cdr.detectChanges();
          console.log('Attorney:', this.recordIsPresent);
          console.log('Status:', this.approvalStatus);
        }
      },
      error: (err) => {
        console.error('Check Attorney API Error:', err);
      },
    });
  }

  formatAttorneyDate(dateValue: any): string {
    if (!dateValue) return '';

    // 👇 convert seconds → milliseconds
    const date = new Date(Number(dateValue) * 1000);

    const formatter = new Intl.DateTimeFormat('de-DE', {
      timeZone: 'Europe/Berlin',
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });

    const parts = formatter.formatToParts(date);

    const get = (type: string) => parts.find((p) => p.type === type)?.value;

    return `${get('day')}.${get('month')}.${get('year')} um ${get('hour')}:${get('minute')} Uhr (MEZ)`;
  }

  revoke() {
    const customerId = this.authService.getUserId();

    if (!customerId) return;

    const payload = {
      id: customerId,
    };

    this.isLoading = true;

    this.http.post<any>(`${API_BASE}/customer/revoke-attorny`, payload).subscribe({
      next: ({ res, message }) => {
        this.isLoading = false;

        if (res) {
          console.log('Success:', message);

          this.nextStep(1);
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        this.isLoading = false;
        console.error('API Error:', err);
      },
    });
  }
  getSignatureFile(): File {
    const canvas = this.canvasRef.nativeElement;

    const tempCanvas = document.createElement('canvas');
    tempCanvas.width = canvas.width;
    tempCanvas.height = canvas.height;

    const ctx = tempCanvas.getContext('2d')!;

    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, tempCanvas.width, tempCanvas.height);

    ctx.drawImage(canvas, 0, 0);

    const dataUrl = tempCanvas.toDataURL('image/png');

    const byteString = atob(dataUrl.split(',')[1]);
    const mimeString = dataUrl.split(',')[0].split(':')[1].split(';')[0];

    const ab = new ArrayBuffer(byteString.length);
    const ia = new Uint8Array(ab);

    for (let i = 0; i < byteString.length; i++) {
      ia[i] = byteString.charCodeAt(i);
    }

    return new File([ab], 'signature.png', { type: mimeString });
  }

  submitAttorney() {
    this.fieldErrors = {};

    let valid = true;

    if (this.customerType === 'BUSINESS') {
      if (!this.legalFirstName?.trim()) {
        this.fieldErrors['legalFirstName'] = 'Vorname erforderlich';
        valid = false;
      }

      if (!this.legalLastName?.trim()) {
        this.fieldErrors['legalLastName'] = 'Nachname erforderlich';
        valid = false;
      }
    }

    if (!this.placeAndDate?.trim()) {
      this.fieldErrors['placeAndDate'] = 'Ort und Datum erforderlich';
      valid = false;
    }

    if (!this.signaturePad || this.signaturePad.isEmpty()) {
      this.fieldErrors['signature'] = 'Unterschrift erforderlich';
      valid = false;
    }

    if (!valid) return;
    const customerId = this.authService.getUserId() || 0;

    const payload = {
      adminId: 1,
      customerId: customerId,
      salutation: this.customerData.salutation,
      title: this.customerData.title,
      userType: this.customerType,
      firstName: this.customerData.firstName,
      lastName: this.customerData.lastName,
      zip: this.customerData.address?.zip,
      city: this.customerData.address?.city,
      street: this.customerData.address?.street,
      houseNumber: this.customerData.address?.houseNumber,
      placeAndDate: this.placeAndDate,
      companyName: this.customerData.companyName,
      legalRepresentativeFirstName: this.legalFirstName,
      legalRepresentativeLastName: this.legalLastName,
    };

    const formData = new FormData();

    formData.append('data', JSON.stringify(payload));

    const file = this.getSignatureFile();
    formData.append('file', file);

    this.isLoading = true;

    this.http.post<any>(`${API_BASE}/customer/add-attorny`, formData).subscribe({
      next: ({ res, message, createdOn }) => {
        this.isLoading = false;

        if (res) {
          console.log('Success:', message);
          console.log('Created On:', createdOn);

          this.nextStep(3);
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        this.isLoading = false;
        console.error('API Error:', err);
      },
    });
  }

  /*── Power of Attorney Section End ──*/

  /* ════════════════════════════════════════════════════════════════════════════════════════════════*/
  /* ──  Document Section Start ──*/
  orderDocuments = [
    {
      logo: 'assets/icons/Icons_energyprovider/eon.png',
      title: 'E.ON ÖkoStrom Extra 12',
      workPrice: '26,80',
      basePrice: '14,90',
      contractNumber: '0215/123456789',
      monthly: '68,40',
    },
    {
      logo: 'assets/icons/Icons_energyprovider/vattenfall.png',
      title: 'Strom XXL Extra 12',
      workPrice: '11,72',
      basePrice: '21,90',
      contractNumber: '012455-64564564k1245',
      monthly: '151,40',
    },
  ];

  attorneyDocuments = [
    {
      title: 'Beratervollmacht',
      subtitle: 'für Privatkunden',
      createdOn: 'Erstellt am 30.03.2026 umd 18:26 Uhr (MEZ)',
    },
    {
      title: '360° Beraterservice',
      subtitle: 'Beraterservicevollmacht für Privatkunden',
      createdOn: 'Erteilt am 09.04.2026 umd 17:04 Uhr (MEZ)',
    },
  ];

  cancelDocuments = [
    {
      logo: 'assets/icons/Icons_energyprovider/GruenWelt.png',
      title: 'Grünwelt ÖkoStrom 12',
      contractNumber: '0125/1789454784654',
      terminatedOn: '09.01.2026',
    },
  ];

  contractDocuments = [
    {
      logo: 'assets/icons/Icons_energyprovider/eon.png',
      title: 'E.ON ÖkoStrom Extra 12',
      workPrice: '26,80',
      basePrice: '14,90',
      contractNumber: '0215/123456789',
      monthly: '68,40',
    },
    {
      logo: 'assets/icons/Icons_energyprovider/vattenfall.png',
      title: 'Strom XXL Extra 12',
      workPrice: '11,72',
      basePrice: '21,90',
      contractNumber: '012455-64564564k1245',
      monthly: '151,40',
    },
  ];

  miscellaneousDocuments = [
    {
      title: 'Datenschutzbestimmungen',
      subtitle: 'für Privatkunden',
      createdOn: 'Erstellt am 30.03.2026 umd 18:26 Uhr (MEZ)',
      viewBtn: 'Bestimmungen ansehen',
      downloadBtn: 'Bestimmungen downloaden',
    },
    {
      title: 'Widerrufsbelehrung',
      subtitle: 'Beraterservicevollmacht für Privatkunden',
      createdOn: 'Erstellt am 09.04.2026 umd 17:04 Uhr (MEZ)',
      viewBtn: 'Belehrung ansehen ',
      downloadBtn: 'Vollmacht downloaden',
    },
  ];

  toggleDocument(step: number) {
    this.documentTab = step;
  }

  /*── Document Section End ──*/

  /* ════════════════════════════════════════════════════════════════════════════════════════════════*/
  /* ──  Reset Password Section Start ──*/
  /* ══════════════════════════════════════════════════════════════════
  STEP 1 — Password VALIDATION
  ══════════════════════════════════════════════════════════════════ */

  /* ── Password validation flags ──────────────────────────────────── */
  pw_length: boolean = false;
  pw_case: boolean = false;
  pw_special: boolean = false;
  pw_number: boolean = false;
  showPw: boolean = false;
  showOldPw: boolean = false;
  showRepPw: boolean = false;
  otpError: string = '';
  newPassword: string = '';
  oldPassword: string = '';
  confirmPassword: string = '';
  passwordMismatch: boolean = false;
  otpValue: string = '';
  otpInvalid = false;
  newOtp = false;
  isLoadingReset: boolean = false;
  apiError: string = '';
  resendSuccess: boolean = false;

  clearPwdField() {
    this.newPassword = '';
    this.oldPassword = '';
    this.confirmPassword = '';
    this.showPw = false;
    this.showOldPw = false;
    this.showRepPw = false;
    this.otpError = '';
    this.passwordMismatch = false;
  }

  validatePassword(password: string, repeat: string) {
    this.newPassword = password;

    this.pw_length = password.length >= 8 && password.length <= 50;
    this.pw_case = /[a-z]/.test(password) && /[A-Z]/.test(password);
    this.pw_special = /[!@\$%\^&\*\+#]/.test(password);
    this.pw_number = /[0-9]/.test(password);

    if (repeat.length > 0) {
      this.passwordMismatch = password !== repeat;
    } else {
      this.passwordMismatch = false;
    }
  }

  private isPasswordValid(): boolean {
    return this.pw_length && this.pw_case && this.pw_special && this.pw_number;
  }

  private validateStepReset(passwordRepeat: string): boolean {
    this.fieldErrors = {};
    let valid = true;

    if (!this.oldPassword) {
      this.fieldErrors['oldPassword'] = 'Ein altes Passwort wird benötigt.';
      valid = false;
    } else if (!this.isPasswordValid()) {
      this.fieldErrors['oldPassword'] = 'Passwort erfüllt nicht alle Anforderungen.';
      valid = false;
    }

    if (!this.newPassword) {
      this.fieldErrors['newPassword'] = 'Ein neues Passwort ist erforderlich.';
      valid = false;
    } else if (!this.isPasswordValid()) {
      this.fieldErrors['newPassword'] = 'Passwort erfüllt nicht alle Anforderungen.';
      valid = false;
    }

    if (this.newPassword !== passwordRepeat) {
      this.passwordMismatch = true;
      valid = false;
    }
    return valid;
  }

  resetPassword() {
    console.log('resetPassword called');
    this.apiError = '';

    const isValid = this.validateStepReset(this.confirmPassword);

    if (!isValid) {
      console.log('not valid');
      return;
    }

    if (!this.authService.getUserId()) {
      this.apiError = 'Session abgelaufen.';
      console.log('Session abgelaufen.');
      return;
    }

    this.isLoadingReset = true;

    this.http
      .post<{
        res: boolean;
        message: string;
        errMessage: string;
      }>(`${API_BASE}/auth/change-password-request`, {
        id: Number(this.authService.getUserId()),
        oldPassword: this.oldPassword,
        newPassword: this.newPassword,
        confirmPassword: this.confirmPassword,
        adminId: 1,
      })
      .subscribe({
        next: (res) => {
          this.isLoadingReset = false;

          if (res.res) {
            this.currentStep = 2;
            this.clearPwdField();
          } else {
            console.log('false going');
            console.log('error message', res.errMessage);

            this.apiError = res.errMessage || 'Error occurred';
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.isLoadingReset = false;
          this.apiError =
            err?.error?.message || 'Fehler beim Zurücksetzen. Bitte erneut versuchen.';
        },
      });
  }

  @ViewChild('countdown', { static: false }) private countdown!: CountdownComponent;

  config: CountdownConfig = {
    leftTime: environment.resendTimer,
    format: 'm:ss',
    demand: false,
  };

  isResendDisabled: boolean = true;

  handleEvent(event: CountdownEvent) {
    if (event.action === 'done') {
      this.isResendDisabled = false;
    }
  }

  /* ══════════════════════════════════════════════════════════════════
  STEP 2 — OTP INPUT HELPERS
  ══════════════════════════════════════════════════════════════════ */

  onOtpInput(event: Event, index: number) {
    const input = event.target as HTMLInputElement;
    const val = input.value.replace(/\D/g, '').slice(-1);
    input.value = val;
    this.collectOtp();
    this.otpError = '';

    if (val && index < 5) {
      const next = document.getElementById(`otp-${index + 1}`) as HTMLInputElement;
      if (next) next.focus();
    }
  }

  onOtpKeydown(event: KeyboardEvent, index: number) {
    if (event.key === 'Backspace') {
      const input = event.target as HTMLInputElement;
      if (!input.value && index > 0) {
        const prev = document.getElementById(`otp-${index - 1}`) as HTMLInputElement;
        if (prev) {
          prev.value = '';
          prev.focus();
        }
      }
      this.collectOtp();
    }
  }

  onOtpPaste(event: ClipboardEvent) {
    event.preventDefault();
    const pasted = event.clipboardData?.getData('text').replace(/\D/g, '').slice(0, 6) || '';
    pasted.split('').forEach((ch, i) => {
      const el = document.getElementById(`otp-${i}`) as HTMLInputElement;
      if (el) el.value = ch;
    });
    this.collectOtp();
    const last = document.getElementById(
      `otp-${Math.min(pasted.length - 1, 5)}`,
    ) as HTMLInputElement;
    if (last) last.focus();
  }

  private collectOtp() {
    let val = '';
    for (let i = 0; i < 6; i++) {
      const el = document.getElementById(`otp-${i}`) as HTMLInputElement;
      val += el ? el.value || '' : '';
    }
    this.otpValue = val;
  }

  verifyOtp() {
    this.collectOtp();
    if (this.otpValue.length < 6) {
      this.otpError = 'Bitte alle 6 Stellen eingeben.';
      return;
    }
    if (!this.authService.getUserId()) {
      this.otpError = 'Sitzung abgelaufen. Bitte neu registrieren.';
      return;
    }

    this.isLoading = true;
    this.otpError = '';

    this.http
      .post<{
        res: boolean;
        newOtp?: boolean;
        message: string;
      }>(`${API_BASE}/auth/verify-change-password`, {
        id: this.authService.getUserId(),
        otp: this.otpValue,
        adminId: 1,
      })
      .subscribe({
        next: (res) => {
          this.isLoading = false;
          if (res.res) {
            this.otpInvalid = false;

            this.nextStep(3);
            this.cdr.detectChanges();
          } else {
            this.otpError = 'Der eingegebene Code ist ungültig.';
            this.otpInvalid = true;
          }
          this.newOtp = !!res.newOtp;
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.isLoading = false;
          this.otpError =
            err?.error?.message || 'Code-Überprüfung fehlgeschlagen. Bitte erneut versuchen.';
        },
      });
  }

  resendOtp() {
    if (!this.authService.getUserId()) return;
    if (this.isResendDisabled) return;
    this.isResendDisabled = true;

    setTimeout(() => {
      this.countdown?.restart();
    });
    this.resendSuccess = false;
    this.otpError = '';

    this.http
      .post<{
        res: boolean;
        message: string;
      }>(`${API_BASE}/auth/resend-otp`, { id: this.authService.getUserId() })
      .subscribe({
        next: (res) => {
          this.resendSuccess = true;
          // Clear boxes
          for (let i = 0; i < 6; i++) {
            const el = document.getElementById(`otp-${i}`) as HTMLInputElement;
            if (el) el.value = '';
          }
          this.otpValue = '';

          setTimeout(() => (this.resendSuccess = false), 4000);
        },
        error: () => {
          this.otpError = 'Code konnte nicht gesendet werden. Bitte erneut versuchen.';
        },
      });
  }
  /* ──  Reset Password Section End ──*/
  /* ════════════════════════════════════════════════════════════════════════════════════════════════*/
}
