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

@NgModule({
  declarations: [KpiReportComponent],
  imports: [CommonModule, FormsModule, NumberCardModule, BarChartModule, LineChartModule, PieChartModule, InViewportModule, AreaChartModule, DropdownModule, TableModule, AnimateOnScrollModule, ScrollTopModule, SpeedDialModule, ProgressSpinnerModule, ToggleButtonModule, FloatLabelModule, GaugeModule, TreeMapModule, TabMenuModule, AvatarModule, TagModule],
  providers: [KpiReportService],
})
export class KpiReportModule {}
