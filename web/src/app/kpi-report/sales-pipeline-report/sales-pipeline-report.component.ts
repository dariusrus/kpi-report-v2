import {Component, HostListener, Input, OnInit} from '@angular/core';
import { KpiReport } from "../../models/kpi-report";
import { MonthlyAverage } from "../../models/monthly-average";
import { Pipeline } from "../../models/ghl/pipeline";
import {PipelineStage} from "../../models/ghl/pipeline-stage";
import {LegendPosition} from "@swimlane/ngx-charts";
import {SharedUtil} from "../../util/shared-util";

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
  selectedPipeline: Pipeline | null = null;
  selectedPipelineNoData: boolean = false;
  pieChartView: [number, number] = [580, 350];
  scheme = 'picnic';

  salesPersons: SalesPerson[] = [];
  selectedSalesPersons: SalesPerson[] = [];
  totalConversions: number = 0;

  ngOnInit() {
    if (this.reportData?.pipelines?.length) {
      this.updateSelectedPipeline(this.reportData.pipelines[0]);
      this.salesPersons = this.getUniqueSalesPersonNames(this.reportData.pipelines);
      this.selectedSalesPersons = this.salesPersons;
      this.preloadImages();
      console.log(this.salesPersons);
    }
    this.setupChart();
    this.updateChartView();
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.updateChartView();
  }

  onParameterChange(): void {
    this.setupChart();
    this.updateChartView();
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

  getTotalCounts(stages: PipelineStage[]): number {
    return stages.reduce((acc, stage) => acc + this.getSalesPersonDataForStage(stage).count, 0);
  }

  getTotalMonetaryValue(stages: PipelineStage[]): number {
    return stages.reduce((acc, stage) => acc + this.getSalesPersonDataForStage(stage).monetaryValue, 0);
  }



  private updateSelectedPipeline(pipeline: Pipeline | null): void {
    this.selectedPipeline = pipeline;
    this.selectedPipelineNoData = !pipeline?.pipelineStages.some(stage => stage.count > 0);
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

  protected readonly LegendPosition = LegendPosition;
  protected readonly SharedUtil = SharedUtil;
}
