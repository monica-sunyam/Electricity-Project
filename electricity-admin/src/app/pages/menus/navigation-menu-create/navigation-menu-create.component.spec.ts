import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NavigationMenuCreateComponent } from './navigation-menu-create.component';

describe('NavigationMenuCreateComponent', () => {
  let component: NavigationMenuCreateComponent;
  let fixture: ComponentFixture<NavigationMenuCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavigationMenuCreateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NavigationMenuCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
