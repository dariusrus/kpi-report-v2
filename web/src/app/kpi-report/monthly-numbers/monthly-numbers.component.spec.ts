import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MonthlyNumbersComponent } from './monthly-numbers.component';

describe('MonthlyNumbersComponent', () => {
  let component: MonthlyNumbersComponent;
  let fixture: ComponentFixture<MonthlyNumbersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MonthlyNumbersComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MonthlyNumbersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
