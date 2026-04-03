import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FreeServicesCreateComponent } from './free-services-create.component';

describe('FreeServicesCreateComponent', () => {
  let component: FreeServicesCreateComponent;
  let fixture: ComponentFixture<FreeServicesCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FreeServicesCreateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FreeServicesCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
