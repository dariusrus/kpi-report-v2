import {Component, HostListener, Inject, OnInit, ViewEncapsulation} from '@angular/core';
import {KpiReportService} from '../kpi-report.service';
import {KpiReport} from '../../models/kpi-report';
import {forkJoin} from 'rxjs';
import {LegendPosition} from "@swimlane/ngx-charts";
import {animate, query, stagger, state, style, transition, trigger} from "@angular/animations";
import {ActivatedRoute} from "@angular/router";
import {APP_CONFIG, AppConfig} from "../../app.config";
import {Pipeline} from "../../models/ghl/pipeline";
import {PipelineStage} from "../../models/ghl/pipeline-stage";
import {MonthlyAverage} from "../../models/monthly-average";
import {LeadSource} from "../../models/ghl/lead-source";
import {SharedUtil} from "../../util/shared-util";
import {HttpClient} from "@angular/common/http";
import {AnalyticsInsights} from "../../models/ghl/analytic-insights";


@Component({
  selector: 'app-kpi-report',
  templateUrl: './kpi-report.component.html',
  styleUrls: ['./kpi-report.component.css'],
  animations: [
    trigger('fadeIn', [
      transition('* => *', [
        query(':enter', [
          style({opacity: 0}),
          stagger(100, [
            animate('0.5s', style({opacity: 1}))
          ])
        ], {optional: true})
      ])
    ]),
    trigger('dropIn', [
      state('void', style({
        opacity: 0,
        transform: 'translateY(-20px) translateX(-50%)'
      })),
      state('*', style({
        opacity: 1,
        transform: 'translateY(0) translateX(-50%)'
      })),
      transition(':enter', [
        style({
          opacity: 0,
          transform: 'translateY(-20px) translateX(-50%)'
        }),
        animate('300ms ease-in')
      ]),
      transition(':leave', [
        animate('200ms ease-out', style({
          opacity: 0,
          transform: 'translateY(0) translateX(-50%)'
        }))
      ])
    ])
  ],
  encapsulation: ViewEncapsulation.None
})
export class KpiReportComponent implements OnInit {
  clarityAverageScrollGaugeChart: any[] = [];
  clarityTotalSessionsPieChart: any[] = [];
  clarityTotalActiveTimeTreeMap: any[] = [];

  reportData: KpiReport | null = null;
  averageReportData: MonthlyAverage | null = null;
  reportDataPrevious: KpiReport[] = [];
  averageReportDataPrevious: MonthlyAverage[] = [];
  reportDataPreviousMap: [KpiReport, MonthlyAverage][] = [];
  clientType: string = '';

  monthCount: number = 1;
  isLoading: boolean = true;

  displayComparisons: boolean = false;
  averageLabel: string = '';

  selectedMonth: string;
  selectedYear: number;
  isVisible: { [key: string]: string } = {};

  gradient: boolean = true;

