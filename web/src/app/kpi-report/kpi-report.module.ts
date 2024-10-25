import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { KpiReportComponent } from './kpi-report/kpi-report.component';
import { KpiReportService } from './kpi-report.service';
import {FormsModule} from "@angular/forms";
import {
  AreaChartModule,
  BarChartModule,
  GaugeModule,
  LineChartModule,
  NumberCardModule,
  PieChartModule, TreeMapModule
} from "@swimlane/ngx-charts";
import {InViewportModule} from "@elvirus/angular-inviewport";
import {DropdownModule} from "primeng/dropdown";
import {TableModule} from "primeng/table";
import {AnimateOnScrollModule} from "primeng/animateonscroll";
import {ScrollTopModule} from "primeng/scrolltop";
import {SpeedDialModule} from "primeng/speeddial";
import {ProgressSpinnerModule} from "primeng/progressspinner";
import {ToggleButtonModule} from "primeng/togglebutton";
import {FloatLabelModule} from "primeng/floatlabel";
import {TabMenuModule} from "primeng/tabmenu";
import {AvatarModule} from "primeng/avatar";
import {TagModule} from "primeng/tag";
import {DialogModule} from "primeng/dialog";
import {InputNumberModule} from "primeng/inputnumber";
import { GoogleAnalyticsComponent } from './google-analytics/google-analytics.component';
import { OpportunityToLeadComponent } from './opportunity-to-lead/opportunity-to-lead.component';
import { MonthlyNumbersComponent } from './monthly-numbers/monthly-numbers.component';
import { CapturedLeadsCountComponent } from './captured-leads-count/captured-leads-count.component';
import { LeadValuationComponent } from './lead-valuation/lead-valuation.component';
import { MonthlyLeadsCapturedComponent } from './monthly-leads-captured/monthly-leads-captured.component';
import { AppointmentsComponent } from './appointments/appointments.component';
import { ContactsWonComponent } from './contacts-won/contacts-won.component';
import { PipelineStagesComponent } from './pipeline-stages/pipeline-stages.component';
import { AttributionDistributionComponent } from "./attribution-distribution/attribution-distribution.component";
import { ButtonModule} from "primeng/button";
import {SelectButtonModule} from "primeng/selectbutton";
import {MultiSelectModule} from "primeng/multiselect";
import {ChipModule} from "primeng/chip";

@NgModule({
  declarations: [KpiReportComponent, GoogleAnalyticsComponent, OpportunityToLeadComponent, MonthlyNumbersComponent, CapturedLeadsCountComponent, LeadValuationComponent, MonthlyLeadsCapturedComponent, AppointmentsComponent, ContactsWonComponent, ContactsWonComponent, PipelineStagesComponent, AttributionDistributionComponent],
    imports: [CommonModule, FormsModule, NumberCardModule, BarChartModule, LineChartModule, PieChartModule, InViewportModule, AreaChartModule, DropdownModule, TableModule, AnimateOnScrollModule, ScrollTopModule, SpeedDialModule, ProgressSpinnerModule, ToggleButtonModule, FloatLabelModule, GaugeModule, TreeMapModule, TabMenuModule, AvatarModule, TagModule, DialogModule, InputNumberModule, ButtonModule, SelectButtonModule, MultiSelectModule, ChipModule],
  providers: [KpiReportService],
})
export class KpiReportModule {}
