import {Component, HostListener, Input, OnInit} from '@angular/core';
import { KpiReport } from "../../models/kpi-report";
import { MonthlyAverage } from "../../models/monthly-average";
import { Pipeline } from "../../models/ghl/pipeline";
import {PipelineStage} from "../../models/ghl/pipeline-stage";
import {LegendPosition} from "@swimlane/ngx-charts";
import {SharedUtil} from "../../util/shared-util";
import {SalesPersonConversation} from "../../models/ghl/sales-person-conversation";
import * as shape from "d3-shape";
import {dropInAnimation} from "../../util/animations";

interface SalesPerson {
  id: string;
  name: string;
  imageUrl: string;
  cachedImage?: string;
}

@Component({
  selector: 'app-sales-pipeline-report',
  templateUrl: './sales-pipeline-report.component.html',
  styleUrls: ['./sales-pipeline-report.component.css'],
  animations: [dropInAnimation]
})
export class SalesPipelineReportComponent implements OnInit {
  @Input() reportData!: KpiReport;
  @Input() reportDataPreviousMap: [KpiReport, MonthlyAverage][] = [];
  @Input() averageReportData!: MonthlyAverage;
  @Input() isVisible!: { [key: string]: string };
  @Input() monthCount!: number;
  @Input() displayComparisons!: boolean;

  data: any[] = [];
  followupData: any[] = [];
  selectedPipeline: Pipeline | null = null;
  selectedPipelineNoData: boolean = false;
  pieChartView: [number, number] = [580, 350];
  lineChartView: [number, number] = [500, 300];
  scheme = 'picnic';

  salesPersons: SalesPerson[] = [];
  selectedSalesPersons: SalesPerson[] = [];
  totalConversions: number = 0;

  totalSmsCount: number = 0;
  totalEmailCount: number = 0;
  totalCallCount: number = 0;
  totalLiveChatCount: number = 0;
  totalFollowups: number = 0;
  filteredConversations: SalesPersonConversation[] = [];
  filteredFollowUpConversions: any[] = [];

  stageConversionIndex: number = 0;
  followUpIndex: number = 0;

  isScrolled = false;

  pipelineStageData: any[] = [];
  pipelineView: [number, number] = [1150, 400];
  pipelineStages: PipelineStage[] = [];
  selectedPipelineStages: PipelineStage[] = [];
  selectedPipelineStagesLabel: string = 'All stages selected';

  conversionTimeline: { month: string, totalConversions: number }[] = [];

  followUpPerConversionData: any[] = [];
  xAxisLabel = 'Month & Year';
  yAxisLabel = 'Follow Up per Conversion';
  curve: any = shape.curveCatmullRom.alpha(1);

  peerComparisonTooltip = false;
  peerFollowUpTooltip = false;
  peerConversionTooltip = false;
  timelineInfoTooltip = false;
  fpcInfoTooltip = false;

  @HostListener('window:scroll', [])
  onWindowScroll() {
    const pipelineRow = document.querySelector('.sales-pipeline-row') as HTMLElement;
    if (pipelineRow) {
      const rect = pipelineRow.getBoundingClientRect();
      this.isScrolled = rect.top >= 110 && rect.top <= 130;
    }
  }

  ngOnInit() {
    if (this.reportData?.pipelines?.length) {
      this.initializeSalesPersons();
      if (this.salesPersons.length > 0) {
        this.selectedSalesPersons = [this.salesPersons[0]];
      }
      this.updateSelectedPipeline(this.reportData.pipelines[0]);
      this.salesPersons = this.getUniqueSalesPersonNames(this.reportData.pipelines);
      this.selectedSalesPersons = this.salesPersons;
      this.preloadImages();
    }

    this.filterConversations();
    this.updateTotalCounts();

    this.setupChart();
    this.setupFollowupChart();
    this.populateFollowUpPerConversionChart(this.reportData, this.reportDataPreviousMap);
    this.updateChartView();
    this.computeIndices();
    this.updateFilteredFollowUpConversions();
    this.initializePipelineStages();
    this.populatePipelineChart();
    this.populateConversionTimeline();
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.updateChartView();
  }

