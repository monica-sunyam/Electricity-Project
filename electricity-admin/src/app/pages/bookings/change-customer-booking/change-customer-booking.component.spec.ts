import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChangeCustomerBookingComponent } from './change-customer-booking.component';

describe('ChangeCustomerBookingComponent', () => {
  let component: ChangeCustomerBookingComponent;
  let fixture: ComponentFixture<ChangeCustomerBookingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChangeCustomerBookingComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ChangeCustomerBookingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
