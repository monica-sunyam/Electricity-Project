import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddQueryCategoriesComponent } from './add-query-categories.component';

describe('AddQueryCategoriesComponent', () => {
  let component: AddQueryCategoriesComponent;
  let fixture: ComponentFixture<AddQueryCategoriesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddQueryCategoriesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddQueryCategoriesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