  showTooltip(hoveredObject: string) {
    this.hideAllTooltips();

    if (hoveredObject === 'timelineInfoTooltip') {
      this.timelineInfoTooltip = true;
    } else if (hoveredObject === 'peerComparisonTooltip') {
      this.peerComparisonTooltip = true;
    } else if (hoveredObject === 'fpcInfoTooltip') {
      this.fpcInfoTooltip = true;
    } else if (hoveredObject === 'peerFollowUpTooltip') {
      this.peerFollowUpTooltip = true
    } else if (hoveredObject === 'peerConversionTooltip') {
      this.peerConversionTooltip = true;
    }
  }

  hideAllTooltips() {
    this.peerComparisonTooltip = false;
    this.timelineInfoTooltip = false;
    this.fpcInfoTooltip = false;
    this.peerFollowUpTooltip = false;
    this.peerConversionTooltip = false;
  }

  getBuilderType(): string {
    let clientType = this.reportData.clientType;
    return clientType === 'REMODELING'
      ? 'remodelers'
      : clientType === 'CUSTOM_HOMES'
        ? 'custom home builders'
        : 'remodelers and custom home builders';
  }

  initializePipelineStages(): void {
    if (this.selectedPipeline) {
      this.pipelineStages = [...this.selectedPipeline.pipelineStages];
      this.selectedPipelineStages = [...this.pipelineStages];
      this.updateSelectedPipelineStages();
    }
  }

  getFontSize(index: number): string {
    const maxSize = 2;
    const decrement = 0.2;
    const minSize = 1;
    const calculatedSize = maxSize - index * decrement;
    return `${Math.max(calculatedSize, minSize)}em`;
  }

  updateSelectedPipelineStages(): void {
    if (this.selectedPipelineStages.length === 0) {
      this.selectedPipelineStages = this.selectedPipeline!.pipelineStages;
    }

    const stageCount = this.selectedPipelineStages.length;
    this.selectedPipelineStagesLabel =
      stageCount === this.pipelineStages.length
        ? 'All stages selected'
        : `${stageCount} stages selected`;

    this.populatePipelineChart();
    this.populateConversionTimeline();
  }

  private populatePipelineChart(): void {
    if (!this.selectedPipeline) {
      this.pipelineStageData = [];
      return;
    }

    const activeSalesPersons = this.selectedSalesPersons.length > 0
      ? this.selectedSalesPersons
      : this.salesPersons;

    const activePipelineStages = this.selectedPipelineStages.length > 0
      ? this.selectedPipelineStages
      : this.selectedPipeline.pipelineStages;

    const stageData: { [key: string]: { [month: string]: number } } = {};

    const getSalesPersonCount = (salesPersonConversions: any[]): number => {
      return salesPersonConversions
        .filter(sp => activeSalesPersons.some(person => person.id === sp.salesPersonId))
        .reduce((sum, sp) => sum + sp.count, 0);
    };

    this.reportDataPreviousMap.forEach(([previousReport, _]: [KpiReport, MonthlyAverage]) => {
      const previousPipeline = previousReport.pipelines?.find(
        pipeline => pipeline.pipelineName === this.selectedPipeline?.pipelineName
      );

      if (previousPipeline) {
        const month = SharedUtil.formatMonthAndYear(previousReport.monthAndYear);

        previousPipeline.pipelineStages
          .filter(stage => activePipelineStages.some(selected => selected.stageName === stage.stageName))
          .forEach(stage => {
            const totalCount = getSalesPersonCount(stage.salesPersonConversions);

            if (!stageData[stage.stageName]) {
              stageData[stage.stageName] = {};
            }

            stageData[stage.stageName][month] = (stageData[stage.stageName][month] || 0) + totalCount;
          });
      }
    });

    const currentMonth = SharedUtil.formatMonthAndYear(this.reportData.monthAndYear);
    this.selectedPipeline.pipelineStages
      .filter(stage => activePipelineStages.some(selected => selected.stageName === stage.stageName))
      .forEach(stage => {
        const totalCount = getSalesPersonCount(stage.salesPersonConversions);

        if (!stageData[stage.stageName]) {
          stageData[stage.stageName] = {};
        }

        stageData[stage.stageName][currentMonth] = (stageData[stage.stageName][currentMonth] || 0) + totalCount;
      });

    this.pipelineStageData = Object.keys(stageData).map(stageName => ({
      name: stageName,
      series: Object.keys(stageData[stageName])
        .sort((a, b) => new Date(a).getTime() - new Date(b).getTime())
        .map(month => ({
          name: month,
          value: stageData[stageName][month],
        })),
    }));
  }

