import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ContentService } from '../../services/content.service';

@Component({
  selector: 'app-need-support',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './need-support.html',
  styleUrl: './need-support.css',
})
export class NeedSupport implements OnInit {
  contactNumber: string = '';

  constructor(private contentService: ContentService) {}

  ngOnInit(): void {
    console.log('NeedSupport: Starting to load data');
    this.contentService.getData().subscribe({
      next: (data) => {
        console.log('NeedSupport: Data received', data);
        const about = data?.menu?.about;
        if (about?.length > 0) {
          this.contactNumber = about[0].contactNumber ?? '';
          console.log('NeedSupport: Contact number set to', this.contactNumber);
        } else {
          console.log('NeedSupport: No about data found');
        }
      },
      error: (err) => console.error('NeedSupport load failed', err),
    });
  }
}
