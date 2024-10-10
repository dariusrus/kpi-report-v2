import { Component, HostListener, Input, OnInit } from '@angular/core';
import { animate, query, stagger, style, transition, trigger } from "@angular/animations";
import { KpiReport } from "../../models/kpi-report";
import { Pipeline } from "../../models/ghl/pipeline";
import { PipelineStage } from "../../models/ghl/pipeline-stage";
import {MonthlyAverage} from "../../models/monthly-average";
import {SharedUtil} from "../../util/shared-util";

interface SalesPerson {
  id: string;
  name: string;
}

@Component({
  selector: 'app-pipeline-stages',
  templateUrl: './pipeline-stages.component.html',
  styleUrl: './pipeline-stages.component.css',
  animations: [
    trigger('fadeIn', [
      transition('* => *', [
        query(':enter', [
          style({ opacity: 0 }),
          stagger(100, [
            animate('0.5s', style({ opacity: 1 }))
          ])
        ], { optional: true })
      ])
    ]),
  ]
})
export class PipelineStagesComponent implements OnInit {
  @Input() reportData!: KpiReport;
  @Input() reportDataPreviousMap: [KpiReport, MonthlyAverage][] = [];
  @Input() isVisible!: { [key: string]: string };

  data: any[] = [];
  selectedPipeline: Pipeline | null = null;
  selectedPipelineNoData: boolean = false;
  pieChartView: [number, number] = [400, 425];
  scheme = 'picnic';

  salesPersons!: SalesPerson[];
  selectedSalesPersons!: SalesPerson[];

  ngOnInit() {
    if (this.reportData?.pipelines.length) {
      this.updateSelectedPipeline(this.reportData.pipelines[0]);
      this.salesPersons = this.getUniqueSalesPersonNames(this.reportData.pipelines);
      this.selectedSalesPersons = this.salesPersons;
    }
    this.setupChart();
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.updateChartView();
  }

  getUniqueSalesPersonNames(pipelines: Pipeline[]): SalesPerson[] {
    const salesPersonMap = new Map<string, SalesPerson>();

    pipelines.forEach(pipeline => {
      pipeline.pipelineStages.forEach(stage => {
        if (Array.isArray(stage.salesPersonConversions) && stage.salesPersonConversions.length > 0) {
          stage.salesPersonConversions.forEach(salesPerson => {
            if (salesPerson.salesPersonId && salesPerson.salesPersonName && !salesPersonMap.has(salesPerson.salesPersonId)) {
              salesPersonMap.set(salesPerson.salesPersonId, {
                id: salesPerson.salesPersonId,
                name: salesPerson.salesPersonName
              });
            }
          });
        }
      });
    });

    return Array.from(salesPersonMap.values());
  }

  onPipelineChange(event: any): void {
    this.updateSelectedPipeline(event.value);
    this.onParameterChange();
  }

  onParameterChange(): void {
    this.setupChart();
    this.updateChartView();
  }

  updateChartView() {
    if (window.innerWidth >= 1400) {
      this.pieChartView = [400, 425];
    } else if (window.innerWidth < 1400 && window.innerWidth >= 1200) {
      this.pieChartView = [400, 425];
    } else {
      this.pieChartView = [300, 425];
    }
  }

  getSalesPersonDataForStage(stage: PipelineStage): { count: number, monetaryValue: number, performanceIndex: number } {
    let totalCount = 0;
    let totalMonetaryValue = 0;

    if (this.selectedSalesPersons.length > 0) {
      const selectedSalesPersonsData = stage.salesPersonConversions
        .filter(salesPerson => this.selectedSalesPersons.find(sp => sp.id === salesPerson.salesPersonId));

      totalCount = selectedSalesPersonsData.reduce((acc, salesPerson) => acc + salesPerson.count, 0);
      totalMonetaryValue = selectedSalesPersonsData.reduce((acc, salesPerson) => acc + salesPerson.monetaryValue, 0);
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
              .filter(salesPerson => this.selectedSalesPersons.find(sp => sp.id === salesPerson.salesPersonId));

            previousStageCount = previousSalesPersonsData.reduce((acc, salesPerson) => acc + salesPerson.count, 0);
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
  }

  protected readonly SharedUtil = SharedUtil;
}