  private populateFollowUpPerConversionChart(
    currentData: KpiReport,
    previousData: [KpiReport, MonthlyAverage][]
  ): void {
    const monthsToDisplay = [
      ...new Set([
        ...previousData.map(([report]) => SharedUtil.formatMonthAndYear(report.monthAndYear)),
        SharedUtil.formatMonthAndYear(currentData.monthAndYear)
      ])
    ].sort((a, b) => new Date(a).getTime() - new Date(b).getTime());

    const salespersonData = this.selectedSalesPersons.map(salesperson => ({
      name: salesperson.name,
      series: monthsToDisplay.map(month => {
        const previousEntry = previousData.find(([report]) =>
          SharedUtil.formatMonthAndYear(report.monthAndYear) === month &&
          report.followUpConversions.some(conversion =>
            conversion.ghlUserId === salesperson.id
          )
        );

        const previousValue = previousEntry
          ? previousEntry[0].followUpConversions.find(conversion =>
          conversion.ghlUserId === salesperson.id
        )?.totalFollowUpPerConversion ?? 0
          : 0;

        const currentValue =
          SharedUtil.formatMonthAndYear(currentData.monthAndYear) === month &&
          currentData.followUpConversions.some(conversion =>
            conversion.ghlUserId === salesperson.id
          )
            ? currentData.followUpConversions.find(conversion =>
            conversion.ghlUserId === salesperson.id
          )?.totalFollowUpPerConversion ?? 0
            : 0;

        return {
          name: month,
          value: parseFloat((currentValue || previousValue).toFixed(2))
        };
      })
    }));

    const peerIndustryAverage = {
      name: "Peer Industry Average",
      series: monthsToDisplay.map(month => {
        const previousAverage = previousData.find(([report, average]) =>
          SharedUtil.formatMonthAndYear(report.monthAndYear) === month &&
          average?.averageTotalFollowUpPerConversion !== null
        )?.[1]?.averageTotalFollowUpPerConversion ?? 0;

        const currentAverage =
          SharedUtil.formatMonthAndYear(currentData.monthAndYear) === month &&
          this.averageReportData?.averageTotalFollowUpPerConversion !== null
            ? this.averageReportData.averageTotalFollowUpPerConversion
            : 0;

        return {
          name: month,
          value: parseFloat((currentAverage || previousAverage).toFixed(2))
        };
      })
    };

    this.followUpPerConversionData = [...salespersonData, peerIndustryAverage];
  }

