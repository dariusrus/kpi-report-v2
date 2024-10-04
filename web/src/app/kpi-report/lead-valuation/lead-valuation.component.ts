import {Component, Input, OnInit} from '@angular/core';
import {KpiReport} from "../../models/kpi-report";
import {MonthlyAverage} from "../../models/monthly-average";
import {SharedUtil} from "../../util/shared-util";

@Component({
  selector: 'app-lead-valuation',
  templateUrl: './lead-valuation.component.html',
  styleUrl: './lead-valuation.component.css'
})
export class LeadValuationComponent implements OnInit {
  @Input() reportData!: KpiReport;
  @Input() reportDataPreviousMap: [KpiReport, MonthlyAverage][] = [];
  @Input() displayComparisons!: boolean;
  @Input() monthCount!: number;
  @Input() isVisible!: { [key: string]: string };

  data: any[] = [];
  xAxisLabel = 'Month & Year';
  yAxisLabel = 'Lead Valuation';

  customColorsValue = [
    { name: 'Website Lead', value: '#76c437' },
    { name: 'Manual User Input', value: '#45abb9' }
  ];
  protected readonly SharedUtil = SharedUtil;

  previousMonthIndex: number | null = null;
  fromLastMonthTooltip = false;

  ngOnInit(): void {
    this.populateChart(this.reportData, this.reportDataPreviousMap);
    this.computeIndexes(this.reportData, this.reportDataPreviousMap);
  }

  showTooltip(hoveredObject: string) {
    this.hideAllTooltips();

    if (hoveredObject === 'fromLastMonth') {
      this.fromLastMonthTooltip = true;
    }
  }

  hideAllTooltips() {
    this.fromLastMonthTooltip = false;
  }

  private computeIndexes(currentData: KpiReport, previousData: any[]) {
    const previousMonth = previousData.length > 0 ? previousData[0][0] : null;
    if (previousMonth && previousMonth.websiteLead.totalValues !== null && currentData.websiteLead.totalValues !== null) {
      this.previousMonthIndex = ((currentData.websiteLead.totalValues - previousMonth.websiteLead.totalValues) / previousMonth.websiteLead.totalValues) * 100;
    }
  }

  private populateChart(currentData: KpiReport, previousData: any[]): void {
    this.data = previousData
      .filter(([report]) =>
        report?.monthAndYear &&
        report.websiteLead &&
        report.websiteLead.totalManualValuation !== null &&
        report.websiteLead.totalWebsiteValuation !== null
      )
      .map(([report]) => ({
        name: SharedUtil.formatMonthAndYear(report.monthAndYear),
        series: [
          {
            name: 'Manual User Input',
            value: report.websiteLead.totalManualValuation
          },
          {
            name: 'Website Lead',
            value: report.websiteLead.totalWebsiteValuation
          }
        ]
      })).reverse();

    if (currentData?.monthAndYear &&
      currentData.websiteLead &&
      currentData.websiteLead.totalManualValuation !== null &&
      currentData.websiteLead.totalWebsiteValuation !== null) {
      this.data.push({
        name: SharedUtil.formatMonthAndYear(currentData.monthAndYear),
        series: [
          {
            name: 'Manual User Input',
            value: currentData.websiteLead.totalManualValuation
          },
          {
            name: 'Website Lead',
            value: currentData.websiteLead.totalWebsiteValuation
          }
        ]
      });
    }
  }
}
