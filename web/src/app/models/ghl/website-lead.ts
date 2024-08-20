import {LeadSource} from "./lead-source";

export interface WebsiteLead {
  totalLeads: number;
  totalValues: number;
  totalOpen: number;
  totalWon: number;
  totalLost: number;
  totalAbandoned: number;
  leadSource: LeadSource[];
}
