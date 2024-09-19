import {Component, HostListener, Input, OnInit} from '@angular/core';
import {animate, query, stagger, style, transition, trigger} from "@angular/animations";
import {KpiReport} from "../../models/kpi-report";
import {Pipeline} from "../../models/ghl/pipeline";
import {PipelineStage} from "../../models/ghl/pipeline-stage";

@Component({
  selector: 'app-pipeline-stages',
  templateUrl: './pipeline-stages.component.html',
  styleUrl: './pipeline-stages.component.css',
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
  ]
})
export class PipelineStagesComponent implements OnInit {
  @Input() reportData!: KpiReport;
  @Input() isVisible!: { [key: string]: string };

  data: any[] = [];
  selectedPipeline: Pipeline | null = null;
  selectedPipelineNoData: boolean = false;
  pieChartView: [number, number] = [500, 425];
  scheme = 'picnic'

  ngOnInit() {
    if (this.reportData?.pipelines.length) {
      this.updateSelectedPipeline(this.reportData.pipelines[0]);
    }
    this.setupChart();
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.updateChartView();
  }

  onPipelineChange(event: any): void {
    this.updateSelectedPipeline(event.value);
    this.setupChart();
    this.updateChartView();
  }

  updateChartView() {
    if (window.innerWidth >= 1400) {
      this.pieChartView = [600, 425];
    } else if (window.innerWidth < 1400 && window.innerWidth >= 1200) {
      this.pieChartView = [500, 425];
    } else {
      this.pieChartView = [400, 425];
    }
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

  private updateSelectedPipeline(pipeline: Pipeline | null): void {
    this.selectedPipeline = pipeline;
    this.selectedPipelineNoData = !pipeline?.pipelineStages.some(stage => stage.count > 0);
  }

  private setupChart(): void {
    if (this.reportData && this.reportData.pipelines.length > 0) {
      const pipelineStages = this.selectedPipeline!.pipelineStages;
      this.data = pipelineStages.map(stage => ({
        name: stage.stageName,
        value: stage.count
      }));
    }
  }
}
