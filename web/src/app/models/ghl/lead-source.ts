import {LeadContact} from "./lead-ghlContact";

export interface LeadSource {
  source: string;
  totalLeads: number;
  totalValues: number;
  open: number;
  won: number;
  lost: number;
  abandoned: number;
  winPercentage: number;
  leadType: string;
  leadContacts: LeadContact[];
}
