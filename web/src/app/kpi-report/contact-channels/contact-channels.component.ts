import {Component, HostListener, Input, OnInit} from '@angular/core';
import {KpiReport} from "../../models/kpi-report";
import {MonthlyAverage} from "../../models/monthly-average";
import {SharedUtil} from "../../util/shared-util";
import {dropInAnimation} from "../../util/animations";
import {LegendPosition} from "@swimlane/ngx-charts";
import {CityAnalytics} from "../../models/ga/city-analytics";

@Component({
  selector: 'app-ghlContact-channels',
  templateUrl: './ghlContact-channels.component.html',
  styleUrl: './ghlContact-channels.component.css',
  animations: [dropInAnimation]
})
export class ContactChannelsComponent implements OnInit {
  @Input() reportData!: KpiReport;
  @Input() reportDataPreviousMap: [KpiReport, MonthlyAverage][] = [];
  @Input() displayComparisons!: boolean;
  @Input() monthCount!: number;
  @Input() isVisible!: { [key: string]: string };

  data: any[] = [];
  xAxisLabel = 'Month & Year';
  yAxisLabel = 'Session Channel Count';

  protected readonly SharedUtil = SharedUtil;

  view: [number, number] = [550, 485];

  attributionSources: string[] = [];
  selectedSources: string[] = [];
  selectedSourcesLabel: string = 'All channels displayed';

  infoTooltip = false;

  ngOnInit(): void {
    this.populateChart([],this.reportData, this.reportDataPreviousMap);
    this.selectedSources = this.attributionSources;
    this.updateChartView();
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.updateChartView();
  }

  updateSelectedSources() {
    let count = this.selectedSources.length;

    this.selectedSourcesLabel = `${count} channels displayed`;

    if (this.selectedSources.length === this.attributionSources.length) {
      this.selectedSourcesLabel = 'All channels displayed';
    }

    this.populateChart(this.selectedSources, this.reportData, this.reportDataPreviousMap);
  }

  updateChartView() {
    if (window.innerWidth >= 1400) {
      this.view = [550, 485];
    } else if (window.innerWidth < 1400 && window.innerWidth >= 1200) {
      this.view = [450, 485];
    } else {
      this.view = [350, 485];
    }
  }

  showTooltip(hoveredObject: string) {
    this.hideAllTooltips();

    if (hoveredObject === 'infoTooltip') {
      this.infoTooltip = true;
    }
  }

  hideAllTooltips() {
    this.infoTooltip = false;
  }

  private populateChart(selectedAttributions: string[], currentData: KpiReport, previousData: [KpiReport, MonthlyAverage][]): void {
    const attributionData: { [key: string]: { [month: string]: number } } = {};
    const isValidAttribution = (attribution: string) => attribution && attribution !== '-' && attribution.trim() !== '';

    previousData.forEach(([previousReport, previousAverage]: [KpiReport, MonthlyAverage]) => {
      if (previousReport.websiteLead?.leadSource) {
        const previousMonth = SharedUtil.formatMonthAndYear(previousReport.monthAndYear);
        previousReport.websiteLead.leadSource.forEach(source => {
          source.leadContacts?.forEach(ghlContact => {
            if (!ghlContact.attributionSource) return;
            const firstAttribution = ghlContact.attributionSource.split(',')[0].trim();
            if (!isValidAttribution(firstAttribution)) return;

            if (!attributionData[firstAttribution]) {
              attributionData[firstAttribution] = {};
            }

            if (attributionData[firstAttribution][previousMonth]) {
              attributionData[firstAttribution][previousMonth]++;
            } else {
              attributionData[firstAttribution][previousMonth] = 1;
            }

            if (!this.attributionSources.includes(firstAttribution)) {
              this.attributionSources.push(firstAttribution);
            }
          });
        });
      }
    });

    currentData.websiteLead?.leadSource.forEach(source => {
      source.leadContacts?.forEach(ghlContact => {
        if (!ghlContact.attributionSource) return;
        const firstAttribution = ghlContact.attributionSource.split(',')[0].trim();
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

        if (!this.attributionSources.includes(firstAttribution)) {
          this.attributionSources.push(firstAttribution);
        }
      });
    });

    if (selectedAttributions.length > 0) {
      this.selectedSources = this.attributionSources.filter(source => selectedAttributions.includes(source));
      this.data = this.selectedSources.map(attributionSource => ({
        name: attributionSource,
        series: Object.keys(attributionData[attributionSource])
          .sort((a, b) => new Date(a).getTime() - new Date(b).getTime())
          .map(month => ({
            name: month,
            value: attributionData[attributionSource][month]
          }))
      }));
    } else {
      this.selectedSources = [...this.attributionSources];
      this.data = this.selectedSources.map(attributionSource => ({
        name: attributionSource,
        series: Object.keys(attributionData[attributionSource])
          .sort((a, b) => new Date(a).getTime() - new Date(b).getTime())
          .map(month => ({
            name: month,
            value: attributionData[attributionSource][month]
          }))
      }));
      this.selectedSources = [];
      this.selectedSourcesLabel = 'All channels displayed';
    }
  }
  protected readonly LegendPosition = LegendPosition;
}
