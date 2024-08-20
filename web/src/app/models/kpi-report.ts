import {WebsiteLead} from "./ghl/website-lead";
import {Appointment} from "./ghl/appointment";
import {Pipeline} from "./ghl/pipeline";
import {ContactWon} from "./ghl/contact-won";
import {MonthlyClarityReport} from "./mc/monthly-clarity-report";

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
  clientType: string;
  monthlyClarityReport: MonthlyClarityReport;
}
