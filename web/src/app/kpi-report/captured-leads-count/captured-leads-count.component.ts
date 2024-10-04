import {Component, Input, OnInit} from '@angular/core';
import {LegendPosition} from "@swimlane/ngx-charts";
import {KpiReport} from "../../models/kpi-report";
import {MonthlyAverage} from "../../models/monthly-average";
import {SharedUtil} from "../../util/shared-util";
import {dropInAnimation} from "../../util/animations";

@Component({
  selector: 'app-captured-leads-count',
  templateUrl: './captured-leads-count.component.html',
  styleUrl: './captured-leads-count.component.css',
  animations: [dropInAnimation]
})
export class CapturedLeadsCountComponent implements OnInit {
  @Input() reportData!: KpiReport;
  @Input() averageReportData!: MonthlyAverage;
  @Input() reportDataPreviousMap: [KpiReport, MonthlyAverage][] = [];
  @Input() displayComparisons!: boolean;
  @Input() monthCount!: number;
  @Input() isVisible!: { [key: string]: string };

  scheme = 'cool';
  data: any[] = [];
  dataAverage: any[] = [];
  xAxisLabel = 'Month & Year';
  yAxisLabel = 'Captured Leads Count';
  protected readonly LegendPosition = LegendPosition;

  previousMonthIndex: number | null = null;
  peerComparisonIndex: number | null = null;

  fromLastMonthTooltip = false;
  peerComparisonTooltip = false;

  customColors = [
    { name: 'Website Lead', value: '#37C469FF' },
    { name: 'Manual User Input', value: '#4571B9FF' }
  ];

  ngOnInit(): void {
    this.populateChart(this.reportData, this.reportDataPreviousMap);
    this.populateGroupedChart(this.reportData, this.reportDataPreviousMap, this.averageReportData);
    this.computeIndexes(this.reportData, this.reportDataPreviousMap, this.averageReportData);
    this.yAxisLabel = 'Unique Site Visitors (' + this.reportData.country + ')';
  }

  showTooltip(hoveredObject: string) {
    this.hideAllTooltips();

    if (hoveredObject === 'fromLastMonth') {
      this.fromLastMonthTooltip = true;
    } else if (hoveredObject === 'peerComparisonTooltip') {
      this.peerComparisonTooltip = true;
    }
  }

  hideAllTooltips() {
    this.fromLastMonthTooltip = false;
    this.peerComparisonTooltip = false;
  }

  private computeIndexes(currentData: KpiReport, previousData: any[], currentAverage: MonthlyAverage) {
    const previousMonth = previousData.length > 0 ? previousData[0][0] : null;
    if (previousMonth && previousMonth.websiteLead.totalLeads !== null && currentData.websiteLead.totalLeads !== null) {
      this.previousMonthIndex = ((currentData.websiteLead.totalLeads - previousMonth.websiteLead.totalLeads) / previousMonth.websiteLead.totalLeads) * 100;
    }

    if (currentAverage && currentAverage.averageTotalLeads !== null && currentData.websiteLead.totalLeads !== null) {
      this.peerComparisonIndex = ((currentData.websiteLead.totalLeads - currentAverage.averageTotalLeads) / currentAverage.averageTotalLeads) * 100;
    }
  }

  private populateChart(currentData: KpiReport, previousData: any[]): void {
    this.data = previousData
      .filter(([report]) =>
        report?.monthAndYear &&
        report.websiteLead &&
        report.websiteLead.totalManualLeads !== null &&
        report.websiteLead.totalWebsiteLeads !== null
      )
      .map(([report]) => ({
        name: SharedUtil.formatMonthAndYear(report.monthAndYear),
        series: [
          {
            name: 'Manual User Input',
            value: report.websiteLead.totalManualLeads
          },
          {
            name: 'Website Lead',
            value: report.websiteLead.totalWebsiteLeads
          }
        ]
      })).reverse();

    if (currentData?.monthAndYear &&
      currentData.websiteLead &&
      currentData.websiteLead.totalManualLeads !== null &&
      currentData.websiteLead.totalWebsiteLeads !== null) {
      this.data.push({
        name: SharedUtil.formatMonthAndYear(currentData.monthAndYear),
        series: [
          {
            name: 'Manual User Input',
            value: currentData.websiteLead.totalManualLeads
          },
          {
            name: 'Website Lead',
            value: currentData.websiteLead.totalWebsiteLeads
          }
        ]
      });
    }
  }

  private populateGroupedChart(currentData: KpiReport, previousData: any[], currentAverage: MonthlyAverage): void {
    this.dataAverage = [
      ...previousData
        .filter(([report, average]) =>
          report?.monthAndYear &&
          report.websiteLead &&
          report.websiteLead.totalLeads !== null &&
          average &&
          average.averageTotalLeads !== null
        )
        .map(([report, average]) => ({
          name: SharedUtil.formatMonthAndYear(report.monthAndYear),
          series: [
            {
              name: "BLC Average",
              value: average.averageTotalLeads
            },
            {
              name: currentData.subAgency,
              value: report.websiteLead.totalLeads
            }
          ]
        })).reverse(),
      currentData.monthAndYear &&
      currentData.websiteLead &&
      currentData.websiteLead.totalLeads !== null &&
      currentAverage &&
      currentAverage.averageTotalLeads !== null ? {
        name: SharedUtil.formatMonthAndYear(currentData.monthAndYear),
        series: [
          {
            name: "BLC Average",
            value: currentAverage.averageTotalLeads
          },
          {
            name: currentData.subAgency,
            value: currentData.websiteLead.totalLeads
          }
        ]
      } : null
    ].filter(Boolean);
  }

  protected readonly SharedUtil = SharedUtil;
}