  private populateConversionTimeline(): void {
    if (!this.selectedPipeline) {
      this.conversionTimeline = [];
      return;
    }

    const activeSalesPersons = this.selectedSalesPersons.length > 0
      ? this.selectedSalesPersons
      : this.salesPersons;

    const activePipelineStages = this.selectedPipelineStages.length > 0
      ? this.selectedPipelineStages
      : this.selectedPipeline.pipelineStages;

    const timelineData: { [month: string]: number } = {};

    const getTotalConversions = (stage: PipelineStage): number => {
      return stage.salesPersonConversions
        .filter(sp => activeSalesPersons.some(person => person.id === sp.salesPersonId))
        .reduce((sum, sp) => sum + sp.count, 0);
    };

    this.reportDataPreviousMap.forEach(([previousReport]) => {
      const previousPipeline = previousReport.pipelines?.find(
        pipeline => pipeline.pipelineName === this.selectedPipeline?.pipelineName
      );

      if (previousPipeline) {
        const month = SharedUtil.formatMonthAndYear(previousReport.monthAndYear);

        previousPipeline.pipelineStages
          .filter(stage => activePipelineStages.some(selected => selected.stageName === stage.stageName))
          .forEach(stage => {
            const totalConversions = getTotalConversions(stage);
            timelineData[month] = (timelineData[month] || 0) + totalConversions;
          });
      }
    });

    const currentMonth = SharedUtil.formatMonthAndYear(this.reportData.monthAndYear);

    this.selectedPipeline.pipelineStages
      .filter(stage => activePipelineStages.some(selected => selected.stageName === stage.stageName))
      .forEach(stage => {
        const totalConversions = getTotalConversions(stage);
        timelineData[currentMonth] = (timelineData[currentMonth] || 0) + totalConversions;
      });

    this.conversionTimeline = Object.keys(timelineData)
      .sort((a, b) => new Date(a).getTime() - new Date(b).getTime())
      .map((month, index) => ({
        month,
        totalConversions: timelineData[month],
        index
      }));
  }

  initializeSalesPersons() {
    const uniqueSalesPersons = new Map<string, SalesPerson>();

    this.reportData.salesPersonConversations.forEach(conversation => {
      if (!uniqueSalesPersons.has(conversation.salesPersonId)) {
        uniqueSalesPersons.set(conversation.salesPersonId, {
          id: conversation.salesPersonId,
          name: conversation.salesPersonName,
          imageUrl: '',
          cachedImage: ''
        });
      }
    });

    this.reportDataPreviousMap.forEach(([report]) => {
      report.salesPersonConversations.forEach(conversation => {
        if (!uniqueSalesPersons.has(conversation.salesPersonId)) {
          uniqueSalesPersons.set(conversation.salesPersonId, {
            id: conversation.salesPersonId,
            name: conversation.salesPersonName,
            imageUrl: '',
            cachedImage: ''
          });
        }
      });
    });

    this.salesPersons = Array.from(uniqueSalesPersons.values());
  }

  onParameterChange(): void {
    this.setupChart();
    this.filterConversations();
    this.setupFollowupChart();
    this.populateFollowUpPerConversionChart(this.reportData, this.reportDataPreviousMap);
    this.updateFilteredFollowUpConversions();
    this.updateChartView();
    this.computeIndices();
    this.populatePipelineChart();
    this.populateConversionTimeline();
  }

  updateFilteredFollowUpConversions(): void {
    if (this.selectedSalesPersons?.length > 0) {
      const selectedIds = this.selectedSalesPersons.map(sp => sp.id);
      this.filteredFollowUpConversions = this.reportData.followUpConversions.filter(conversion =>
        selectedIds.includes(conversion.ghlUserId)
      );
    } else {
      this.filteredFollowUpConversions = this.reportData.followUpConversions;
    }
  }

  onPipelineChange(event: any): void {
    this.updateSelectedPipeline(event.value);
    this.onParameterChange();
  }

  updateChartView() {
    if (window.innerWidth >= 1400) {
      this.pieChartView = [580, 450];
      this.lineChartView = [460, 250];
      this.pipelineView = [1150, 400];
    } else if (window.innerWidth < 1400 && window.innerWidth >= 1200) {
      this.pieChartView = [480, 450];
      this.lineChartView = [400, 250];
      this.pipelineView = [900, 400];
    } else {
      this.pieChartView = [380, 450];
      this.lineChartView = [260, 250];
      this.pipelineView = [700, 400];
    }
  }

