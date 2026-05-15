import { Routes } from "@angular/router";
import { DashboardComponent } from "./pages/dashboard/dashboard.component";
import { AppLayoutComponent } from "./shared/layout/app-layout/app-layout.component";
import { SignInComponent } from "./pages/sign-in/sign-in.component";
import { authGuard } from "./guards/auth.guard";
import { NavigationMenuListComponent } from "./pages/menus/navigation-menu-list/navigation-menu-list.component";
import { NavigationMenuCreateComponent } from "./pages/menus/navigation-menu-create/navigation-menu-create.component";
import { SidebarMenuCreateComponent } from "./pages/menus/sidebar-menu-create/sidebar-menu-create.component";
import { SidebarMenuListComponent } from "./pages/menus/sidebar-menu-list/sidebar-menu-list.component";
import { FreeServicesListComponent } from "./pages/free-services/free-services-list/free-services-list.component";
import { FreeServicesCreateComponent } from "./pages/free-services/free-services-create/free-services-create.component";
import { AboutUsComponent } from "./pages/about-us/about-us.component";
import { BannersComponent } from "./pages/banners/banners.component";
import { CustomerListComponent } from "./pages/customers/customer-list/customer-list.component";
import { BookingListComponent } from "./pages/bookings/booking-list/booking-list.component";
import { BookingEditComponent } from "./pages/bookings/change-customer-booking/change-customer-booking.component";
import { ComparisonListComponent } from "./pages/customer-comparison/customer-comparison.component";
import { CustomerServiceFormComponent } from "./pages/customer-query/add-query-categories/add-query-categories.component";
import { QueryCategoriesComponent } from "./pages/customer-query/query-categories/query-categories.component";
import { CustomerQueriesComponent } from "./pages/customer-query/customer-queries/customer-queries.component";
import { HolidayMarkerComponent } from "./pages/customers/holiday-markers/holiday-markers.component";
import { ChangeBookingProviderComponent } from "./pages/bookings/change-booking-provider/change-booking-provider.component";
import { CreateCustomerComponent } from "./pages/customers/create-customer/create-customer.component";
import { CreateBookingComponent } from "./pages/bookings/create-booking/create-booking.component";
import { CustomerCallbackComponent } from "./pages/customer-callback/customer-callback.component";
import { BookingDetailComponent } from "./pages/bookings/booking-details/booking-details.component";
import { EmailRequestsComponent } from "./pages/email/email-requests/email-requests.component";
import { EmailCategoryComponent } from "./pages/email/email-category/email-category.component";
import { EmailTemplateViewComponent } from "./pages/email-template-view/email-template-view.component";
import { ContentPDFsComponent } from "./pages/contents/content-pdfs/content-pdfs.component";
import { AdminSignatureComponent } from "./pages/credentials/admin-signature/admin-signature.component";
import { StaticContentComponent } from "./pages/static-content/static-content.component";

export const routes: Routes = [
  {
    path: "",
    component: AppLayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: "",
        component: DashboardComponent,
        pathMatch: "full",
        title: "Dashboard",
      },

      {
        path: "menus/navigation",
        component: NavigationMenuListComponent,
        title: "Navigation Menus",
      },
      {
        path: "menus/navigation/create",
        component: NavigationMenuCreateComponent,
        title: "Create Navigation Menu",
      },
      {
        path: "menus/sidebar",
        component: SidebarMenuListComponent,
        title: "Sidebar Menus",
      },
      {
        path: "menus/sidebar/create",
        component: SidebarMenuCreateComponent,
        title: "Create Sidebar Menu",
      },
      {
        path: "services/free",
        component: FreeServicesListComponent,
        title: "Services",
      },
      {
        path: "services/free/create",
        component: FreeServicesCreateComponent,
        title: "Create Service",
      },
      {
        path: "about-us",
        component: AboutUsComponent,
        title: "About Us",
      },
      {
        path: "banners",
        component: BannersComponent,
        title: "Banners",
      },
      {
        path: "customers",
        component: CustomerListComponent,
        title: "Customers",
      },
      {
        path: "bookings",
        component: BookingListComponent,
        title: "Bookings",
      },
      {
        path: "menus/navigation/edit/:id",
        component: NavigationMenuCreateComponent,
        title: "Edit Navigation Menu",
      },
      {
        path: "menus/sidebar/edit/:id",
        component: SidebarMenuCreateComponent,
        title: "Edit Sidebar Menu",
      },
      {
        path: "services/free/edit/:id",
        component: FreeServicesCreateComponent,
        title: "Edit Service",
      },
      {
        path: "comparisons",
        component: ComparisonListComponent,
        title: "Customer Comparisons",
      },
      {
        path: "customer-query/categories",
        component: QueryCategoriesComponent,
        title: "Customer Query Categories",
      },
      {
        path: "customer-query/categories/edit/:id",
        component: CustomerServiceFormComponent,
        title: "Edit Customer Query Category",
      },
      {
        path: "customer-query/categories/create",
        component: CustomerServiceFormComponent,
        title: "Add Customer Query Category",
      },
      {
        path: "customer-query/customer-queries",
        component: CustomerQueriesComponent,
        title: "Customer Queries",
      },
      {
        path: "bookings/change/:id/edit",
        component: BookingEditComponent,
        title: "Edit Booking",
      },
      {
        path: "customers/holiday-markers",
        component: HolidayMarkerComponent,
        title: "Holiday Markers",
      },
      {
        path: "bookings/:id/change-provider",
        component: ChangeBookingProviderComponent,
        title: "Anbieter wechseln",
      },
      {
        path: "customers/new",
        component: CreateCustomerComponent,
        title: "Neuen Kunden anlegen",
      },
      {
        path: "booking/new",
        component: CreateBookingComponent,
        title: "Neue Buchung erstellen",
      },
      {
        path: "customers/callback-requests",
        component: CustomerCallbackComponent,
        title: "Rückrufe",
      },
      {
        path: "customer-callback",
        component: CustomerCallbackComponent,
        title: "Rückrufe",
      },
      {
        path: "bookings/:id",
        component: BookingDetailComponent,
        title: "Buchungsdetails",
      },
      {
        path: "email-requests",
        component: EmailRequestsComponent,
        title: "E-Mail Anfragen",
      },
      {
        path: "email-category",
        component: EmailCategoryComponent,
        title: "E-Mail Kategorie",
      },
      {
        path: "email-template-view",
        component: EmailTemplateViewComponent,
        title: "E-Mail-Vorlage",
      },
      {
        path: "content",
        component: ContentPDFsComponent,
        title: "Contents",
      },
      {
        path: "static-content",
        component: StaticContentComponent,
        title: "Static Content",
      },
      {
        path: "credentials/admin-signature",
        component: AdminSignatureComponent,
        title: "Admin Signature",
      },
    ],
  },
  {
    path: "signin",
    component: SignInComponent,
  },
];
