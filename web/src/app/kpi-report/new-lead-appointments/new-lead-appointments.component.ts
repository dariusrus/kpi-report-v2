import { Component, Input, OnInit } from '@angular/core';
import { KpiReport } from "../../models/kpi-report";
import { SharedUtil } from "../../util/shared-util";
import {dropInAnimation} from "../../util/animations";

@Component({
  selector: 'app-new-lead-appointments',
  templateUrl: './new-lead-appointments.component.html',
  styleUrl: './new-lead-appointments.component.css',
  animations: [dropInAnimation]
})
export class NewLeadAppointmentsComponent implements OnInit {
  @Input() reportData!: KpiReport;
  @Input() reportDataPreviousMap: [KpiReport, any][] = [];

  view: [number, number] = [600, 35];

  gradient: boolean = false;

  customColors = [
    { name: 'with appointment', value: '#5db255' },
    { name: 'w/out appointments', value: '#a84d6b' }
  ];

  multi: any[] = [];

  withAppointments: number = 0;
  withoutAppointments: number = 0;

  withAppointmentsPercentage: number = 0;
  withoutAppointmentsPercentage: number = 0;

  previousMonthIndex: number | null = null;

  fromLastMonthTooltip = false;

  ngOnInit(): void {
    const contactScheduledAppointments = this.reportData.contactScheduledAppointments || [];
    const totalAppointments = contactScheduledAppointments.length;

    this.withAppointments = contactScheduledAppointments.filter(contact => contact.scheduledACall).length;
    this.withoutAppointments = contactScheduledAppointments.filter(contact => !contact.scheduledACall).length;

    this.withAppointmentsPercentage = totalAppointments > 0
      ? parseFloat(((this.withAppointments / totalAppointments) * 100).toFixed(2))
      : 0;
    this.withoutAppointmentsPercentage = totalAppointments > 0
      ? parseFloat(((this.withoutAppointments / totalAppointments) * 100).toFixed(2))
      : 0;

    if (this.reportDataPreviousMap.length > 0) {
      const previousReportData = this.reportDataPreviousMap[0][0];

      if (previousReportData) {
        const previousAppointments = previousReportData.contactScheduledAppointments || [];
        const previousTotalAppointments = previousAppointments.length;

        const previousWithAppointments = previousAppointments.filter(contact => contact.scheduledACall).length;
        const previousWithAppointmentsPercentage = previousTotalAppointments > 0
          ? parseFloat(((previousWithAppointments / previousTotalAppointments) * 100).toFixed(2))
          : 0;

        if (previousWithAppointmentsPercentage > 0) {
          this.previousMonthIndex = parseFloat((
            ((this.withAppointmentsPercentage - previousWithAppointmentsPercentage) /
              previousWithAppointmentsPercentage) * 100
          ).toFixed(2));
        } else {
          this.previousMonthIndex = this.withAppointmentsPercentage > 0 ? 100 : 0;
        }
      }
    } else {
      this.previousMonthIndex = null;
    }

    this.multi = [
      {
        "name": "New Leads",
        "series": [
          {
            "name": "with appointment",
            "value": this.withAppointments
          },
          {
            "name": "w/out appointments",
            "value": this.withoutAppointments
          }
        ]
      }
    ];
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

  protected readonly SharedUtil = SharedUtil;
}
