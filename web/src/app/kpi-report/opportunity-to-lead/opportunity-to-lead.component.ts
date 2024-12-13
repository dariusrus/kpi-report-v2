import {Component, HostListener, Input, OnInit} from '@angular/core';
import {LegendPosition} from "@swimlane/ngx-charts";
import {KpiReport} from "../../models/kpi-report";
import {MonthlyAverage} from "../../models/monthly-average";
import {SharedUtil} from "../../util/shared-util";
import * as shape from "d3-shape";
import {animate, state, style, transition, trigger} from "@angular/animations";
import {dropInAnimation} from "../../util/animations";

declare var MathJax: any;

@Component({
  selector: 'app-opportunity-to-lead',
  templateUrl: './opportunity-to-lead.component.html',
  styleUrl: './opportunity-to-lead.component.css',
  animations: [dropInAnimation]
})
export class OpportunityToLeadComponent implements OnInit {
  @Input() reportData!: KpiReport;
  @Input() averageReportData!: MonthlyAverage;
  @Input() averageReportDataPrevious!: MonthlyAverage[];
  @Input() reportDataPreviousMap: [KpiReport, MonthlyAverage][] = [];
  @Input() displayComparisons!: boolean;
  @Input() monthCount!: number;
  @Input() isVisible!: { [key: string]: string };

  dialogVisible: boolean = false
  scheme = 'forest';
  schemeAverage = 'forest';
  data: any[] = [];
  dataAverage: any[] = [];
  xAxisLabel = 'Month & Year';
  yAxisLabel = 'Opportunity-to-Lead (%)';
  curve: any = shape.curveCatmullRom.alpha(1);
  protected readonly LegendPosition = LegendPosition;
  protected readonly SharedUtil = SharedUtil;

  builders = [
    { name: 'Builder A', visitors: 1200, leads: 30 },
    { name: 'Builder B', visitors: 2300, leads: 30 },
    { name: 'Builder C', visitors: 2500, leads: 50 },
    { name: 'Builder D', visitors: 750, leads: 20 },
  ];
  totalVisitors: number = 0;
  totalLeadsAverage: number = 0;
  nonWeightedO2L: number = 0;
  weightedO2L: number = 0;

  nonWeightedFormula: string = '';
  weightedFormula: string = '';
  nonWeightedComputation: string = '';
  weightedComputation: string = '';

  previousMonthIndex: number | null = null;
  peerComparisonIndexWeighted: number | null = null;
  peerComparisonIndexNonWeighted: number | null = null;

  fromLastMonthTooltip = false;
  peerComparisonTooltip = false;
  view: [number, number] = [580, 400];

  ngOnInit(): void {
    this.populateChart(this.reportData, this.reportDataPreviousMap);
    this.populateGroupedChart(this.reportData, this.reportDataPreviousMap, this.averageReportData);
    this.calculateO2L();
    this.computeIndexes(this.reportData, this.reportDataPreviousMap, this.averageReportData);
    this.updateChartView();
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.updateChartView();
  }

  updateChartView() {
    if (window.innerWidth >= 1400) {
      this.view = [580, 400];
    } else if (window.innerWidth < 1400 && window.innerWidth >= 1200) {
      this.view = [480, 400];
    } else {
      this.view = [380, 400];
    }
  }

  showDialog() {
    this.dialogVisible = true;
  }

