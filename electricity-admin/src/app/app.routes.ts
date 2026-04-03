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
        title: "Free Services",
      },
      {
        path: "services/free/create",
        component: FreeServicesCreateComponent,
        title: "Create Free Service",
      }
    ],
  },
  {
    path: "signin",
    component: SignInComponent,
  },
];