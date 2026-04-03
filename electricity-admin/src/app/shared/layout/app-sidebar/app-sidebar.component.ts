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
      name: "Free Services",
      icon: `<svg width="1em" height="1em" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>`,
      subItems: [
        { name: "Free Services", path: "/services/free" },
      ],
    },
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
