import { CommonModule } from "@angular/common";
import {
  Component,
  ElementRef,
  QueryList,
  ViewChildren,
  ChangeDetectorRef,
  OnInit,
  OnDestroy,
} from "@angular/core";
import { SidebarService } from "../../services/sidebar.service";
import { NavigationEnd, Router, RouterModule } from "@angular/router";
import { SafeHtmlPipe } from "../../pipe/safe-html.pipe";
import { combineLatest, Subscription, filter, first } from "rxjs";

type NavItem = {
  name: string;
  icon: string;
  path?: string;
  new?: boolean;
  subItems?: { name: string; path: string; pro?: boolean; new?: boolean }[];
};

@Component({
  selector: "app-sidebar",
  standalone: true,
  imports: [CommonModule, RouterModule, SafeHtmlPipe],
  templateUrl: "./app-sidebar.component.html",
})
export class AppSidebarComponent implements OnInit, OnDestroy {
  readonly isExpanded$;
  readonly isMobileOpen$;
  readonly isHovered$;

  private subscription = new Subscription();
  navItems: NavItem[] = [
    {
      name: "Dashboard",
      path: "/",
      icon: `<svg width="1em" height="1em" viewBox="0 0 24 24" fill="none">
      <path d="M5.5 3.25C4.25736 3.25 3.25 4.25736 3.25 5.5V8.99998C3.25 10.2426 4.25736 11.25 5.5 11.25H9C10.2426 11.25 11.25 10.2426 11.25 8.99998V5.5C11.25 4.25736 10.2426 3.25 9 3.25H5.5Z" fill="currentColor"></path>
      <path d="M15 3.25C13.7574 3.25 12.75 4.25736 12.75 5.5V8.99998C12.75 10.2426 13.7574 11.25 15 11.25H18.5C19.7426 11.25 20.75 10.2426 20.75 8.99998V5.5C20.75 4.25736 19.7426 3.25 18.5 3.25H15Z" fill="currentColor"></path>
      <path d="M5.5 12.75C4.25736 12.75 3.25 13.7574 3.25 15V18.5C3.25 19.7426 4.25736 20.75 5.5 20.75H9C10.2426 20.75 11.25 19.7426 11.25 18.5V15C11.25 13.7574 10.2426 12.75 9 12.75H5.5Z" fill="currentColor"></path>
      <path d="M15 12.75C13.7574 12.75 12.75 13.7574 12.75 15V18.5C12.75 19.7426 13.7574 20.75 15 20.75H18.5C19.7426 20.75 20.75 19.7426 20.75 18.5V15C20.75 13.7574 19.7426 12.75 18.5 12.75H15Z" fill="currentColor"></path>
    </svg>`,
    },
    {
      name: "Menus",
      icon: `<svg width="1em" height="1em" viewBox="0 0 24 24" fill="none">
      <path d="M4 6H20M4 12H20M4 18H20" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
    </svg>`,
      subItems: [
        { name: "Navigation Menus", path: "/menus/navigation" },
        { name: "Sidebar Menus", path: "/menus/sidebar" },
      ],
    },
    {
      name: "Services",
      icon: `<svg width="1em" height="1em" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>`,
      subItems: [{ name: "All Services", path: "/services/free" }],
    },
    {
      name: "About Us",
      icon: `<svg width="1em" height="1em" viewBox="0 0 24 24" fill="none">
      <path d="M12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      <path d="M12 16C10.3431 16 9 14.6569 9 13C9 11.3431 10.3431 10 12 10C13.6569 10 15 11.3431 15 13C15 14.6569 13.6569 16 12 16Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      <path d="M12 10V8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>`,
      subItems: [{ name: "About Us", path: "/about-us" }],
    },
    {
      name: "Banners",
      icon: `<svg width="1em" height="1em" viewBox="0 0 24 24" fill="none">
      <path d="M4 4H20V20H4V4Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      <path d="M4 9H20M4 15H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>`,
      subItems: [{ name: "Banners", path: "/banners" }],
    },
    {
      name: "Customers",
      icon: `<svg width="1em" height="1em" viewBox="0 0 24 24" fill="none">
      <path d="M16 21V19C16 17.8954 15.1046 17 14 17H6C4.89543 17 4 17.8954 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      <circle cx="10" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
      <path d="M20 21V19C20 18.067 19.3638 17.2519 18.5 17.0127" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      <path d="M15.5 3.28906C16.3968 3.64352 17 4.52164 17 5.5C17 6.47836 16.3968 7.35648 15.5 7.71094" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>`,
      subItems: [{ name: "Customer List", path: "/customers" }, { name: "Holiday Markers", path: "/customers/holiday-markers" }],
    },
    {
      name: "Bookings",
      icon: `<svg width="1em" height="1em" viewBox="0 0 24 24" fill="none">
      <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
      <path d="M8 2V6M16 2V6M3 10H21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      <path d="M8 14H12M8 18H10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>`,
      subItems: [{ name: "Booking List", path: "/bookings" }],
    },
    {
      name: "Customer comparison",
      icon: `<svg width="1em" height="1em" viewBox="0 0 24 24" fill="none">
      <path d="M12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      <path d="M9.5 9C9.5 7.61929 10.6193 6.5 12 6.5C13.3807 6.5 14.5 7.61929 14.5 9C14.5 10.3807 13.3807 11.5 12 11.5C10.6193 11.5 9.5 10.3807 9.5 9Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      <path d="M12 11.5V14M12 17H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>`,
      subItems: [{ name: "Comparison List", path: "/comparisons" }],
    },
    {
      name: "Customer Queries",
      icon: `<svg width="1em" height="1em" viewBox="0 0 24 24" fill="none">
      <path d="M12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      <path d="M9.5 9C9.5 7.61929 10.6193 6.5 12 6.5C13.3807 6.5 14.5 7.61929 14.5 9C14.5 10.3807 13.3807 11.5 12 11.5C10.6193 11.5 9.5 10.3807 9.5 9Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      <path d="M12 11.5V14M12 17H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>`,
      subItems: [{ name: "Query Categories", path: "/customer-query/categories" }, { name: "Customer Queries", path: "/customer-query/customer-queries" }],
    }
  ];

