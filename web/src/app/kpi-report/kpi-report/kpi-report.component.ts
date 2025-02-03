import {Component, HostListener, Inject, OnInit, ViewEncapsulation} from '@angular/core';
import {KpiReportService} from '../kpi-report.service';
import {KpiReport} from '../../models/kpi-report';
import {forkJoin} from 'rxjs';
import {LegendPosition} from "@swimlane/ngx-charts";
import {animate, query, stagger, state, style, transition, trigger} from "@angular/animations";
import {ActivatedRoute} from "@angular/router";
import {APP_CONFIG, AppConfig} from "../../app.config";
import {MonthlyAverage} from "../../models/monthly-average";
import {LeadSource} from "../../models/ghl/lead-source";
import {SharedUtil} from "../../util/shared-util";
import {HttpClient} from "@angular/common/http";
import {AnalyticsInsights} from "../../models/ghl/analytic-insights";
import { MenuItem } from "primeng/api";


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
    ]),
    trigger('slideRight', [
      state('default', style({ transform: 'translateX(0)' })),
      state('moved', style({ transform: 'translateX(600px)' })),
      transition('default <=> moved', animate('500ms ease-in-out')),
    ])
  ],
  encapsulation: ViewEncapsulation.None
})
export class KpiReportComponent implements OnInit {
  items: MenuItem[] | undefined;
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
  years: number[] = [2024, 2025];
  months: string[] = [];

  ghlLocationId: string | null = null;

  deviceTypes: string[] = ['PC', 'Tablet', 'Mobile'];
  selectedDevice = 'PC';

  topUrls: any[] = [];

  advancedTableOptions = false;

  analyticsInsights: AnalyticsInsights | null = null; // TODO: Update on OpenAI account init

  single = [
    {
      name: '(not set)',
      value: 344,
    },
    {
      name: 'Miami',
      value: 201,
    },
    {
      name: 'Ocala',
      value: 178,
    },
    {
      name: 'Orlando',
      value: 108,
    },
    {
      name: 'Atlanta',
      value: 87,
    },
    {
      name: 'New York',
      value: 84,
    },
    {
      name: 'Ashburn',
      value: 27,
    },
    {
      name: 'Los Angeles',
      value: 25,
    },
    {
      name: 'Tampa',
      value: 25,
    },
    {
      name: 'Chicago',
      value: 23,
    },
    {
      name: 'Gainesville',
      value: 23,
    },
    {
      name: 'Houston',
      value: 21,
    },
    {
      name: 'Jacksonville',
      value: 18,
    },
    {
      name: 'Washington',
      value: 17,
    },
    {
      name: 'Belleview',
      value: 16,
    },
    {
      name: 'Columbus',
      value: 14,
    },
    {
      name: 'Summerfield',
      value: 13,
    },
    {
      name: 'Las Vegas',
      value: 12,
    },
    {
      name: 'Dallas',
      value: 11,
    },
    {
      name: 'Philadelphia',
      value: 11,
    },
    {
      name: 'San Jose',
      value: 10,
    },
    {
      name: 'Denver',
      value: 9,
    },
    {
      name: 'Silver Springs Shores',
      value: 9,
    },
    {
      name: 'Boston',
      value: 8,
    },
    {
      name: 'Port St. Lucie',
      value: 8,
    },
    {
      name: 'Dunnellon',
      value: 7,
    },
    {
      name: 'St. Cloud',
      value: 7,
    },
    {
      name: 'Apopka',
      value: 6,
    },
    {
      name: 'Kansas City',
      value: 6,
    },
    {
      name: 'The Villages',
      value: 6,
    },
    {
      name: 'Trinity',
      value: 6,
    },
    {
      name: 'Baltimore',
      value: 5,
    },
    {
      name: 'Hialeah',
      value: 5,
    },
    {
      name: 'Keystone Heights',
      value: 5,
    },
    {
      name: 'Bay Lake',
      value: 4,
    },
    {
      name: 'Canton',
      value: 4,
    },
    {
      name: 'Charleston',
      value: 4,
    },
    {
      name: 'Clearwater',
      value: 4,
    },
    {
      name: 'Des Moines',
      value: 4,
    },
    {
      name: 'Escondido',
      value: 4,
    },
    {
      name: 'Eustis',
      value: 4,
    },
    {
      name: 'Kissimmee',
      value: 4,
    },
    {
      name: 'Largo',
      value: 4,
    },
    {
      name: 'Lebanon',
      value: 4,
    },
    {
      name: 'Live Oak',
      value: 4,
    },
    {
      name: 'Melbourne',
      value: 4,
    },
    {
      name: 'Pembroke Pines',
      value: 4,
    },
    {
      name: 'Phoenix',
      value: 4,
    },
    {
      name: 'Sacramento',
      value: 4,
    },
    {
      name: 'Wesley Chapel',
      value: 4,
    },
    {
      name: 'Winter Haven',
      value: 4,
    },
  ];

  isScrolled: boolean = false;

  activeTabIndex: number = 0;

