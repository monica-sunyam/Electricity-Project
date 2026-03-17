import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConnectionData } from './connection-data';

describe('ConnectionData', () => {
  let component: ConnectionData;
  let fixture: ComponentFixture<ConnectionData>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConnectionData]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConnectionData);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
