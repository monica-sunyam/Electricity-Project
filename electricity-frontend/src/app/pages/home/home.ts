import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Discount } from '../discount/discount';
import { NeedSupport } from '../../layout/need-support/need-support';
import { ContentService } from '../../services/content.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [MatIconModule, MatInputModule, CommonModule, Discount, RouterModule, NeedSupport],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit {
  sidebarItems: any[] = [];
  freeServices: any[] = [];
  otherServices: any[] = [];
  aboutItems: any[] = [];
  isLoading = true;

  private readonly ICON_CLASS_MAP: Record<string, string> = {
    'Stromvergleich.png': 'icon-cable',
    'Gasvergleich.png': 'icon',
    'Warmepumpe.png': 'icon-pump',
    'Nachtspeicherofen.png': 'icon',
    'Ladestrom.png': 'icon',
  };

  private readonly ROUTE_MAP: Record<string, string> = {
    'Stromvergleich.png': '/home/electricity',
    'Gasvergleich.png': '/home/gas',
    'Warmepumpe.png': '/home/heating-electricity',
    'Nachtspeicherofen.png': '/home/night-heaters',
    'Ladestrom.png': '/home/car-electricity',
  };

  private readonly LABEL_MAP: Record<string, string> = {
    'Stromvergleich.png': 'Strom | Hausstrom',
    'Gasvergleich.png': 'Gas',
    'Warmepumpe.png': 'Heizstrom | Wärmepumpe',
    'Nachtspeicherofen.png': 'Heizstrom | Nachtspeicher',
    'Ladestrom.png': 'Ladestrom | Autostrom',
  };

  constructor(
    private contentService: ContentService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.contentService.getData().subscribe({
      next: (data) => {
        if (data?.res && data?.menu?.sidebar) {
          this.sidebarItems = [...data.menu.sidebar].sort((a, b) => a.order - b.order);
        }

        if (data.service?.['free-service']) {
          this.freeServices = data.service['free-service']
            .filter((s: any) => s.highlight === 0)
            .sort((a: any, b: any) => a.order - b.order);
        }

        if (data.service?.['other-service']) {
          const seen = new Set<string>();
          this.otherServices = data.service['other-service']
            .sort((a: any, b: any) => a.order - b.order)
            .filter((s: any) => {
              if (seen.has(s.heading)) return false;
              seen.add(s.heading);
              return true;
            });
        }

        if (data.menu?.about) {
          this.aboutItems = [...data.menu.about].sort((a, b) => a.order - b.order);
        }

        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Home data load failed', err);
        this.isLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  getImageUrl(contentUrl: string): string {
    return this.contentService.getImageUrl(contentUrl);
  }

  getIconClass(fileName: string): string {
    return this.ICON_CLASS_MAP[fileName] || 'icon';
  }

  getRouterLink(fileName: string): string {
    return this.ROUTE_MAP[fileName] || '/home';
  }

  getLabel(fileName: string): string {
    return this.LABEL_MAP[fileName] || 'Service';
  }
}
