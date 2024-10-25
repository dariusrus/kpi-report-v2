import {Component, HostListener, Input, OnInit} from '@angular/core';
import {KpiReport} from "../../models/kpi-report";
import {MonthlyAverage} from "../../models/monthly-average";
import {SharedUtil} from "../../util/shared-util";
import {dropInAnimation} from "../../util/animations";
import {LegendPosition} from "@swimlane/ngx-charts";

@Component({
  selector: 'app-attribution-distribution',
  templateUrl: './attribution-distribution.component.html',
  styleUrl: './attribution-distribution.component.css',
  animations: [dropInAnimation]
})
export class AttributionDistributionComponent implements OnInit {
  @Input() reportData!: KpiReport;
  @Input() reportDataPreviousMap: [KpiReport, MonthlyAverage][] = [];
  @Input() displayComparisons!: boolean;
  @Input() monthCount!: number;
  @Input() isVisible!: { [key: string]: string };

  data: any[] = [];
  xAxisLabel = 'Month & Year';
  yAxisLabel = 'Session Source Count';

  protected readonly SharedUtil = SharedUtil;

  previousMonthIndex: number | null = null;
  fromLastMonthTooltip = false;
  view: [number, number] = [550, 450];

  ngOnInit(): void {
    this.populateChart(this.reportData, this.reportDataPreviousMap);
    this.updateChartView();
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.updateChartView();
  }

  updateChartView() {
    if (window.innerWidth >= 1400) {
      this.view = [550, 450];
    } else if (window.innerWidth < 1400 && window.innerWidth >= 1200) {
      this.view = [450, 450];
    } else {
      this.view = [350, 450];
    }
  }

  showTooltip(hoveredObject: string) {
    this.hideAllTooltips();

    if (hoveredObject === 'fromLastMonth') {
      this.fromLastMonthTooltip = true;
    }
  }

  hideAllTooltips() {
    this.fromLastMonthTooltip = false;
  }

  private populateChart(currentData: KpiReport, previousData: [KpiReport, MonthlyAverage][]): void {
    const attributionData: { [key: string]: { [month: string]: number } } = {};
    const isValidAttribution = (attribution: string) => attribution && attribution !== '-' && attribution.trim() !== '';
    previousData.forEach(([previousReport, previousAverage]: [KpiReport, MonthlyAverage]) => {
      if (previousReport.websiteLead && previousReport.websiteLead.leadSource) {
        const previousMonth = SharedUtil.formatMonthAndYear(previousReport.monthAndYear);
        previousReport.websiteLead.leadSource.forEach(source => {
          source.leadContacts.forEach(contact => {
            const firstAttribution = contact.attributionSource.split(',')[0].trim();
            if (!isValidAttribution(firstAttribution)) return;
            if (!attributionData[firstAttribution]) {
              attributionData[firstAttribution] = {};
            }
            if (attributionData[firstAttribution][previousMonth]) {
              attributionData[firstAttribution][previousMonth]++;
            } else {
              attributionData[firstAttribution][previousMonth] = 1;
            }
          });
        });
      }
    });

    currentData.websiteLead.leadSource.forEach(source => {
      source.leadContacts.forEach(contact => {
        const firstAttribution = contact.attributionSource.split(',')[0].trim();
        const currentMonth = SharedUtil.formatMonthAndYear(currentData.monthAndYear);
        if (!isValidAttribution(firstAttribution)) return;
        if (!attributionData[firstAttribution]) {
          attributionData[firstAttribution] = {};
        }
        if (attributionData[firstAttribution][currentMonth]) {
          attributionData[firstAttribution][currentMonth]++;
        } else {
          attributionData[firstAttribution][currentMonth] = 1;
        }
      });
    });

    this.data = Object.keys(attributionData).map(attributionSource => ({
      name: attributionSource,
      series: Object.keys(attributionData[attributionSource])
        .sort((a, b) => new Date(a).getTime() - new Date(b).getTime())
        .map(month => ({
          name: month,
          value: attributionData[attributionSource][month]
        }))
    }));
  }
  protected readonly LegendPosition = LegendPosition;
}