  allMonths: string[] = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];
  years: number[] = [2024];
  months: string[] = [];

  ghlLocationId: string | null = null;

  deviceTypes: string[] = ['PC', 'Tablet', 'Mobile'];
  selectedDevice = 'PC';

  topUrls: any[] = [];

  advancedTableOptions = false;

  analyticsInsights: AnalyticsInsights | null = null; // TODO: Update on OpenAI account init

  constructor(private kpiReportService: KpiReportService,
              private route: ActivatedRoute,
              @Inject(APP_CONFIG) private config: AppConfig,
              private http: HttpClient) {
    const currentDate = new Date();
    const currentMonth = currentDate.getMonth();
    const currentYear = currentDate.getFullYear();

    this.selectedMonth = '';
    this.selectedYear = currentYear;

    this.filterAndSortMonths(currentMonth, currentYear);
  }

  @HostListener('window:scroll', [])
  onWindowScroll(): void {
    const header = document.querySelector('.header-container');
    if (header) {
      if (window.scrollY > 48) {
        header.classList.add('scrolled');
      } else {
        header.classList.remove('scrolled');
      }
    }
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.ghlLocationId = params.get('id');
      if (this.ghlLocationId) {
        this.years = this.years.sort((a, b) => b - a);

        this.selectedMonth = this.months[0];
        this.selectedYear = this.years[0];

        const currentDate = new Date();
        const currentMonth = currentDate.getMonth();
        const currentYear = currentDate.getFullYear();

        this.loadAllData(this.ghlLocationId, currentMonth, currentYear);
      }
    });
  }

  handleViewportChange(inViewport: boolean, chartId: string): void {
    if (inViewport && !this.isLoading) {
      this.isVisible[chartId] = 'I see ' + chartId;
    }
  }

  loadAllData(locationId: string, currentMonth: number, currentYear: number): void {
    this.isLoading = true;
    this.monthCount = 1 + this.config.previousMonthsCount;
    const previousMonths = this.getPreviousMonths(currentMonth, currentYear, this.config.previousMonthsCount);

    this.kpiReportService.getReportData(locationId, currentMonth, currentYear).subscribe({
      next: (currentData) => {
        if (!currentData) {
          console.error('No data received for the current month. Aborting operation.');
          this.reportData = null;
          this.isLoading = false;
          return;
        }

        this.reportData = currentData;
        this.preprocessLeadSources();
        this.preprocessDevices(this.selectedDevice);

        this.clientType = this.reportData.clientType;
        this.averageLabel = this.clientType === 'REMODELING'
          ? 'Toggle Remodeling Average'
          : this.clientType === 'CUSTOM_HOMES'
            ? 'Toggle Custom Homes Average'
            : 'Toggle BLC Average';

        if (!this.clientType) {
          console.error('Client type is undefined. Cannot proceed with fetching monthly averages.');
          this.isLoading = false;
          return;
        }

        this.kpiReportService.getMonthlyAverage(currentMonth, currentYear, this.clientType).subscribe({
          next: (currentAverage) => {
            this.averageReportData = currentAverage;

            const observables = previousMonths.map(([month, year]) =>
              forkJoin([
                this.kpiReportService.getReportData(locationId, month, year),
                this.kpiReportService.getMonthlyAverage(month, year, this.clientType)
              ])
            );

            forkJoin(observables).subscribe({
              next: (previousData) => {
                this.reportDataPrevious = [];
                this.averageReportDataPrevious = [];
                this.selectedDevice = 'PC';

                previousData.forEach(([report, average]) => {
                  if (report) {
                    if (!report.uniqueSiteVisitors) {
                      report.opportunityToLead = 0;
                    }
                    this.reportDataPrevious.push(report);
                    this.averageReportDataPrevious.push(average);
                  }
                });

                this.reportDataPreviousMap = previousData;
                this.populateChartsAndCards(currentData, currentAverage, previousData);
                this.isLoading = false;
                console.log(this.reportData);
              },
              error: (error) => {
                console.error('Failed to fetch previous months\' KPI report data:', error);
                this.isLoading = false;
              }
            });
          },
          error: (error) => {
            console.error('Failed to fetch current month\'s monthly average:', error);
            this.isLoading = false;
          }
        });
      },
      error: (error) => {
        console.error('Failed to fetch current month\'s KPI report data:', error);
        this.isLoading = false;
      }
    });
  }

  private preprocessLeadSources() {
    if (this.reportData && this.reportData.websiteLead && this.reportData.websiteLead.leadSource) {
      this.reportData.websiteLead.leadSource = this.reportData.websiteLead.leadSource
        .sort((a: LeadSource, b: LeadSource) => {
          if (a.leadType > b.leadType) return -1;
          if (a.leadType < b.leadType) return 1;

          const aSourceInvalid = !a.source || a.source === "Unspecified";
          const bSourceInvalid = !b.source || b.source === "Unspecified";

          if (aSourceInvalid && !bSourceInvalid) return 1;
          if (!aSourceInvalid && bSourceInvalid) return -1;
          if (aSourceInvalid && bSourceInvalid) return 0;

          if (a.source < b.source) return -1;
          if (a.source > b.source) return 1;

          return 0;
        });
    }
  }

  populateChartsAndCards(currentData: KpiReport, currentAverage: MonthlyAverage, previousData: [KpiReport, MonthlyAverage][]): void {
    this.setupClarityAverageScrollGaugeChart(currentData);
    this.setupClarityTotalSessionPieChart(currentData);
    this.setupClarityTotalActiveTimeTreeMap(currentData);
  }

  private setupClarityAverageScrollGaugeChart(currentData: KpiReport) {
    const monthlyClarityReport = currentData.monthlyClarityReport;
    if (monthlyClarityReport) {
      const deviceClarityAggregate = monthlyClarityReport.deviceClarityAggregate;
      this.clarityAverageScrollGaugeChart = deviceClarityAggregate
        .filter(device => device.collectiveAverageScrollDepth !== null && device.collectiveAverageScrollDepth !== undefined)
        .map(device => ({
          name: device.deviceName,
          value: device.collectiveAverageScrollDepth
        }));
    }
  }

  private setupClarityTotalSessionPieChart(currentData: KpiReport): void {
    const monthlyClarityReport = currentData.monthlyClarityReport;
    if (monthlyClarityReport) {
      const deviceClarityAggregate = monthlyClarityReport.deviceClarityAggregate;
      this.clarityTotalSessionsPieChart = deviceClarityAggregate
        .filter(device => device.totalSessions !== null && device.totalSessions !== 0)
        .map(device => ({
          name: device.deviceName,
          value: device.totalSessions
        }));
    }
  }

  private setupClarityTotalActiveTimeTreeMap(currentData: KpiReport): void {
    const monthlyClarityReport = currentData.monthlyClarityReport;
    if (monthlyClarityReport) {
      const deviceClarityAggregate = monthlyClarityReport.deviceClarityAggregate;
      this.clarityTotalActiveTimeTreeMap = deviceClarityAggregate
        .filter(device => device.deviceName !== 'Other' && device.totalActiveTime !== null && device.totalActiveTime !== 0)
        .map(device => ({
          name: device.deviceName,
          value: device.totalActiveTime
        }));
    }
  }

  onDeviceChange(event: any): void {
    this.selectedDevice = event.value;
    this.preprocessDevices(this.selectedDevice);
  }

  getPreviousMonths(currentMonth: number, currentYear: number, count: number): [number, number][] {
    const previousMonths: [number, number][] = [];
    for (let i = 1; i <= count; i++) {
      let prevMonth = currentMonth - i;
      let prevYear = currentYear;

      if (prevMonth < 1) {
        prevMonth += 12;
        prevYear -= 1;
      }

      previousMonths.push([prevMonth, prevYear]);
    }
    return previousMonths;
  }

  filterAndSortMonths(currentMonth: number, currentYear: number): void {
    if (this.selectedYear === currentYear) {
      this.months = this.allMonths.slice(0, currentMonth).reverse();
    } else {
      this.months = this.allMonths.slice().reverse();
    }
  }

  preprocessDevices(selectedDevice: string) {
    if (this.reportData?.monthlyClarityReport) {
      this.reportData!.monthlyClarityReport.urls.forEach((urlMetric: any) => {
        const toggleDevice = urlMetric.devices.find((device: any) => device.deviceType === selectedDevice);
        urlMetric.averageScrollDepth = toggleDevice ? toggleDevice.averageScrollDepth : 0;
        urlMetric.activeTime = toggleDevice ? toggleDevice.activeTime : 0;
        urlMetric.totalSessionCount = toggleDevice ? toggleDevice.totalSessionCount : 0;
      });


      this.topUrls = this.reportData.monthlyClarityReport.urls.map((urlMetric: any) => {
        const totalSessionCount = urlMetric.devices.reduce((sum: number, device: any) => sum + (device.totalSessionCount || 0), 0);
        const totalAverageScrollDepth = urlMetric.devices.reduce((sum: number, device: any) => sum + (device.averageScrollDepth || 0), 0);
        const totalActiveTime = urlMetric.devices.reduce((sum: number, device: any) => sum + (device.activeTime || 0), 0);

        return {
          ...urlMetric,
          totalSessionCount,
          totalAverageScrollDepth,
          totalActiveTime
        };
      })
        .sort((a, b) => b.totalSessionCount - a.totalSessionCount)
        .slice(0, 5);
    }
  }


  onYearChange(): void {
    const currentDate = new Date();
    const currentMonth = currentDate.getMonth();
    const currentYear = currentDate.getFullYear();

    this.filterAndSortMonths(currentMonth, currentYear);

    if (this.selectedYear === currentYear) {
      this.selectedMonth = this.months[0];
    } else if (!this.months.includes(this.selectedMonth)) {
      this.selectedMonth = this.months[0];
    }

    const selectedMonthIndex = this.allMonths.indexOf(this.selectedMonth) + 1;
    this.loadAllData(this.ghlLocationId!, selectedMonthIndex, this.selectedYear);
  }

  onMonthChange(): void {
    const selectedMonthIndex = this.allMonths.indexOf(this.selectedMonth) + 1;
    this.loadAllData(this.ghlLocationId!, selectedMonthIndex, this.selectedYear);
  }


  protected readonly LegendPosition = LegendPosition;
  protected readonly SharedUtil = SharedUtil;
}
