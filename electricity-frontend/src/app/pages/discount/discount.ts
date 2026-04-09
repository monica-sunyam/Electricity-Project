import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-discount',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatIconModule],
  templateUrl: './discount.html',
  styleUrl: './discount.css',
})
export class Discount implements OnInit {
  highlightedServices: any[] = [];
  currentDialogText = '';
  isLoading = true;

  constructor(
    public dialog: MatDialog,
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.http.post<any>('http://192.168.0.155:8080/api/content', {}).subscribe({
      next: (data) => {
        if (data?.res && data.service?.['free-service']) {
          // Filter for items with highlight: 1
          this.highlightedServices = data.service['free-service']
            .filter((s: any) => s.highlight === 1)
            .sort((a: any, b: any) => a.order - b.order);
        }
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  openInfo(template: any, htmlContent: string) {
    this.currentDialogText = htmlContent;
    this.dialog.open(template);
  }
}
