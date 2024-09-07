import {Component, HostListener, Inject, OnInit} from '@angular/core';
import {KpiReportService} from '../kpi-report.service';
import {KpiReport} from '../../models/kpi-report';
import {forkJoin} from 'rxjs';
import {LegendPosition} from "@swimlane/ngx-charts";
import {animate, query, stagger, style, transition, trigger} from "@angular/animations";
import {ActivatedRoute} from "@angular/router";
import {APP_CONFIG, AppConfig} from "../../app.config";
import * as shape from 'd3-shape';
import {Pipeline} from "../../models/ghl/pipeline";
import {PipelineStage} from "../../models/ghl/pipeline-stage";
import {MonthlyAverage} from "../../models/monthly-average";
import {TotalAppointment} from "../../models/ghl/total-appointment";
import {Calendar} from "../../models/ghl/calendar";
import {Appointment} from "../../models/ghl/appointment";
import {SelectItemGroup} from "primeng/api";
import {LeadSource} from "../../models/ghl/lead-source";

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
    ])
  ]
})
export class KpiReportComponent implements OnInit {
  numberCards: any[] = [];
  gaBarChart: any[] = [];
  gaGroupedBarChart: any[] = [];
  leadBarChart: any[] = [];
  leadBarChartStacked: any[] = [];
  leadBarChartStackedValuation: any[] = [];
  leadGroupedBarChart: any[] = [];
  leadSourceBarChart: any[] = [];
  opportunityLineChart: any[] = [];
  opportunityGroupedLineChart: any[] = [];
  appointmentsPieChart: any[] = [];
  appointmentsLineChart: any[] = [];
  pipelinesPieChart: any[] = [];
  clarityAverageScrollGaugeChart: any[] = [];
  clarityTotalSessionsPieChart: any[] = [];
  clarityTotalActiveTimeTreeMap: any[] = [];

  reportData: KpiReport | null = null;
  averageReportData: MonthlyAverage | null = null;
  reportDataPrevious: KpiReport[] = [];
  averageReportDataPrevious: MonthlyAverage[] = [];
  clientType: string = '';

  selectedPipeline: Pipeline | null = null;
  selectedPipelineNoData: boolean = false;

  selectedCalendar: Calendar | null = null;

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

  showXAxis = true;
  showYAxis = true;
  showXAxisLabel = true;
  showYAxisLabel = true;

  xAxisLabel = 'Month & Year';

  ghlLocationId: string | null = null;

  curve: any = shape.curveCatmullRom.alpha(1);

  deviceTypes: string[] = ['PC', 'Tablet', 'Mobile'];
  selectedDevice = 'PC';

  availableAppointmentStatuses: string[] = [];
  totalAppointments: TotalAppointment[] = [];
  filteredCalendars: Calendar[] = [];

  advancedPieChartView: [number, number] = [500, 250];
  pieChartView: [number, number] = [500, 425];

  customColors = [
    { name: 'Website Lead', value: '#37C469FF' }, // Green color for Website Lead
    { name: 'Manual User Input', value: '#4571B9FF' }
  ];

  customColorsValue = [
    { name: 'Website Lead', value: '#76c437' }, // Green color for Website Lead
    { name: 'Manual User Input', value: '#45abb9' }
  ];