  renderMathJax() {
    if (typeof MathJax !== 'undefined') {
      MathJax.typesetPromise()
        .then(() => {
        })
        .catch((err: any) => console.log(err.message));
    }
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

  calculateO2L() {
    let totalO2L = 0;
    let weightedO2L = 0;
    this.totalVisitors = this.builders.reduce((sum, builder) => sum + builder.visitors, 0);
    this.totalLeadsAverage = this.builders.reduce((sum, builder) => sum + builder.leads, 0);

    this.builders.forEach(builder => {
      if (builder.visitors > 0) {
        const o2l = (builder.leads / builder.visitors) * 100;
        totalO2L += o2l;
        weightedO2L += o2l * (builder.visitors / this.totalVisitors);
      }
    });

    this.nonWeightedO2L = totalO2L / this.builders.length;
    this.weightedO2L = weightedO2L;

    this.nonWeightedFormula = `
      \\[
      \\scriptsize\\text{Non-weighted O2L:} \\quad \\frac{\\scriptsize\\text{Sum of opportunity-to-lead for each builder}}{\\scriptsize\\text{Total number of builders}} \\\\
      \\]
    `;

    this.weightedFormula = `
      \\[
      \\scriptsize\\text{Weighted O2L:} \\quad \\frac{\\scriptsize\\text{Sum of (opportunity-to-lead for each builder } \\times \\scriptsize\\text{ unique site visitors)}}{\\scriptsize\\text{Total unique site visitors}} \\\\
      \\]
    `;

    this.nonWeightedComputation = `
      \\[
      \\scriptsize\\text{Non-weighted O2L} = \\frac{${this.builders.map(builder => (builder.leads / builder.visitors * 100).toFixed(2)).join(' + ')}}{${this.builders.length}} = \\normalsize${this.nonWeightedO2L.toFixed(2)}\\%
      \\]
    `;

    this.weightedComputation = `
      \\[
      \\scriptsize\\text{Weighted O2L} = \\frac{\\text${this.builders.map(builder => `(${(builder.leads / builder.visitors * 100).toFixed(2)} \\times ${builder.visitors})`).join(' + ')}}{${this.totalVisitors}} = \\normalsize${this.weightedO2L.toFixed(2)}\\%
      \\]
    `;

    setTimeout(() => this.renderMathJax(), 0);
  }

  private computeIndexes(currentData: KpiReport, previousData: any[], currentAverage: MonthlyAverage) {
    const previousMonth = previousData.length > 0 ? previousData[0][0] : null;

    if (previousMonth && previousMonth.opportunityToLead !== null && currentData.opportunityToLead !== null) {
      const currentOpportunityToLead = currentData.opportunityToLead;
      const previousOpportunityToLead = previousMonth.opportunityToLead;

      if (previousOpportunityToLead === 0 && currentOpportunityToLead > 0) {
        this.previousMonthIndex = 100;
      } else if (previousOpportunityToLead > 0 && currentOpportunityToLead === 0) {
        this.previousMonthIndex = -100;
      } else if (previousOpportunityToLead === 0 && currentOpportunityToLead === 0) {
        this.previousMonthIndex = 0;
      } else {
        this.previousMonthIndex = ((currentOpportunityToLead - previousOpportunityToLead) / previousOpportunityToLead) * 100;
      }
    }

    if (currentAverage && currentAverage.weightedAverageOpportunityToLead !== null && currentData.opportunityToLead !== null) {
      const currentOpportunityToLead = currentData.opportunityToLead;
      const weightedAverageOpportunityToLead = currentAverage.weightedAverageOpportunityToLead;

      if (weightedAverageOpportunityToLead === 0 && currentOpportunityToLead > 0) {
        this.peerComparisonIndexWeighted = 100;
      } else if (weightedAverageOpportunityToLead > 0 && currentOpportunityToLead === 0) {
        this.peerComparisonIndexWeighted = -100;
      } else if (weightedAverageOpportunityToLead === 0 && currentOpportunityToLead === 0) {
        this.peerComparisonIndexWeighted = 0;
      } else {
        this.peerComparisonIndexWeighted = ((currentOpportunityToLead - weightedAverageOpportunityToLead) / weightedAverageOpportunityToLead) * 100;
      }
    }

    if (currentAverage && currentAverage.averageOpportunityToLead !== null && currentData.opportunityToLead !== null) {
      const currentOpportunityToLead = currentData.opportunityToLead;
      const averageOpportunityToLead = currentAverage.averageOpportunityToLead;

      if (averageOpportunityToLead === 0 && currentOpportunityToLead > 0) {
        this.peerComparisonIndexNonWeighted = 100;
      } else if (averageOpportunityToLead > 0 && currentOpportunityToLead === 0) {
        this.peerComparisonIndexNonWeighted = -100;
      } else if (averageOpportunityToLead === 0 && currentOpportunityToLead === 0) {
        this.peerComparisonIndexNonWeighted = 0;
      } else {
        this.peerComparisonIndexNonWeighted = ((currentOpportunityToLead - averageOpportunityToLead) / averageOpportunityToLead) * 100;
      }
    }
  }

  private populateChart(currentData: KpiReport, previousData: any[]): void {
    this.data = [
      {
        name: "Opportunity-to-Lead (O2L)",
        series: [
          ...previousData
            .filter(([report]) =>
              report?.monthAndYear &&
              report.opportunityToLead !== null
            )
            .map(([report]) => ({
              name: SharedUtil.formatMonthAndYear(report.monthAndYear),
              value: report.opportunityToLead
            })).reverse(),
          currentData?.monthAndYear &&
          currentData.opportunityToLead !== null ? {
            name: SharedUtil.formatMonthAndYear(currentData.monthAndYear),
            value: currentData.opportunityToLead
          } : null
        ].filter(Boolean)
      }
    ];
  }

  private populateGroupedChart(currentData: KpiReport, previousData: any[], currentAverage: MonthlyAverage): void {
    this.dataAverage = [
      {
        name: "Opportunity-to-Lead (O2L)",
        series: [
          ...previousData
            .filter(([report]) =>
              report?.monthAndYear &&
              report.opportunityToLead !== null
            )
            .map(([report]) => ({
              name: SharedUtil.formatMonthAndYear(report.monthAndYear),
              value: report.opportunityToLead
            })).reverse(),
          currentData?.monthAndYear &&
          currentData.opportunityToLead !== null ? {
            name: SharedUtil.formatMonthAndYear(currentData.monthAndYear),
            value: currentData.opportunityToLead
          } : null
        ].filter(Boolean)
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
              name: SharedUtil.formatMonthAndYear(average.monthAndYear),
              value: average.weightedAverageOpportunityToLead
            })).reverse(),
          currentAverage?.monthAndYear &&
          currentAverage.weightedAverageOpportunityToLead !== null ? {
            name: SharedUtil.formatMonthAndYear(currentAverage.monthAndYear),
            value: currentAverage.weightedAverageOpportunityToLead
          } : null
        ].filter(Boolean)
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
              name: SharedUtil.formatMonthAndYear(average.monthAndYear),
              value: average.averageOpportunityToLead
            })).reverse(),
          currentAverage?.monthAndYear &&
          currentAverage.averageOpportunityToLead !== null ? {
            name: SharedUtil.formatMonthAndYear(currentAverage.monthAndYear),
            value: currentAverage.averageOpportunityToLead
          } : null
        ].filter(Boolean)
      }
    ];
  }
}
