import {Component, HostListener, Input, OnInit} from '@angular/core';
import {LegendPosition} from "@swimlane/ngx-charts";
import {KpiReport} from "../../models/kpi-report";
import {MonthlyAverage} from "../../models/monthly-average";
import {SharedUtil} from "../../util/shared-util";
import {dropInAnimation} from "../../util/animations";
import {CityAnalytics} from "../../models/ga/city-analytics";
import {MultiSelectChangeEvent} from "primeng/multiselect";

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
  view: [number, number] = [600, 400];

  cities: CityAnalytics[] = [];
  selectedCities: CityAnalytics[] = [];
  selectedCitiesLabel: string = '0 cities selected';

  cityReportData: KpiReport = this.reportData;
  cityReportDataPreviousMap: [KpiReport, MonthlyAverage][] = this.reportDataPreviousMap;

  infoTooltip = false;

  ngOnInit(): void {
    this.yAxisLabel = 'Unique Site Visitors (' + this.reportData.country + ')';
    this.populateChart(this.reportData, this.reportDataPreviousMap);
    this.populateGroupedChart(this.reportData, this.reportDataPreviousMap, this.averageReportData);
    this.computeIndexes(this.reportData, this.reportDataPreviousMap, this.averageReportData);
    this.cities = this.reportData.cityAnalytics;
    this.cityReportData = this.reportData;
    this.cityReportDataPreviousMap = this.reportDataPreviousMap;
    this.cities = this.reportData.cityAnalytics.map(cityAnalytics => ({
      ...cityAnalytics,
      cityCount: `${cityAnalytics.city} (${cityAnalytics.uniqueSiteVisitors})`
    }));
    this.updateChartView();
    this.updateSelectedCities();
    this.selectedCities = this.cities;
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.updateChartView();
  }

  updateSelectedCities() {
    let count = this.selectedCities.length;

    if (count === 0) {
      this.selectedCities = this.cities;
      count = this.selectedCities.length;
    }

    this.selectedCitiesLabel = `${count} cities selected`;
    if (count <= 3) {
      const selectedCityNames = this.selectedCities.map(city => city.city).join(', ');
      this.yAxisLabel = `Unique Site Visitors (${selectedCityNames})`;
    } else {
      this.yAxisLabel = `Unique Site Visitors (${count} cities)`;
    }

    if (this.selectedCities.length === this.cities.length) {
      this.selectedCitiesLabel = 'All cities selected';
      this.yAxisLabel = 'Unique Site Visitors (' + this.reportData.country + ')';
    }

    const totalUniqueVisitors = this.selectedCities.reduce((sum, city) => sum + city.uniqueSiteVisitors, 0);

    this.cityReportData.uniqueSiteVisitors = totalUniqueVisitors;

    this.cityReportDataPreviousMap.forEach(([previousReport, monthlyAverage]) => {
      const previousTotalUniqueVisitors = this.selectedCities.reduce((sum, city) => {
        const matchingCity = previousReport.cityAnalytics.find(c => c.city === city.city);
        return sum + (matchingCity ? matchingCity.uniqueSiteVisitors : 0);
      }, 0);

      previousReport.uniqueSiteVisitors = previousTotalUniqueVisitors;
    });

    this.populateChart(this.cityReportData, this.cityReportDataPreviousMap);
    this.populateGroupedChart(this.cityReportData, this.cityReportDataPreviousMap, this.averageReportData);
    this.computeIndexes(this.cityReportData, this.cityReportDataPreviousMap, this.averageReportData);
  }

  updateChartView() {
    if (window.innerWidth >= 1400) {
      this.view = [600, 400];
    } else if (window.innerWidth < 1400 && window.innerWidth >= 1200) {
      this.view = [500, 400];
    } else {
      this.view = [400, 400];
    }
  }

  showTooltip(hoveredObject: string) {
    this.hideAllTooltips();

    if (hoveredObject === 'fromLastMonth') {
      this.fromLastMonthTooltip = true;
    } else if (hoveredObject === 'peerComparisonTooltip') {
      this.peerComparisonTooltip = true;
    } else if (hoveredObject === 'infoTooltip') {
      this.infoTooltip = true;
    }
  }

  hideAllTooltips() {
    this.fromLastMonthTooltip = false;
    this.peerComparisonTooltip = false;
    this.infoTooltip = false;
  }

  private computeIndexes(currentData: KpiReport, previousData: any[], currentAverage: MonthlyAverage) {
    const previousMonth = previousData.length > 0 ? previousData[0][0] : null;

    if (previousMonth && previousMonth.uniqueSiteVisitors !== null && currentData.uniqueSiteVisitors !== null) {
      const currentVisitors = currentData.uniqueSiteVisitors;
      const previousVisitors = previousMonth.uniqueSiteVisitors;

      if (previousVisitors === 0 && currentVisitors > 0) {
        this.previousMonthIndex = 100;
      } else if (previousVisitors > 0 && currentVisitors === 0) {
        this.previousMonthIndex = -100;
      } else if (previousVisitors === 0 && currentVisitors === 0) {
        this.previousMonthIndex = 0;
      } else {
        this.previousMonthIndex = ((currentVisitors - previousVisitors) / previousVisitors) * 100;
      }
    }

    if (currentAverage && currentAverage.averageUniqueSiteVisitors !== null && currentData.uniqueSiteVisitors !== null) {
      const currentVisitors = currentData.uniqueSiteVisitors;
      const averageVisitors = currentAverage.averageUniqueSiteVisitors;

      if (averageVisitors === 0 && currentVisitors > 0) {
        this.peerComparisonIndex = 100;
      } else if (averageVisitors > 0 && currentVisitors === 0) {
        this.peerComparisonIndex = -100;
      } else if (averageVisitors === 0 && currentVisitors === 0) {
        this.peerComparisonIndex = 0;
      } else {
        this.peerComparisonIndex = ((currentVisitors - averageVisitors) / averageVisitors) * 100;
      }
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

  onCalendarChange($event: MultiSelectChangeEvent) {

  }
}
