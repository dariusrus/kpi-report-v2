import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SalesPipelineReportComponent } from './sales-pipeline-report.component';

describe('SalesPipelineReportComponent', () => {
  let component: SalesPipelineReportComponent;
  let fixture: ComponentFixture<SalesPipelineReportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SalesPipelineReportComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SalesPipelineReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
