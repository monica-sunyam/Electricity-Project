import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CreateBusinessAccount } from './create-account-business';


describe('CreateAccountBusiness', () => {
  let component: CreateBusinessAccount;
  let fixture: ComponentFixture<CreateBusinessAccount>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateBusinessAccount]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateBusinessAccount);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
