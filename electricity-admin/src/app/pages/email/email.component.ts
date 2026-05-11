import { Component } from '@angular/core';

@Component({
  selector: 'app-email',
  imports: [],
  templateUrl: './email.component.html',
  styleUrl: './email.component.css',
})
export class EmailComponent {
  showSecondUpload = false;

  addUploadField() {
    this.showSecondUpload = true;
  }

  removeUploadField() {
    this.showSecondUpload = false;
  }
}
