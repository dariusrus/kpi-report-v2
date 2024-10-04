import {Component, Input, OnInit} from '@angular/core';
import {LegendPosition} from "@swimlane/ngx-charts";
import {KpiReport} from "../../models/kpi-report";
import {MonthlyAverage} from "../../models/monthly-average";
import {SharedUtil} from "../../util/shared-util";
import {dropInAnimation} from "../../util/animations";

@Component({
  selector: 'app-google-analytics',
  templateUrl: './google-analytics.component.html',
  styleUrl: './google-analytics.component.css',
  animations: [dropInAnimation]
})
export class GoogleAnalyticsComponent implements OnInit {
  @Input() reportData!: KpiReport;
  @Input() averageReportData!: MonthlyAverage;
  @Input() averageReportDataPrevious!: MonthlyAverage[];
  @Input() reportDataPreviousMap: [KpiReport, MonthlyAverage][] = [];
  @Input() displayComparisons!: boolean;
  @Input() monthCount!: number;
  @Input() isVisible!: { [key: string]: string };

  scheme = 'forest';
  schemeAverage = 'picnic'
  data: any[] = [];
  dataAverage: any[] = [];
  xAxisLabel = 'Month & Year';
  yAxisLabel = '';
  protected readonly LegendPosition = LegendPosition;

  previousMonthIndex: number | null = null;
  peerComparisonIndex: number | null = null;

  fromLastMonthTooltip = false;
  peerComparisonTooltip = false;

  ngOnInit(): void {
    this.yAxisLabel = 'Unique Site Visitors (' + this.reportData.country + ')';
    this.populateChart(this.reportData, this.reportDataPreviousMap);
    this.populateGroupedChart(this.reportData, this.reportDataPreviousMap, this.averageReportData);
    this.computeIndexes(this.reportData, this.reportDataPreviousMap, this.averageReportData);
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
    if (previousMonth && previousMonth.uniqueSiteVisitors !== null && currentData.uniqueSiteVisitors !== null) {
      this.previousMonthIndex = ((currentData.uniqueSiteVisitors - previousMonth.uniqueSiteVisitors) / previousMonth.uniqueSiteVisitors) * 100;
    }

    if (currentAverage && currentAverage.averageUniqueSiteVisitors !== null && currentData.uniqueSiteVisitors !== null) {
      this.peerComparisonIndex = ((currentData.uniqueSiteVisitors - currentAverage.averageUniqueSiteVisitors) / currentAverage.averageUniqueSiteVisitors) * 100;
    }
  }

  private populateChart(currentData: KpiReport, previousData: any[]): void {
    this.data = [
      ...previousData
        .filter(([report]) => report !== null && report.uniqueSiteVisitors !== null)
        .map(([report]) => ({
          name: SharedUtil.formatMonthAndYear(report.monthAndYear),
          value: report.uniqueSiteVisitors
        })).reverse(),
      {
        name: SharedUtil.formatMonthAndYear(currentData.monthAndYear),
        value: currentData.uniqueSiteVisitors
      }
    ];
  }

  private populateGroupedChart(currentData: KpiReport, previousData: any[], currentAverage: MonthlyAverage): void {
    this.dataAverage = [
      ...previousData
        .filter(([report, average]) =>
          report?.monthAndYear &&
          report.uniqueSiteVisitors !== null &&
          average &&
          average.averageUniqueSiteVisitors !== null
        )
        .map(([report, average]) => ({
          name: SharedUtil.formatMonthAndYear(report.monthAndYear),
          series: [
            {
              name: "BLC Average",
              value: average.averageUniqueSiteVisitors
            },
            {
              name: currentData.subAgency,
              value: report.uniqueSiteVisitors
            }
          ]
        })).reverse(),
      currentData?.monthAndYear &&
      currentData.uniqueSiteVisitors !== null &&
      currentAverage &&
      currentAverage.averageUniqueSiteVisitors !== null ? {
        name: SharedUtil.formatMonthAndYear(currentData.monthAndYear),
        series: [
          {
            name: "BLC Average",
            value: currentAverage.averageUniqueSiteVisitors
          },
          {
            name: currentData.subAgency,
            value: currentData.uniqueSiteVisitors
          }
        ]
      } : null
    ].filter(Boolean);
  }

  protected readonly SharedUtil = SharedUtil;
}
