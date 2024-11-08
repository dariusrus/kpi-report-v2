import {Component, HostListener, Input, OnInit} from '@angular/core';
import {KpiReport} from "../../models/kpi-report";
import {Calendar} from "../../models/ghl/calendar";
import {Appointment} from "../../models/ghl/appointment";
import {TotalAppointment} from "../../models/ghl/total-appointment";
import {dropInAnimation} from "../../util/animations";

@Component({
  selector: 'app-appointments',
  templateUrl: './appointments.component.html',
  styleUrl: './appointments.component.css',
  animations: [dropInAnimation]
})
export class AppointmentsComponent implements OnInit{
  @Input() reportData!: KpiReport;
  @Input() isVisible!: { [key: string]: string };

  data: any[] = [];

  filteredCalendars: Calendar[] = [];
  selectedCalendar: Calendar | null = null;
  availableAppointmentStatuses: string[] = [];
  totalAppointments: TotalAppointment[] = [];
  advancedPieChartView: [number, number] = [400, 250];

  infoTooltip = false;

  ngOnInit(): void {
    this.preprocessCalendars(this.reportData.calendars);
    this.setupChart(this.reportData);
    this.updateChartView();
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.updateChartView();
  }

  onCalendarChange(event: any): void {
    this.updateSelectedCalendar(event.value);
    this.setupChart(this.reportData!);
  }

  preprocessCalendars(calendars: Calendar[]) {
    this.selectedCalendar = null;
    if (calendars) {
      this.reportData!.calendars = this.normalizeAndSortAppointments(calendars);
      this.availableAppointmentStatuses = this.getAvailableStatuses(this.reportData!.calendars);
      this.totalAppointments = this.calculateTotalAppointments(calendars);
    }
  }

  updateChartView() {
    if (window.innerWidth >= 1400) {
      this.advancedPieChartView = [600, 250];
    } else if (window.innerWidth < 1400 && window.innerWidth >= 1200) {
      this.advancedPieChartView = [500, 250];
    } else {
      this.advancedPieChartView = [400, 250];
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

  normalizeAndSortAppointments(calendars: Calendar[]): Calendar[] {
    const allStatuses: Set<string> = new Set();
    calendars.forEach(calendar => {
      calendar.appointments.forEach(appointment => {
        allStatuses.add(appointment.status);
      });
    });
    return calendars.map(calendar => {
      const updatedAppointments: Appointment[] = Array.from(allStatuses).map(status => {
        const existingAppointment = calendar.appointments.find(app => app.status === status);
        return existingAppointment ? existingAppointment : { status, count: 0 };
      });
      updatedAppointments.sort((a, b) => a.status.localeCompare(b.status));
      return {
        ...calendar,
        appointments: updatedAppointments
      };
    });
  }

  groupCalendars() {
    const withAppointments: Calendar[] = [];
    const withoutAppointments: Calendar[] = [];

    this.reportData!.calendars.forEach(calendar => {
      const totalAppointments = calendar.appointments.reduce((sum, appointment) => sum + appointment.count, 0);
      if (totalAppointments > 0) {
        withAppointments.push(calendar);
      } else {
        withoutAppointments.push(calendar);
      }
    });
    this.filteredCalendars = withAppointments;
  }

  getAvailableStatuses(calendars: Calendar[]): string[] {
    const statusesSet = new Set<string>();

    calendars.forEach(calendar => {
      calendar.appointments.forEach(appointment => {
        statusesSet.add(appointment.status);
      });
    });

    return Array.from(statusesSet).sort();
  }

  calculateTotalAppointments(calendars: Calendar[]): TotalAppointment[] {
    const statusCountMap: { [status: string]: number } = {};

    calendars.forEach(calendar => {
      calendar.appointments.forEach(appointment => {
        const status = appointment.status;
        const count = appointment.count;

        if (statusCountMap[status]) {
          statusCountMap[status] += count;
        } else {
          statusCountMap[status] = count;
        }
      });
    });

    return Object.keys(statusCountMap).map(status => ({
      status,
      totalCount: statusCountMap[status]
    }));
  }

  setupChart(currentData: KpiReport) {
    currentData.calendars = currentData.calendars.filter(calendar => calendar.calendarId !== 'all-calendars');
    const totalAppointments = this.calculateTotalAppointments(currentData.calendars);
    const allCalendars: Calendar = {
      calendarId: 'all-calendars',
      calendarName: 'All Calendars',
      appointments: totalAppointments.map(totalAppointment => ({
        status: totalAppointment.status,
        count: totalAppointment.totalCount
      }))
    };

    currentData.calendars.unshift(allCalendars);

    if (!this.selectedCalendar || !currentData.calendars.find(calendar => calendar.calendarId === this.selectedCalendar!.calendarId)) {
      this.selectedCalendar = allCalendars;
    }

    this.data = this.selectedCalendar.appointments.map(appointment => ({
      name: this.formatStatus(appointment.status),
      value: appointment.count
    })).sort((a, b) => a.name.localeCompare(b.name));

    this.groupCalendars();
  }

  private formatStatus(status: string): string {
    if (status.toLowerCase() === 'noshow') {
      return 'No Show';
    }
    return status.charAt(0).toUpperCase() + status.slice(1).toLowerCase();
  }

  private updateSelectedCalendar(calendar: Calendar | null): void {
    this.selectedCalendar = calendar;
  }
}
