import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HolidayMarkersComponent } from './holiday-markers.component';

describe('HolidayMarkersComponent', () => {
  let component: HolidayMarkersComponent;
  let fixture: ComponentFixture<HolidayMarkersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HolidayMarkersComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HolidayMarkersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
