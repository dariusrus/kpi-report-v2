import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MonthlyLeadsCapturedComponent } from './monthly-leads-captured.component';

describe('MonthlyLeadsCapturedComponent', () => {
  let component: MonthlyLeadsCapturedComponent;
  let fixture: ComponentFixture<MonthlyLeadsCapturedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MonthlyLeadsCapturedComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MonthlyLeadsCapturedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
