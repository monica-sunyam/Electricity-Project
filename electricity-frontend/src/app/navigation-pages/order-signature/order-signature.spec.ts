import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrderSignature } from './order-signature';

describe('OrderSignature', () => {
  let component: OrderSignature;
  let fixture: ComponentFixture<OrderSignature>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrderSignature]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrderSignature);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
