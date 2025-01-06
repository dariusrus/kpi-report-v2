import { Component, Input, OnInit } from '@angular/core';
import { KpiReport } from "../../models/kpi-report";
import { MonthlyAverage } from "../../models/monthly-average";
import { AppointmentOpportunities, Calendar } from "../../models/ghl/calendar";
import { SharedUtil } from "../../util/shared-util";
import {LegendPosition} from "@swimlane/ngx-charts";
import {dropInAnimation} from "../../util/animations";

@Component({
  selector: 'app-appointment-conversion',
  templateUrl: './appointment-conversion.component.html',
  styleUrls: ['./appointment-conversion.component.css'],
  animations: [dropInAnimation]
})
export class AppointmentConversionComponent implements OnInit {
  @Input() reportData!: KpiReport;
  @Input() reportDataPreviousMap: [KpiReport, MonthlyAverage][] = [];
  @Input() averageReportData!: MonthlyAverage;
  @Input() isVisible!: { [key: string]: string };
  @Input() monthCount!: number;

  filteredCalendars: Calendar[] = [];
  selectedCalendar: Calendar | null = null;
  appointmentOpportunities: AppointmentOpportunities[] = [];
  infoTooltip = false;

  // Gauge chart properties
  gaugeData: { name: string; value: number }[] = [];
  gaugeView: [number, number] = [400, 400];

  ngOnInit(): void {
    this.filterCalendarsWithOpportunities();
    this.addAllCalendarsOption();
    if (this.filteredCalendars.length > 0) {
      this.selectedCalendar = this.filteredCalendars[0];
      this.updateAppointmentOpportunities(this.selectedCalendar);
    }
    this.generateGaugeData(); // Generate data for the gauge chart
  }

  filterCalendarsWithOpportunities(): void {
    this.filteredCalendars = this.reportData.calendars.filter(
      calendar => calendar.appointmentOpportunities && calendar.appointmentOpportunities.length > 0
    );
  }

  addAllCalendarsOption(): void {
    const allOpportunities = this.reportData.calendars
      .flatMap(calendar => calendar.appointmentOpportunities || []);

    const uniqueContactNames = new Set<string>();
    const deduplicatedOpportunities = allOpportunities.filter(opportunity => {
      const isDuplicate = uniqueContactNames.has(opportunity.contactName);
      uniqueContactNames.add(opportunity.contactName);
      return !isDuplicate;
    });

    const allCalendarsOption: Calendar = {
      calendarId: 'all-calendars',
      calendarName: 'All Calendars',
      appointments: [],
      appointmentOpportunities: deduplicatedOpportunities
    };

    this.filteredCalendars.unshift(allCalendarsOption);
  }

  onCalendarChange(event: any): void {
    this.updateAppointmentOpportunities(event.value);
  }

  updateAppointmentOpportunities(selectedCalendar: Calendar | null): void {
    if (selectedCalendar) {
      const allOpportunities = selectedCalendar.appointmentOpportunities || [];
      const uniqueOpportunities = new Set<string>();
      this.appointmentOpportunities = allOpportunities.filter(opportunity => {
        const isDuplicate = uniqueOpportunities.has(opportunity.contactName);
        uniqueOpportunities.add(opportunity.contactName);
        return !isDuplicate;
      });
    }
  }

  generateGaugeData(): void {
    this.gaugeData = this.filteredCalendars
      .filter(calendar => calendar.calendarId !== 'all-calendars')
      .map(calendar => {
        const opportunities = calendar.appointmentOpportunities || [];
        const advancedCount = opportunities.filter(
          opportunity =>
            new Date(opportunity.lastStageChangeAt) > new Date(opportunity.appointmentDate)
        ).length;
        const conversionRate = opportunities.length > 0 ? (advancedCount / opportunities.length) * 100 : 0;

        return {
          name: calendar.calendarName,
          value: Math.round(conversionRate)
        };
      })
      .sort((a, b) => b.value - a.value);
  }

  getTimelineEvents(opportunity: AppointmentOpportunities): { type: string; time: string; contactName?: string; pipelineName?: string; stageName?: string }[] {
    const events = [
      {
        type: 'appointment',
        time: opportunity.appointmentDate
      },
      {
        type: 'stage-change',
        time: opportunity.lastStageChangeAt,
        contactName: opportunity.contactName,
        pipelineName: opportunity.pipelineName,
        stageName: opportunity.stageName
      }
    ];

    return events.sort((a, b) => new Date(a.time).getTime() - new Date(b.time).getTime());
  }

  showTooltip(hoveredObject: string): void {
    this.hideAllTooltips();

    if (hoveredObject === 'infoTooltip') {
      this.infoTooltip = true;
    }
  }

  hideAllTooltips(): void {
    this.infoTooltip = false;
  }

  protected readonly SharedUtil = SharedUtil;
  protected readonly LegendPosition = LegendPosition;
}
