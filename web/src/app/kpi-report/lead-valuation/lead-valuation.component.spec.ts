import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LeadValuationComponent } from './lead-valuation.component';

describe('LeadValuationComponent', () => {
  let component: LeadValuationComponent;
  let fixture: ComponentFixture<LeadValuationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LeadValuationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LeadValuationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