  gradients = [
    'linear-gradient(135deg, rgba(246, 208, 255, 0.7), rgba(254, 250, 255, 0.7))', // Soft Lavender and Blush
    'linear-gradient(135deg, rgba(232, 245, 255, 0.7), rgba(208, 255, 236, 0.7))', // Sky Blue and Aqua
    'linear-gradient(135deg, rgba(230, 255, 242, 0.7), rgba(225, 236, 255, 0.7))', // Mint Green and Periwinkle
    'linear-gradient(135deg, rgba(255, 250, 232, 0.7), rgba(255, 236, 208, 0.7))', // Peach and Lemon
    'linear-gradient(135deg, rgba(255, 244, 244, 0.7), rgba(232, 245, 255, 0.7))', // Rose Quartz and Serenity
    'linear-gradient(135deg, rgba(230, 230, 255, 0.7), rgba(200, 245, 255, 0.7))', // Lilac and Baby Blue
    'linear-gradient(135deg, rgba(255, 230, 240, 0.7), rgba(230, 255, 255, 0.7))', // Pale Pink and Soft Cyan
    'linear-gradient(135deg, rgba(255, 255, 230, 0.7), rgba(230, 255, 242, 0.7))', // Lemon Yellow and Mint Green
    'linear-gradient(135deg, rgba(230, 240, 255, 0.7), rgba(255, 230, 240, 0.7))', // Baby Blue and Soft Pink
    'linear-gradient(135deg, rgba(240, 230, 255, 0.7), rgba(230, 255, 250, 0.7))', // Pastel Purple and Light Aqua
    'linear-gradient(135deg, rgba(255, 245, 225, 0.7), rgba(248, 220, 255, 0.7))', // Soft Peach and Lavender
    'linear-gradient(135deg, rgba(220, 255, 250, 0.7), rgba(220, 245, 255, 0.7))', // Aqua and Sky Blue
    'linear-gradient(135deg, rgba(248, 255, 220, 0.7), rgba(220, 255, 230, 0.7))', // Soft Yellow and Mint
    'linear-gradient(135deg, rgba(255, 225, 245, 0.7), rgba(225, 245, 255, 0.7))', // Blush Pink and Light Blue
    'linear-gradient(135deg, rgba(230, 255, 240, 0.7), rgba(220, 230, 255, 0.7))', // Mint Green and Pale Purple
    'linear-gradient(135deg, rgba(255, 240, 240, 0.7), rgba(230, 255, 240, 0.7))', // Soft Pink and Mint Green
    'linear-gradient(135deg, rgba(255, 250, 225, 0.7), rgba(240, 225, 255, 0.7))', // Light Yellow and Lavender
    'linear-gradient(135deg, rgba(240, 255, 255, 0.7), rgba(240, 225, 255, 0.7))', // Light Cyan and Lavender
    'linear-gradient(135deg, rgba(255, 225, 225, 0.7), rgba(255, 240, 225, 0.7))', // Pastel Red and Peach
    'linear-gradient(135deg, rgba(225, 245, 255, 0.7), rgba(240, 250, 255, 0.7))', // Baby Blue and White
    'linear-gradient(135deg, rgba(255, 240, 255, 0.7), rgba(245, 255, 230, 0.7))', // Lavender and Light Green
    'linear-gradient(135deg, rgba(240, 245, 255, 0.7), rgba(255, 245, 230, 0.7))', // Sky Blue and Peach
    'linear-gradient(135deg, rgba(250, 255, 240, 0.7), rgba(255, 235, 245, 0.7))', // Light Green and Soft Pink
    'linear-gradient(135deg, rgba(230, 255, 225, 0.7), rgba(240, 225, 255, 0.7))', // Lime Green and Lavender
    'linear-gradient(135deg, rgba(255, 245, 255, 0.7), rgba(225, 240, 255, 0.7))'  // Pale Pink and Baby Blue
  ];

  isOpen: boolean = false;
  hasTypedOnce: boolean = false;
  typedParagraphs: string[] = [];
  isParagraphVisible: boolean[] = [];
  typingSpeed: number = 2;

  toggleAIPanel(): void {
    if (!this.isOpen && !this.hasTypedOnce) {
      this.startTypewriterEffect();
      this.hasTypedOnce = true;
    }
    this.isOpen = !this.isOpen;
  }

  startTypewriterEffect(): void {
    const paragraphs = this.reportData!.executiveSummary.split('\n\n');
    this.typedParagraphs = Array(paragraphs.length).fill('');
    this.isParagraphVisible = Array(paragraphs.length).fill(false);

    let currentParagraphIndex = 0;

    const typeParagraph = (text: string, index: number, charIndex: number = 0) => {
      if (charIndex === 0) {
        this.isParagraphVisible[index] = true;
      }

      if (charIndex < text.length) {
        this.typedParagraphs[index] += text.charAt(charIndex);

        setTimeout(() => typeParagraph(text, index, charIndex + 1), this.typingSpeed);
      } else if (index + 1 < paragraphs.length) {
        currentParagraphIndex++;
        typeParagraph(paragraphs[currentParagraphIndex], currentParagraphIndex);
      }
    };

    typeParagraph(paragraphs[currentParagraphIndex], currentParagraphIndex);
  }

