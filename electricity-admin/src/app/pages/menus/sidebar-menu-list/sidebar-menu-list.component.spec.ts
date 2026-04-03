import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SidebarMenuListComponent } from './sidebar-menu-list.component';

describe('SidebarMenuListComponent', () => {
  let component: SidebarMenuListComponent;
  let fixture: ComponentFixture<SidebarMenuListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SidebarMenuListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SidebarMenuListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
