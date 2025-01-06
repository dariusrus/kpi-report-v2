import {Appointment} from "./appointment";

export interface AppointmentOpportunities {
  status: string;
  appointmentDate: string;
  lastStageChangeAt: string;
  pipelineName: string;
  stageName: string;
  contactName: string;
}

export interface Calendar {
  calendarId: string;
  calendarName: string;
  appointments: Appointment[];
  appointmentOpportunities?: AppointmentOpportunities[];
}
