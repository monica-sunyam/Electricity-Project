import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SidebarMenuCreateComponent } from './sidebar-menu-create.component';

describe('SidebarMenuCreateComponent', () => {
  let component: SidebarMenuCreateComponent;
  let fixture: ComponentFixture<SidebarMenuCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SidebarMenuCreateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SidebarMenuCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