  getSalesPersonDataForStage(stage: PipelineStage): { count: number, monetaryValue: number, performanceIndex: number } {
    let totalCount = 0;
    let totalMonetaryValue = 0;

    if (this.selectedSalesPersons.length > 0) {
      const selectedSalesPersonsData = stage.salesPersonConversions
        .filter(ghlUser => this.selectedSalesPersons.find(sp => sp.id === ghlUser.salesPersonId));

      totalCount = selectedSalesPersonsData.reduce((acc, ghlUser) => acc + ghlUser.count, 0);
      totalMonetaryValue = selectedSalesPersonsData.reduce((acc, ghlUser) => acc + ghlUser.monetaryValue, 0);
    } else {
      totalCount = stage.count;
      totalMonetaryValue = stage.monetaryValue;
    }

    const previousData = this.reportDataPreviousMap;
    const previousMonth = previousData.length > 0 ? previousData[0][0] : null;
    let previousStageCount = 0;

    if (previousMonth) {
      const previousPipeline = previousMonth.pipelines.find(pipeline => pipeline.pipelineName === this.selectedPipeline?.pipelineName);
      if (previousPipeline) {
        const previousStage = previousPipeline.pipelineStages.find(prevStage => prevStage.stageName === stage.stageName);
        if (previousStage) {
          if (this.selectedSalesPersons.length > 0) {
            const previousSalesPersonsData = previousStage.salesPersonConversions
              .filter(ghlUser => this.selectedSalesPersons.find(sp => sp.id === ghlUser.salesPersonId));

            previousStageCount = previousSalesPersonsData.reduce((acc, ghlUser) => acc + ghlUser.count, 0);
          } else {
            previousStageCount = previousStage.count;
          }
        }
      }
    }

    let performanceIndex = 0;
    if (previousStageCount === 0 && totalCount > 0) {
      performanceIndex = 100;
    } else if (previousStageCount !== 0) {
      performanceIndex = ((totalCount - previousStageCount) / previousStageCount) * 100;
    }

    return {
      count: totalCount || 0,
      monetaryValue: totalMonetaryValue || 0,
      performanceIndex: performanceIndex || 0
    };
  }

  getFilteredContacts(stage: PipelineStage): any[] {
    if (!this.selectedSalesPersons || this.selectedSalesPersons.length === 0) {
      return [];
    }

    const filteredContacts: any[] = [];

    stage.salesPersonConversions.forEach((salesPerson) => {
      if (this.selectedSalesPersons.some((sp) => sp.id === salesPerson.salesPersonId)) {
        salesPerson.convertedContacts.forEach((contact) => {
          const messageCounts = this.getMessageCountsForContact(contact.contactId, salesPerson.salesPersonId);
          filteredContacts.push({
            contactId: contact.contactId,
            contactName: contact.contactName,
            contactEmail: contact.contactEmail,
            contactPhone: contact.contactPhone,
            totalSms: messageCounts.smsCount,
            totalEmails: messageCounts.emailCount,
            totalCalls: messageCounts.callCount,
            totalLiveChatMessages: messageCounts.liveChatCount,
            salesPerson: {
              salesPersonId: salesPerson.salesPersonId,
              salesPersonName: salesPerson.salesPersonName,
              photoUrl: salesPerson.photoUrl
            }
          });
        });
      }
    });

    return filteredContacts;
  }

  getMessageCountsForContact(contactId: string, salesPersonId: string): { smsCount: number; emailCount: number; callCount: number; liveChatCount: number } {
    const conversation = this.reportData.salesPersonConversations.find(
      (conv) => conv.salesPersonId === salesPersonId && conv.contactId === contactId
    );

    if (!conversation) {
      return { smsCount: 0, emailCount: 0, callCount: 0, liveChatCount: 0 };
    }

    const messageCounts = this.getMessageCounts(conversation);
    return {
      smsCount: messageCounts.smsCount,
      emailCount: messageCounts.emailCount,
      callCount: messageCounts.callCount,
      liveChatCount: messageCounts.liveChatCount
    };
  }

