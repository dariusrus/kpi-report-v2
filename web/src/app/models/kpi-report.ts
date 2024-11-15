import {WebsiteLead} from "./ghl/website-lead";
import {Appointment} from "./ghl/appointment";
import {Pipeline} from "./ghl/pipeline";
import {ContactWon} from "./ghl/ghlContact-won";
import {MonthlyClarityReport} from "./mc/monthly-clarity-report";
import {Calendar} from "./ghl/calendar";
import {CityAnalytics} from "./ga/city-analytics";
import {SalesPersonConversion} from "./ghl/sales-person-conversion";
import {
  SalesPersonConversationsComponent
} from "../kpi-report/sales-person-conversations/sales-person-conversations.component";
import {SalesPersonConversation} from "./ghl/sales-person-conversation";

export interface KpiReport {
  subAgency: string;
  ghlLocationId: string;
  monthAndYear: string;
  country: string;
  uniqueSiteVisitors: number;
  cityAnalytics: CityAnalytics[];
  opportunityToLead: number;
  websiteLead: WebsiteLead;
  calendars: Calendar[];
  pipelines: Pipeline[];
  contactsWon: ContactWon[];
  clientType: string;
  monthlyClarityReport: MonthlyClarityReport;
  salesPersonConversations: SalesPersonConversation[];
}
