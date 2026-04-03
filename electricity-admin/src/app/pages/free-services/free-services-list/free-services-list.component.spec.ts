import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FreeServicesListComponent } from './free-services-list.component';

describe('FreeServicesListComponent', () => {
  let component: FreeServicesListComponent;
  let fixture: ComponentFixture<FreeServicesListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FreeServicesListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FreeServicesListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
