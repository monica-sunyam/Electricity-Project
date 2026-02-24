import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { MatIcon } from '@angular/material/icon';

@Component({
  selector: 'app-heating-electricity',
  imports: [CommonModule, MatIcon],
  templateUrl: './heating-electricity.html',
  styleUrl: './heating-electricity.css',
})
export class HeatingElectricity {
  selectedOption: 'ja' | 'nein' = 'ja';
  selectedTariff: 'single' | 'double' = 'single';
  showNoBanner = false;

  select(option: 'ja' | 'nein') {
    this.selectedOption = option;

  if (option === 'nein') {
    this.selectedTariff = 'single';
    this.showNoBanner = true;
    } else {
      this.showNoBanner = false;
    }
  }

  closeNoBanner() {
    this.showNoBanner = false;
    this.selectedOption = 'ja'; // switches to JA view when X is clicked
  }

  tariff(type: 'single' | 'double') {
    this.selectedTariff = type;
  }


}