  constructor(private kpiReportService: KpiReportService,
              private route: ActivatedRoute,
              @Inject(APP_CONFIG) private config: AppConfig,
              private http: HttpClient) {
    const currentDate = new Date();
    const currentMonth = currentDate.getMonth();
    const currentYear = currentDate.getFullYear();

    this.selectedMonth = '';
    this.selectedYear = currentYear;
    if (currentMonth <= 0) {
      this.selectedYear = currentYear - 1;
    }

    this.filterAndSortMonths(currentMonth, currentYear);
  }

  transformData(data: any[]): any[] {
    return data
      .map(item => ({
        name: item.city,
        value: item.uniqueSiteVisitors
      }))
      .slice(0, 10);
  }

  getGradient(index: number): string {
    return this.gradients[index % this.gradients.length];
  }

  @HostListener('window:scroll', [])
  onWindowScroll(): void {
    const header = document.querySelector('.header-container');
    if (header) {
      if (window.scrollY > 48) {
        header.classList.add('scrolled');
        this.isScrolled = true;
      } else {
        header.classList.remove('scrolled');
        this.isScrolled = false;
      }
    }

    const sections = ['analytics', 'leads', 'sources', 'appointments', 'pipeline', 'contacts', 'website'];
    const navbarOffset = 150;
    const threshold = 72;
    let newActiveTabIndex = -1;

    for (let index = 0; index < sections.length; index++) {
      const sectionElement = document.querySelector(`#${sections[index]}`);
      if (sectionElement) {
        const boundingRect = sectionElement.getBoundingClientRect();

        if (boundingRect.top <= navbarOffset + threshold && boundingRect.bottom > navbarOffset) {
          newActiveTabIndex = index;
          break;
        }
      }
    }

    if (newActiveTabIndex !== this.activeTabIndex) {
      this.activeTabIndex = newActiveTabIndex;
    }
  }

  ngOnInit(): void {
    this.shuffleGradients();
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

    this.items = [
      { label: 'Google Analytics', icon: 'pi pi-chart-line' },
      { label: 'Captured Leads', icon: 'pi pi-chart-bar' },
      { label: 'Lead Sources', icon: 'pi pi-list' },
      { label: 'Appointments', icon: 'pi pi-phone' },
      { label: 'Pipeline Stage Conversions', icon: 'pi pi-chart-pie' },
      { label: 'Contacts Won', icon: 'pi pi-hammer' },
      { label: 'Website Analytics', icon: 'pi pi-globe' }
    ]
  }

  shuffleGradients(): void {
    for (let i = this.gradients.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [this.gradients[i], this.gradients[j]] = [this.gradients[j], this.gradients[i]];
    }
  }

  handleViewportChange(inViewport: boolean, chartId: string): void {
    if (inViewport && !this.isLoading) {
      this.isVisible[chartId] = 'I see ' + chartId;

      // const tabIndex = this.items?.findIndex(item => item.label!.toLowerCase().includes(chartId.toLowerCase()));
      // if (tabIndex !== undefined && tabIndex >= 0) {
      //   this.activeTabIndex = tabIndex;
      // }
    }
  }

  onTabClick(event: MouseEvent): void {
    const clickedTab = (event.target as HTMLElement).closest('.p-menuitem-link');
    if (!clickedTab) {
      return;
    }

    const tabs = document.querySelectorAll('.p-tabmenu .p-menuitem-link');
    const index = Array.from(tabs).indexOf(clickedTab as HTMLElement);

    if (index !== -1) {
      this.scrollToSection(index);
    }
  }

  scrollToSection(index: number): void {
    const sections = ['analytics', 'leads', 'sources', 'appointments', 'pipeline', 'contacts', 'website'];
    const sectionId = sections[index];

    const element = document.querySelector(`#${sectionId}`);
    if (element) {
      let yOffset = -140;
      if (sectionId == 'pipeline') yOffset = -120;
      if (sectionId == 'website') yOffset = -180;
      const yPosition = element.getBoundingClientRect().top + window.scrollY + yOffset;

      window.scrollTo({
        top: yPosition,
        behavior: 'smooth',
      });
    }
  }

  loadAllData(locationId: string, currentMonth: number, currentYear: number): void {
    if (currentMonth <= 0) {
      currentMonth = 12;
      currentYear = currentYear - 1;
      this.selectedYear = currentYear;
    }
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

        this.single = this.transformData(this.reportData.cityAnalytics);

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
        .slice(0, 10);
    }
  }

  onYearChange(): void {
    const currentDate = new Date();
    const currentMonth = currentDate.getMonth();
    const currentYear = currentDate.getFullYear();

    this.filterAndSortMonths(currentMonth, currentYear);

    if (this.selectedYear === currentYear) {
      this.selectedMonth = this.months[0];
    } else if (this.selectedYear < currentYear) {
      this.selectedMonth = 'December';
    } else if (this.selectedYear > currentYear) {
      this.selectedMonth = 'January';
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