  getStageTotalsForSalesPerson(stage: PipelineStage, salesPersonId: string): { totalSms: number, totalEmails: number, totalCalls: number, totalLiveChatMessages: number } {
    const filteredContacts = this.getFilteredContacts(stage)
      .filter(contact => contact.salesPerson.salesPersonId === salesPersonId);

    let totalSms = 0;
    let totalEmails = 0;
    let totalCalls = 0;
    let totalLiveChatMessages = 0;

    filteredContacts.forEach(contact => {
      totalSms += contact.totalSms || 0;
      totalEmails += contact.totalEmails || 0;
      totalCalls += contact.totalCalls || 0;
      totalLiveChatMessages += contact.totalLiveChatMessages || 0;
    });

    return {
      totalSms,
      totalEmails,
      totalCalls,
      totalLiveChatMessages
    };
  }

  getTotalCounts(stages: PipelineStage[]): number {
    return stages.reduce((acc, stage) => acc + this.getSalesPersonDataForStage(stage).count, 0);
  }

  getTotalMonetaryValue(stages: PipelineStage[]): number {
    return stages.reduce((acc, stage) => acc + this.getSalesPersonDataForStage(stage).monetaryValue, 0);
  }

  private computeIndices(): void {
    const previousMonth = this.reportDataPreviousMap.length > 0 ? this.reportDataPreviousMap[0][0] : null;

    if (previousMonth) {
      const previousPipeline = previousMonth.pipelines.find(pipeline => pipeline.pipelineName === this.selectedPipeline?.pipelineName);

      if (previousPipeline) {
        const previousStages = previousPipeline.pipelineStages;
        const previousTotalConversions = this.getTotalCounts(previousStages);

        if (previousTotalConversions === 0 && this.totalConversions > 0) {
          this.stageConversionIndex = 100;
        } else if (previousTotalConversions !== 0) {
          this.stageConversionIndex = ((this.totalConversions - previousTotalConversions) / previousTotalConversions) * 100;
        }
      } else {
        this.stageConversionIndex = 0;
      }
    } else {
      this.stageConversionIndex = 0;
    }

    if (this.totalConversions === 0) {
      this.stageConversionIndex = 0;
    }

    if (previousMonth) {
      const previousConversations = previousMonth.salesPersonConversations.filter(conversation =>
        this.selectedSalesPersons.find(sp => sp.id === conversation.salesPersonId)
      );

      const previousFollowups = this.getPreviousFollowupData(previousConversations);
      const previousTotalFollowups =
        previousFollowups.sms + previousFollowups.email + previousFollowups.calls + previousFollowups.liveChat;

      if (previousTotalFollowups === 0 && this.totalFollowups > 0) {
        this.followUpIndex = 100;
      } else if (previousTotalFollowups !== 0) {
        this.followUpIndex = ((this.totalFollowups - previousTotalFollowups) / previousTotalFollowups) * 100;
      }
    } else {
      this.followUpIndex = 0;
    }

    if (this.totalFollowups === 0) {
      this.followUpIndex = 0;
    }
  }

  private getPreviousFollowupData(conversations: SalesPersonConversation[]): { sms: number; email: number; calls: number; liveChat: number } {
    const totals = { sms: 0, email: 0, calls: 0, liveChat: 0 };

    conversations.forEach(conversation => {
      const messageCounts = this.getMessageCounts(conversation);
      totals.sms += messageCounts.smsCount;
      totals.email += messageCounts.emailCount;
      totals.calls += messageCounts.callCount;
      totals.liveChat += messageCounts.liveChatCount;
    });

    return totals;
  }

  private updateSelectedPipeline(pipeline: Pipeline | null): void {
    this.selectedPipeline = pipeline;
    this.selectedPipelineNoData = !pipeline?.pipelineStages.some(stage => stage.count > 0);

    this.pipelineStages = pipeline!.pipelineStages;
    this.selectedPipelineStages = this.pipelineStages;
  }

