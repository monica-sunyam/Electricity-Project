import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NavigationMenuListComponent } from './navigation-menu-list.component';

describe('NavigationMenuListComponent', () => {
  let component: NavigationMenuListComponent;
  let fixture: ComponentFixture<NavigationMenuListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavigationMenuListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NavigationMenuListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
