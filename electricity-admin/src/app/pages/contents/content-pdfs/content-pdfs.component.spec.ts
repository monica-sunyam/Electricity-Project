import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContentPDFsComponent } from './content-pdfs.component';

describe('ContentPDFsComponent', () => {
  let component: ContentPDFsComponent;
  let fixture: ComponentFixture<ContentPDFsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContentPDFsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ContentPDFsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
