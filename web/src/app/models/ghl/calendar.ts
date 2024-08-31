import {Appointment} from "./appointment";

export interface Calendar {
  calendarId: string;
  calendarName: string;
  appointments: Appointment[];
}
