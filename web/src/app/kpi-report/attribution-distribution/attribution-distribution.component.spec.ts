import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AttributionDistributionComponent } from './attribution-distribution.component';

describe('LeadValuationComponent', () => {
  let component: AttributionDistributionComponent;
  let fixture: ComponentFixture<AttributionDistributionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AttributionDistributionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AttributionDistributionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
