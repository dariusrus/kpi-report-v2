import {WebsiteLead} from "./ghl/website-lead";
import {Appointment} from "./ghl/appointment";
import {Pipeline} from "./ghl/pipeline";
import {ContactWon} from "./ghl/contact-won";
import {MonthlyClarityReport} from "./mc/monthly-clarity-report";
import {Calendar} from "./ghl/calendar";
import {CityAnalytics} from "./ga/city-analytics";

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
}