  openSubmenu: string | null = null;
  subMenuHeights: { [key: string]: number } = {};

  // Use @ViewChildren to get references to submenu containers
  @ViewChildren("subMenu") subMenuRefs!: QueryList<ElementRef>;

  constructor(
    public sidebarService: SidebarService,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {
    this.isExpanded$ = this.sidebarService.isExpanded$;
    this.isMobileOpen$ = this.sidebarService.isMobileOpen$;
    this.isHovered$ = this.sidebarService.isHovered$;
  }

  ngOnInit() {
    // 1. Router Subscription
    this.subscription.add(
      this.router.events
        .pipe(
          filter(
            (event): event is NavigationEnd => event instanceof NavigationEnd,
          ),
        )
        .subscribe((event) => {
          this.setActiveMenuFromRoute(event.urlAfterRedirects);
        }),
    );

    // 2. Sidebar State Subscription
    this.subscription.add(
      combineLatest([
        this.isExpanded$,
        this.isMobileOpen$,
        this.isHovered$,
      ]).subscribe(([isExpanded, isMobileOpen, isHovered]) => {
        if (!isExpanded && !isMobileOpen && !isHovered) {
          // Optional: Close menus when sidebar collapses
          // this.openSubmenu = null;
        }
        this.cdr.markForCheck();
      }),
    );

    this.setActiveMenuFromRoute(this.router.url);
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  toggleSubmenu(section: string, index: number) {
    const key = `${section}-${index}`;

    if (this.openSubmenu === key) {
      this.openSubmenu = null;
      this.subMenuHeights[key] = 0;
    } else {
      this.openSubmenu = key;
      this.calculateHeight(key);
    }
  }

  private calculateHeight(key: string) {
    // Use requestAnimationFrame or setTimeout to wait for DOM rendering
    setTimeout(() => {
      const el = document.getElementById(key);
      if (el) {
        this.subMenuHeights[key] = el.scrollHeight;
        this.cdr.markForCheck();
      }
    });
  }

  private setActiveMenuFromRoute(currentUrl: string) {
    this.navItems.forEach((nav, i) => {
      if (nav.subItems?.some((sub) => currentUrl.startsWith(sub.path))) {
        const key = `main-${i}`;
        this.openSubmenu = key;
        this.calculateHeight(key);
      }
    });
  }

  onSidebarMouseEnter() {
    this.isExpanded$.pipe(first()).subscribe((expanded) => {
      if (!expanded) this.sidebarService.setHovered(true);
    });
  }

  onSubmenuClick() {
    this.isMobileOpen$.pipe(first()).subscribe((isMobile) => {
      if (isMobile) this.sidebarService.setMobileOpen(false);
    });
  }

  isActive(path: string | undefined): boolean {
    if (!path) return false;

    // Get the current URL without query params or fragments
    const currentUrl = this.router.url.split("?")[0].split("#")[0];

    // Exact match for the path
    return currentUrl === path;
  }

  isActiveInSubmenu(subItems: { path: string }[]): boolean {
    return subItems.some((sub) => this.isActive(sub.path));
  }
}
