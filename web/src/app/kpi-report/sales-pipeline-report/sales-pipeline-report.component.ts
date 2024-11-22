import {Component, HostListener, Input, OnInit} from '@angular/core';
import { KpiReport } from "../../models/kpi-report";
import { MonthlyAverage } from "../../models/monthly-average";
import { Pipeline } from "../../models/ghl/pipeline";
import {PipelineStage} from "../../models/ghl/pipeline-stage";
import {LegendPosition} from "@swimlane/ngx-charts";
import {SharedUtil} from "../../util/shared-util";
import {SalesPersonConversation} from "../../models/ghl/sales-person-conversation";

interface SalesPerson {
  id: string;
  name: string;
  imageUrl: string;
  cachedImage?: string;
}

@Component({
  selector: 'app-sales-pipeline-report',
  templateUrl: './sales-pipeline-report.component.html',
  styleUrls: ['./sales-pipeline-report.component.css']
})
export class SalesPipelineReportComponent implements OnInit {
  @Input() reportData!: KpiReport;
  @Input() reportDataPreviousMap: [KpiReport, MonthlyAverage][] = [];
  @Input() isVisible!: { [key: string]: string };

  data: any[] = [];
  followupData: any[] = [];
  selectedPipeline: Pipeline | null = null;
  selectedPipelineNoData: boolean = false;
  pieChartView: [number, number] = [580, 350];
  scheme = 'picnic';

  salesPersons: SalesPerson[] = [];
  selectedSalesPersons: SalesPerson[] = [];
  totalConversions: number = 0;

  // Total counts for number cards
  totalSmsCount: number = 0;
  totalEmailCount: number = 0;
  totalCallCount: number = 0;
  totalLiveChatCount: number = 0;
  totalFollowups: number = 0;
  filteredConversations: SalesPersonConversation[] = [];

  stageConversionIndex: number = 0;
  followUpIndex: number = 0;

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
      console.log(this.salesPersons);
    }

    this.filterConversations();
    this.updateTotalCounts();

    this.setupChart();
    this.setupFollowupChart();
    this.updateChartView();
    this.computeIndices();
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.updateChartView();
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
    this.salesPersons = Array.from(uniqueSalesPersons.values());
  }

  onParameterChange(): void {
    this.setupChart();
    this.filterConversations();
    this.setupFollowupChart();
    this.updateChartView();
    this.computeIndices();
  }

  onPipelineChange(event: any): void {
    this.updateSelectedPipeline(event.value);
    this.onParameterChange();
  }

  updateChartView() {
    if (window.innerWidth >= 1400) {
      this.pieChartView = [580, 450];
    } else if (window.innerWidth < 1400 && window.innerWidth >= 1200) {
      this.pieChartView = [480, 350];
    } else {
      this.pieChartView = [380, 350];
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
    // Compute stageConversionIndex
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
        this.stageConversionIndex = 0; // Default if no previous pipeline
      }
    } else {
      this.stageConversionIndex = 0; // Default if no previous month data
    }

    // Compute followUpIndex
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
      this.followUpIndex = 0; // Default if no previous month data
    }

    console.log(this.stageConversionIndex);
    console.log(this.followUpIndex);
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

    return Array.from(salesPersonMap.values());
  }

  // followups
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
  protected readonly LegendPosition = LegendPosition;
  protected readonly SharedUtil = SharedUtil;
}
