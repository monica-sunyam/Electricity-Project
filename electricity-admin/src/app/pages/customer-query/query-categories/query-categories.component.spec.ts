import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QueryCategoriesComponent } from './query-categories.component';

describe('QueryCategoriesComponent', () => {
  let component: QueryCategoriesComponent;
  let fixture: ComponentFixture<QueryCategoriesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QueryCategoriesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QueryCategoriesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
