import { TestBed } from '@angular/core/testing';

import { KpiReportService } from './kpi-report.service';

describe('KpiReportService', () => {
  let service: KpiReportService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KpiReportService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