  private setupFollowupChart(): void {
    this.followupData = [
      { name: 'SMS', value: this.totalSmsCount },
      { name: 'Email', value: this.totalEmailCount },
      { name: 'Calls', value: this.totalCallCount },
      { name: 'Live Chat Messages', value: this.totalLiveChatCount }
    ];
    this.totalFollowups = this.followupData.reduce((sum, item) => sum + item.value, 0);
  }

  private setupChart(): void {
    if (this.reportData && this.reportData.pipelines.length > 0) {
      const pipelineStages = this.selectedPipeline!.pipelineStages;
      this.data = pipelineStages.map(stage => ({
        name: stage.stageName,
        value: this.getSalesPersonDataForStage(stage).count
      }));
    }
    this.totalConversions = this.data.reduce((sum, stage) => sum + stage.value, 0);
  }

  preloadImages() {
    this.salesPersons.forEach(ghlUser => {
      const img = new Image();
      img.crossOrigin = 'Anonymous'; // Allow cross-origin access
      img.src = ghlUser.imageUrl;
      img.onload = () => {
        const canvas = document.createElement('canvas');
        canvas.width = img.width;
        canvas.height = img.height;
        const ctx = canvas.getContext('2d');
        if (ctx) {
          ctx.drawImage(img, 0, 0);
          try {
            ghlUser.cachedImage = canvas.toDataURL(); // Store as base64
          } catch (e) {
            console.error('Error converting image to base64:', e);
          }
        }
      };
    });
  }

  getUniqueSalesPersonNames(pipelines: Pipeline[]): SalesPerson[] {
    const salesPersonMap = new Map<string, SalesPerson>();

    pipelines.forEach(pipeline => {
      pipeline.pipelineStages.forEach(stage => {
        if (Array.isArray(stage.salesPersonConversions) && stage.salesPersonConversions.length > 0) {
          stage.salesPersonConversions.forEach(ghlUser => {
            if (ghlUser.salesPersonId && ghlUser.salesPersonName && !salesPersonMap.has(ghlUser.salesPersonId)) {
              salesPersonMap.set(ghlUser.salesPersonId, {
                id: ghlUser.salesPersonId,
                name: ghlUser.salesPersonName,
                imageUrl: ghlUser.photoUrl
              });
            }
          });
        }
      });
    });

    this.reportDataPreviousMap.forEach(([report]) => {
      report.pipelines.forEach(pipeline => {
        pipeline.pipelineStages.forEach(stage => {
          if (Array.isArray(stage.salesPersonConversions) && stage.salesPersonConversions.length > 0) {
            stage.salesPersonConversions.forEach(ghlUser => {
              if (ghlUser.salesPersonId && ghlUser.salesPersonName && !salesPersonMap.has(ghlUser.salesPersonId)) {
                salesPersonMap.set(ghlUser.salesPersonId, {
                  id: ghlUser.salesPersonId,
                  name: ghlUser.salesPersonName,
                  imageUrl: ghlUser.photoUrl
                });
              }
            });
          }
        });
      });
    });

    return Array.from(salesPersonMap.values());
  }

  getFpcComparisonWithAverage(conversion: any): number {
    if (!this.averageReportData || !this.averageReportData.averageTotalFollowUpPerConversion) {
      return 0;
    }

    const averageFpc = this.averageReportData.averageTotalFollowUpPerConversion;
    const currentFpc = conversion.totalFollowUpPerConversion || 0;

    if (averageFpc === 0 && currentFpc > 0) {
      return 100;
    } else if (averageFpc !== 0) {
      return ((currentFpc - averageFpc) / averageFpc) * 100;
    }

    return 0;
  }

  filterConversations() {
    if (this.selectedSalesPersons.length > 0) {
      const selectedIds = this.selectedSalesPersons.map(sp => sp.id);
      this.filteredConversations = this.reportData.salesPersonConversations.filter(
        conversation => selectedIds.includes(conversation.salesPersonId)
      );
    } else {
      this.filteredConversations = this.reportData.salesPersonConversations;
    }
    this.updateTotalCounts();
  }

