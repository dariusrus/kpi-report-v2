import {Component, Input, OnInit} from '@angular/core';
import {KpiReport} from "../../models/kpi-report";
import {SharedUtil} from "../../util/shared-util";
import {MonthlyAverage} from "../../models/monthly-average";
import {animate, query, stagger, state, style, transition, trigger} from "@angular/animations";
import {dropInAnimation} from "../../util/animations";

@Component({
  selector: 'app-monthly-numbers',
  templateUrl: './monthly-numbers.component.html',
  styleUrl: './monthly-numbers.component.css',
  animations: [dropInAnimation]
})
export class MonthlyNumbersComponent implements OnInit {
  @Input() uniqueSiteVisitors!: any[];
  @Input() totalLeads!: any[];
  @Input() opportunityToLead!: any[];
  @Input() totalValues!: any[];
  @Input() reportData!: KpiReport;
  @Input() selectedMonth!: string;
  @Input() isVisible!: { [key: string]: string };

  scheme = 'vivid';
  uniqueSiteVisitorsTooltip = false;
  totalLeadsTooltip = false;
  opportunityToLeadTooltip = false;
  totalValuesTooltip = false;

  ngOnInit(): void {
    this.populateNumberCards(this.reportData);
  }

  showTooltip(hoveredObject: string) {
    this.hideAllTooltips();

    if (hoveredObject === 'uniqueSiteVisitors') {
      this.uniqueSiteVisitorsTooltip = true;
    } else if (hoveredObject === 'totalLeads') {
      this.totalLeadsTooltip = true;
    } else if (hoveredObject === 'opportunityToLead') {
      this.opportunityToLeadTooltip = true;
    } else if (hoveredObject === 'totalValues') {
      this.totalValuesTooltip = true;
    }
  }

  hideAllTooltips() {
    this.uniqueSiteVisitorsTooltip = false;
    this.totalLeadsTooltip = false;
    this.opportunityToLeadTooltip = false;
    this.totalValuesTooltip = false;
  }


  private populateNumberCards(currentData: KpiReport): void {
    this.uniqueSiteVisitors = [{name: "Unique Site Visitors", value: currentData.uniqueSiteVisitors}];
    this.totalLeads = [{name: "Total Captured Leads", value: currentData.websiteLead.totalLeads}];
    this.opportunityToLead = [{name: "Opportunity-to-Lead", value: currentData.opportunityToLead}];
    this.totalValues = [{name: "Lead Valuation", value: currentData.websiteLead.totalValues}];
  }

  protected readonly SharedUtil = SharedUtil;
}
