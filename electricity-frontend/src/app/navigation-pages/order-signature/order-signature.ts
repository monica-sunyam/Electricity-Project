import {
  Component,
  ElementRef,
  QueryList,
  ViewChildren,
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';

import SignaturePad from 'signature_pad';

import { ContactPerson } from '../../layout/contact-person/contact-person';
import { NeedSupport } from '../../layout/need-support/need-support';

@Component({
  selector: 'app-order-signature',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    ContactPerson,
    NeedSupport,
  ],
  templateUrl: './order-signature.html',
  styleUrl: './order-signature.css',
})
export class OrderSignature {
  @ViewChildren('canvas')
  canvasRefs!: QueryList<ElementRef<HTMLCanvasElement>>;

  signaturePads: SignaturePad[] = [];

  currentStep: number = 0;

  navigateToMainStep(step: number): void {
    if (step < 0 || step > 4) return;

    this.currentStep = step;

    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });

    setTimeout(() => {
      this.initializeSignaturePads();
    }, 0);
  }

  nextStep(step: number): void {
    this.currentStep = step;

    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });

    setTimeout(() => {
      this.initializeSignaturePads();
    }, 0);
  }

  initializeSignaturePads(): void {
    this.signaturePads = [];

    this.canvasRefs.forEach((canvasRef, index) => {
      const canvas = canvasRef.nativeElement;

      canvas.width = canvas.offsetWidth;
      canvas.height = 220;

      const pad = new SignaturePad(canvas, {
        minWidth: 1,
        maxWidth: 2.5,
        penColor: '#000',
      });

      this.signaturePads[index] = pad;
    });
  }

  clearSignature(index: number): void {
    if (this.signaturePads[index]) {
      this.signaturePads[index].clear();
    }
  }

  saveSignature(index: number): void {
    const pad = this.signaturePads[index];

    if (!pad || pad.isEmpty()) {
      alert('Bitte unterschreiben Sie zuerst');
      return;
    }

    const signatureData = pad.toDataURL();

    console.log('Signature:', signatureData);
  }

  isActiveStep(step: number): boolean {
    return this.currentStep === step;
  }
}