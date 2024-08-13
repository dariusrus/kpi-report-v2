import {WebsiteLead} from "./website-lead";
import {Appointment} from "./appointment";
import {Pipeline} from "./pipeline";
import {ContactWon} from "./contact-won";

export interface KpiReport {
  subAgency: string;
  ghlLocationId: string;
  monthAndYear: string;
  uniqueSiteVisitors: number;
  opportunityToLead: number;
  websiteLead: WebsiteLead;
  appointments: Appointment[];
  pipelines: Pipeline[];
  contactsWon: ContactWon[];
}