  updateTotalCounts() {
    this.totalSmsCount = 0;
    this.totalEmailCount = 0;
    this.totalCallCount = 0;
    this.totalLiveChatCount = 0;

    this.filteredConversations.forEach(conversation => {
      const messageCounts = this.getMessageCounts(conversation);
      this.totalSmsCount += messageCounts.smsCount;
      this.totalEmailCount += messageCounts.emailCount;
      this.totalCallCount += messageCounts.callCount;
      this.totalLiveChatCount += messageCounts.liveChatCount;
    });
  }

  getMessageCounts(conversation: SalesPersonConversation) {
    let smsCount = 0;
    let emailCount = 0;
    let callCount = 0;
    let liveChatCount = 0;

    conversation.conversationMessages.forEach(message => {
      switch (message.messageType) {
        case 'TYPE_SMS':
          smsCount++;
          break;
        case 'TYPE_EMAIL':
          emailCount++;
          break;
        case 'TYPE_CALL':
          callCount++;
          break;
        case 'TYPE_LIVE_CHAT':
          liveChatCount++;
          break;
      }
    });

    return { smsCount, emailCount, callCount, liveChatCount };
  }

  getCachedImage(salesPersonId: string): string {
    const salesPerson = this.salesPersons.find(sp => sp.id === salesPersonId);
    return salesPerson?.cachedImage || salesPerson?.imageUrl || 'default-avatar-url';
  }

  populateEvents(conversation: SalesPersonConversation) {
    conversation.events = conversation.conversationMessages.map(message => ({
      status: this.toTitleCase(message.status || ''),
      dateAdded: message.dateAdded,
      direction: message.direction,
      messageType: this.getFriendlyMessageType(message.messageType),
      messageBody: message.messageBody || '',
      callDuration: message.callDuration,
    }));
  }

  getFriendlyMessageType(messageType: string): string {
    switch (messageType) {
      case 'TYPE_SMS':
        return 'SMS';
      case 'TYPE_EMAIL':
        return 'E-mail';
      case 'TYPE_CALL':
        return 'Call';
      case 'TYPE_LIVE_CHAT':
        return 'Live Chat';
      default:
        return 'Unknown';
    }
  }

  getMessageIcon(messageType: string): string {
    switch (messageType) {
      case 'SMS':
        return 'pi pi-mobile';
      case 'E-mail':
        return 'pi pi-envelope';
      case 'Call':
        return 'pi pi-phone';
      case 'Live Chat':
        return 'pi pi-comments';
      default:
        return 'pi pi-info-circle';
    }
  }

  getMessageColor(messageType: string): string {
    switch (messageType) {
      case 'SMS':
        return '#49D025';
      case 'E-mail':
        return '#AD25D0';
      case 'Call':
        return '#259ED0';
      case 'Live Chat':
        return '#CDCC32';
      default:
        return 'pi pi-info-circle';
    }
  }

  getDirectionIcon(direction: string): string {
    return direction === 'inbound' ? 'pi-arrow-circle-down' : 'pi-arrow-circle-up';
  }

  getFormattedMessageType(messageType: string): string {
    return messageType
      .replace(/^type_/i, '')
      .replace(/_/g, ' ')
      .replace(/\b\w/g, char => char.toUpperCase());
  }

  get sortedConversations() {
    return this.filteredConversations.sort((a, b) => a.salesPersonName.localeCompare(b.salesPersonName));
  }

  toTitleCase(text: string): string {
    if (!text) return text;
    return text.replace(/\w\S*/g, (word) =>
      word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()
    );
  }

  formatDuration(seconds: number): string {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    const hoursDisplay = hours > 0 ? `${hours}h ` : '';
    const minutesDisplay = minutes > 0 ? `${minutes}m ` : '';
    const secondsDisplay = secs > 0 ? `${secs}s` : '';

    return `${hoursDisplay}${minutesDisplay}${secondsDisplay}`.trim();
  }

  protected readonly LegendPosition = LegendPosition;
  protected readonly SharedUtil = SharedUtil;
}