  constructor(private kpiReportService: KpiReportService,
              private route: ActivatedRoute,
              @Inject(APP_CONFIG) private config: AppConfig) {
    const currentDate = new Date();
    const currentMonth = currentDate.getMonth(); // hack
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

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.updateChartView();  // Update view size on window resize
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
        this.updateChartView();
      }
    });
  }

  updateChartView() {
    if (window.innerWidth >= 1400) {
      this.advancedPieChartView = [700, 250];
      this.pieChartView = [600, 425];
    } else if (window.innerWidth < 1400 && window.innerWidth >= 1200) {
      this.advancedPieChartView = [600, 250];
      this.pieChartView = [500, 425];
    } else {
      this.advancedPieChartView = [500, 250];
      this.pieChartView = [400, 425];
    }
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

    // Fetch the current month's report data first to retrieve the clientType
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
        this.preprocessCalendars(this.reportData.calendars);
        console.log('DATA LOADED!');
        console.log(this.reportData);

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

        // Now that we have the clientType, fetch the monthly average for the current month
        this.kpiReportService.getMonthlyAverage(currentMonth, currentYear, this.clientType).subscribe({
          next: (currentAverage) => {
            this.averageReportData = currentAverage;

            // Proceed to fetch previous months' data and averages
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

                if (this.reportData?.pipelines.length) {
                  this.updateSelectedPipeline(this.reportData.pipelines[0]);
                }
                this.populateChartsAndCards(currentData, currentAverage, previousData);
                this.isLoading = false;
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
    this.populateNumberCards(currentData);
    this.populateGaBarChart(currentData, previousData);
    this.setupClarityAverageScrollGaugeChart(currentData);
    this.setupClarityTotalSessionPieChart(currentData);
    this.setupClarityTotalActiveTimeTreeMap(currentData);
    this.populateGaGroupedBarChart(currentData, previousData, currentAverage);
    this.populateLeadBarChart(currentData, previousData);
    this.populateLeadBarChartStacked(currentData, previousData);
    this.populateLeadBarChartStackedValuation(currentData, previousData);
    this.populateLeadGroupedBarChart(currentData, previousData, currentAverage);
    this.populateLeadSourceBarChart();
    this.populateOpportunityLineChart(currentData, previousData);
    this.populateOpportunityGroupedLineChart(currentData, previousData, currentAverage);
    // this.setupAppointmentsLineChart(currentData, previousData.map(([report]) => report));
    this.setupAppointmentsPieChart(currentData);
    this.setupPipelinesPieChart();
  }

  private populateNumberCards(currentData: KpiReport): void {
    this.numberCards = [
      {name: "Unique Site Visitors", value: currentData.uniqueSiteVisitors},
      {name: "Total Captured Leads", value: currentData.websiteLead.totalLeads},
      {name: "Opportunity-to-Lead", value: currentData.opportunityToLead},
      {name: "Lead Valuation", value: currentData.websiteLead.totalValues}
    ];
  }

  private populateGaBarChart(currentData: KpiReport, previousData: any[]): void {
    this.gaBarChart = [
      ...previousData
        .filter(([report]) => report !== null && report.uniqueSiteVisitors !== null)
        .map(([report]) => ({
          name: this.formatMonthAndYear(report.monthAndYear),
          value: report.uniqueSiteVisitors
        })).reverse(),
      {
        name: this.formatMonthAndYear(currentData.monthAndYear),
        value: currentData.uniqueSiteVisitors
      }
    ];
  }

  private populateGaGroupedBarChart(currentData: KpiReport, previousData: any[], currentAverage: MonthlyAverage): void {
    this.gaGroupedBarChart = [
      ...previousData
        .filter(([report, average]) =>
          report?.monthAndYear &&
          report.uniqueSiteVisitors !== null &&
          average &&
          average.averageUniqueSiteVisitors !== null
        )
        .map(([report, average]) => ({
          name: this.formatMonthAndYear(report.monthAndYear),
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
        name: this.formatMonthAndYear(currentData.monthAndYear),
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

  private populateLeadBarChart(currentData: KpiReport, previousData: any[]): void {
    this.leadBarChart = [
      ...previousData
        .filter(([report]) =>
          report?.monthAndYear &&
          report.websiteLead &&
          report.websiteLead.totalLeads !== null
        )
        .map(([report]) => ({
          name: this.formatMonthAndYear(report.monthAndYear),
          value: report.websiteLead.totalLeads
        })).reverse(),
      currentData?.monthAndYear &&
      currentData.websiteLead &&
      currentData.websiteLead.totalLeads !== null ? {
        name: this.formatMonthAndYear(currentData.monthAndYear),
        value: currentData.websiteLead.totalLeads
      } : null
    ].filter(Boolean);
  }

  private populateLeadBarChartStacked(currentData: KpiReport, previousData: any[]): void {
    this.leadBarChartStacked = previousData
      .filter(([report]) =>
        report?.monthAndYear &&
        report.websiteLead &&
        report.websiteLead.totalManualLeads !== null &&
        report.websiteLead.totalWebsiteLeads !== null
      )
      .map(([report]) => ({
        name: this.formatMonthAndYear(report.monthAndYear),
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
      this.leadBarChartStacked.push({
        name: this.formatMonthAndYear(currentData.monthAndYear),
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

  private populateLeadBarChartStackedValuation(currentData: KpiReport, previousData: any[]): void {
    this.leadBarChartStackedValuation = previousData
      .filter(([report]) =>
        report?.monthAndYear &&
        report.websiteLead &&
        report.websiteLead.totalManualValuation !== null &&
        report.websiteLead.totalWebsiteValuation !== null
      )
      .map(([report]) => ({
        name: this.formatMonthAndYear(report.monthAndYear),
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
      this.leadBarChartStackedValuation.push({
        name: this.formatMonthAndYear(currentData.monthAndYear),
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

  private populateLeadGroupedBarChart(currentData: KpiReport, previousData: any[], currentAverage: MonthlyAverage): void {
    this.leadGroupedBarChart = [
      ...previousData
        .filter(([report, average]) =>
          report?.monthAndYear &&
          report.websiteLead &&
          report.websiteLead.totalLeads !== null &&
          average &&
          average.averageTotalLeads !== null
        )
        .map(([report, average]) => ({
          name: this.formatMonthAndYear(report.monthAndYear),
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
        name: this.formatMonthAndYear(currentData.monthAndYear),
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
    ].filter(Boolean); // Filter out any null values
  }

  private populateLeadSourceBarChart(): void {
    if (this.reportData?.websiteLead?.leadSource) {
      this.leadSourceBarChart = this.reportData.websiteLead.leadSource
        .map(leadSource => ({
          name: leadSource.source || '-',
          value: leadSource.totalLeads || 0
        }))
        .sort((a, b) => b.value - a.value);
    } else {
      this.leadSourceBarChart = [];
    }
  }

  private populateOpportunityLineChart(currentData: KpiReport, previousData: any[]): void {
    this.opportunityLineChart = [
      {
        name: "Opportunity-to-Lead (O2L)",
        series: [
          ...previousData
            .filter(([report]) =>
              report?.monthAndYear &&
              report.opportunityToLead !== null
            )
            .map(([report]) => ({
              name: this.formatMonthAndYear(report.monthAndYear),
              value: report.opportunityToLead
            })).reverse(),
          currentData?.monthAndYear &&
          currentData.opportunityToLead !== null ? {
            name: this.formatMonthAndYear(currentData.monthAndYear),
            value: currentData.opportunityToLead
          } : null
        ].filter(Boolean)
      }
    ];
  }

  private populateOpportunityGroupedLineChart(currentData: KpiReport, previousData: any[], currentAverage: MonthlyAverage): void {
    this.opportunityGroupedLineChart = [
      {
        name: "Opportunity-to-Lead (O2L)",
        series: [
          ...previousData
            .filter(([report]) =>
              report?.monthAndYear &&
              report.opportunityToLead !== null
            )
            .map(([report]) => ({
              name: this.formatMonthAndYear(report.monthAndYear),
              value: report.opportunityToLead
            })).reverse(),
          currentData?.monthAndYear &&
          currentData.opportunityToLead !== null ? {
            name: this.formatMonthAndYear(currentData.monthAndYear),
            value: currentData.opportunityToLead
          } : null
        ].filter(Boolean) // Filter out any null values
      },
      {
        name: "Weighted Average",
        series: [
          ...this.averageReportDataPrevious
            .filter(average =>
              average?.monthAndYear &&
              average.weightedAverageOpportunityToLead !== null
            )
            .map(average => ({
              name: this.formatMonthAndYear(average.monthAndYear),
              value: average.weightedAverageOpportunityToLead
            })).reverse(),
          currentAverage?.monthAndYear &&
          currentAverage.weightedAverageOpportunityToLead !== null ? {
            name: this.formatMonthAndYear(currentAverage.monthAndYear),
            value: currentAverage.weightedAverageOpportunityToLead
          } : null
        ].filter(Boolean) // Filter out any null values
      },
      {
        name: "Non-weighted Average",
        series: [
          ...this.averageReportDataPrevious
            .filter(average =>
              average?.monthAndYear &&
              average.averageOpportunityToLead !== null
            )
            .map(average => ({
              name: this.formatMonthAndYear(average.monthAndYear),
              value: average.averageOpportunityToLead
            })).reverse(),
          currentAverage?.monthAndYear &&
          currentAverage.averageOpportunityToLead !== null ? {
            name: this.formatMonthAndYear(currentAverage.monthAndYear),
            value: currentAverage.averageOpportunityToLead
          } : null
        ].filter(Boolean) // Filter out any null values
      }
    ];
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

  private updateSelectedPipeline(pipeline: Pipeline | null): void {
    this.selectedPipeline = pipeline;
    this.selectedPipelineNoData = !pipeline?.pipelineStages.some(stage => stage.count > 0);
  }

  private setupPipelinesPieChart(): void {
    if (this.reportData && this.reportData.pipelines.length > 0) {
      const pipelineStages = this.selectedPipeline!.pipelineStages;
      this.pipelinesPieChart = pipelineStages.map(stage => ({
        name: stage.stageName,
        value: stage.count
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

  onPipelineChange(event: any): void {
    this.updateSelectedPipeline(event.value);
    this.setupPipelinesPieChart();
  }

  onDeviceChange(event: any): void {
    this.selectedDevice = event.value;
    this.preprocessDevices(this.selectedDevice);
  }

  getTotalCounts(stages: PipelineStage[]): number {
    return stages.reduce((acc, cur) => acc + cur.count, 0);
  }

  getTotalPercentage(stages: PipelineStage[]): number {
    return Math.ceil(stages.reduce((acc, cur) => acc + cur.percentage, 0));
  }

  getTotalMonetaryValue(stages: PipelineStage[]): number {
    return stages.reduce((acc, cur) => acc + cur.monetaryValue, 0);
  }

  calculateTotalAppointments(calendars: Calendar[]): TotalAppointment[] {
    const statusCountMap: { [status: string]: number } = {};

    calendars.forEach(calendar => {
      calendar.appointments.forEach(appointment => {
        const status = appointment.status;
        const count = appointment.count;

        if (statusCountMap[status]) {
          statusCountMap[status] += count;
        } else {
          statusCountMap[status] = count;
        }
      });
    });

    return Object.keys(statusCountMap).map(status => ({
      status,
      totalCount: statusCountMap[status]
    }));
  }

  // Function to get the total count for a specific status for the footer
  getTotalAppointmentCount(status: string): number {
    const totalAppointment = this.totalAppointments.find(app => app.status === status);
    return totalAppointment ? totalAppointment.totalCount : 0;
  }

  normalizeAndSortAppointments(calendars: Calendar[]): Calendar[] {
    const allStatuses: Set<string> = new Set();
    calendars.forEach(calendar => {
      calendar.appointments.forEach(appointment => {
        allStatuses.add(appointment.status);
      });
    });
    return calendars.map(calendar => {
      const updatedAppointments: Appointment[] = Array.from(allStatuses).map(status => {
        const existingAppointment = calendar.appointments.find(app => app.status === status);
        return existingAppointment ? existingAppointment : { status, count: 0 };
      });
      updatedAppointments.sort((a, b) => a.status.localeCompare(b.status));
      return {
        ...calendar,
        appointments: updatedAppointments
      };
    });
  }

  setupAppointmentsPieChart(currentData: KpiReport) {
    currentData.calendars = currentData.calendars.filter(calendar => calendar.calendarId !== 'all-calendars');
    const totalAppointments = this.calculateTotalAppointments(currentData.calendars);
    const allCalendars: Calendar = {
      calendarId: 'all-calendars',
      calendarName: 'All Calendars',
      appointments: totalAppointments.map(totalAppointment => ({
        status: totalAppointment.status,
        count: totalAppointment.totalCount
      }))
    };

    currentData.calendars.unshift(allCalendars);

    // Step 5: Preserve the selected calendar, defaulting to "All Calendars" if none is selected
    if (!this.selectedCalendar || !currentData.calendars.find(calendar => calendar.calendarId === this.selectedCalendar!.calendarId)) {
      this.selectedCalendar = allCalendars;
    }

    this.appointmentsPieChart = this.selectedCalendar.appointments.map(appointment => ({
      name: this.formatStatus(appointment.status),
      value: appointment.count
    })).sort((a, b) => a.name.localeCompare(b.name));

    this.groupCalendars();
  }

  onCalendarChange(event: any): void {
    this.updateSelectedCalendar(event.value);
    this.setupAppointmentsPieChart(this.reportData!);
  }

  private updateSelectedCalendar(calendar: Calendar | null): void {
    this.selectedCalendar = calendar;
  }

  formatStatus(status: string): string {
    if (status.toLowerCase() === 'noshow') {
      return 'No Show';
    }
    return status.charAt(0).toUpperCase() + status.slice(1).toLowerCase();
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
    }
  }

  preprocessCalendars(calendars: Calendar[]) {
    this.selectedCalendar = null;
    if (calendars) {
      this.reportData!.calendars = this.normalizeAndSortAppointments(calendars);
      this.availableAppointmentStatuses = this.getAvailableStatuses(this.reportData!.calendars);
      this.totalAppointments = this.calculateTotalAppointments(calendars);
    }
  }

  groupCalendars() {
    const withAppointments: Calendar[] = [];
    const withoutAppointments: Calendar[] = [];

    this.reportData!.calendars.forEach(calendar => {
      const totalAppointments = calendar.appointments.reduce((sum, appointment) => sum + appointment.count, 0);
      if (totalAppointments > 0) {
        withAppointments.push(calendar);
      } else {
        withoutAppointments.push(calendar);
      }
    });
    this.filteredCalendars = withAppointments;
  }

  getAvailableStatuses(calendars: Calendar[]): string[] {
    const statusesSet = new Set<string>();

    calendars.forEach(calendar => {
      calendar.appointments.forEach(appointment => {
        statusesSet.add(appointment.status);
      });
    });

    return Array.from(statusesSet).sort();
  }

  // Function to get the count of a specific appointment status
  getAppointmentCount(calendar: Calendar, status: string): number {
    const appointment = calendar.appointments.find(app => app.status === status);
    return appointment ? appointment.count : 0;
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

  valueFormatting(data: any): string {
    if (data.label === 'Opportunity-to-Lead') {
      return data.value.toFixed(2) + '%';
    }
    if (data.label === 'Lead Valuation') {
      return '$ ' + Math.round(data.value).toLocaleString();
    }
    if (!data.label) {
      return data + '%';
    }
    return Math.round(data.value).toLocaleString();
  }

  currencyFormatting(data: any): string {
    return '$ ' + Math.round(data).toLocaleString();
  }

  percentageFormatting(data: any): string {
    if (data === 0) {return '0%'}
    if (data === undefined || data === null || data === 'N/A') {
      return '-';
    }
    return data + '%';
  }

  getStatusSeverity(status: string) {
    switch (status.toLowerCase()) {
      case 'open':
        return 'info';
      case 'won':
        return 'success';
      case 'lost':
        return 'danger';
      case 'abandoned':
        return 'warning';
      default:
        return 'contrast';  // You can choose to handle unexpected values with a default case
    }
  }

  timeFormatting(data: number): string {
    if (data === 0) {return '0s'}
    const secondsInMinute = 60;
    const secondsInHour = secondsInMinute * 60;
    const secondsInDay = secondsInHour * 24;
    const secondsInWeek = secondsInDay * 7;

    const weeks = Math.floor(data / secondsInWeek);
    data %= secondsInWeek;

    const days = Math.floor(data / secondsInDay);
    data %= secondsInDay;

    const hours = Math.floor(data / secondsInHour);
    data %= secondsInHour;

    const minutes = Math.floor(data / secondsInMinute);
    const seconds = data % secondsInMinute;

    const parts = [];
    if (weeks > 0) parts.push(`${weeks}w`);
    if (days > 0) parts.push(`${days}d`);
    if (hours > 0) parts.push(`${hours}h`);
    if (minutes > 0) parts.push(`${minutes}m`);
    if (seconds > 0) parts.push(`${seconds}s`);

    return parts.join(' ');
  }

  formatMonthAndYear(monthAndYear: string): string {
    const [month, year] = monthAndYear.split(', ');
    return `${month.slice(0, 3)}, ${year}`;
  }

  getInitials(name: string): string {
    if (!name || name === '') return 'N/A';
    const ignoreWords = ["and", "or"];
    let initials = name
      .split(' ')
      .filter(word => !ignoreWords.includes(word.toLowerCase()))
      .map(n => n[0])
      .join('');
    return initials.toUpperCase();
  }

  getRandomColor(name: string): string {
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
      hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    let color = '#';
    for (let i = 0; i < 3; i++) {
      let value = (hash >> (i * 8)) & 0xFF;
      value = Math.floor((value * 0.4) + 153);
      color += ('00' + value.toString(16)).substr(-2);
    }
    return color;
  }

  protected readonly LegendPosition = LegendPosition;
}


// getAllStatuses(reports: KpiReport[]): Set<string> {
//   const allStatuses = new Set<string>();
//   reports.forEach(report => report.appointments.forEach(app => allStatuses.add(app.status)));
//   return allStatuses;
// }

// setupAppointmentsLineChart(currentData: KpiReport, previousData: KpiReport[]): void {
//   const combinedData = [currentData, ...previousData].reverse();
//
//   const allStatuses = Array.from(this.getAllStatuses(combinedData)).sort();
//
//   this.appointmentsLineChart = allStatuses.map(status => ({
//     name: this.formatStatus(status),
//     series: combinedData.map(data => ({
//       name: this.formatMonthAndYear(data.monthAndYear),
//       value: data.appointments.find(app => app.status === status)?.count || 0
//     }))
//   }));
// }
