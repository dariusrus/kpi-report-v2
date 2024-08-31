import { LeadSource } from './lead-source';

export interface WebsiteLead {
  totalLeads: number;
  totalWebsiteLeads: number;
  totalManualLeads: number;
  totalValues: number;
  totalWebsiteValuation: number;
  totalManualValuation: number;
  totalOpen: number;
  totalWon: number;
  totalLost: number;
  totalAbandoned: number;
  leadSource: LeadSource[];
}
