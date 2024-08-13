import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { KpiReportComponent } from './kpi-report/kpi-report.component';
import { KpiReportService } from './kpi-report.service';
import {FormsModule} from "@angular/forms";
import {AreaChartModule, BarChartModule, LineChartModule, NumberCardModule, PieChartModule} from "@swimlane/ngx-charts";
import {InViewportModule} from "@elvirus/angular-inviewport";
import {DropdownModule} from "primeng/dropdown";
import {TableModule} from "primeng/table";
import {AnimateOnScrollModule} from "primeng/animateonscroll";
import {ScrollTopModule} from "primeng/scrolltop";
import {SpeedDialModule} from "primeng/speeddial";
import {ProgressSpinnerModule} from "primeng/progressspinner";
import {ToggleButtonModule} from "primeng/togglebutton";
import {FloatLabelModule} from "primeng/floatlabel";

@NgModule({
  declarations: [KpiReportComponent],
  imports: [CommonModule, FormsModule, NumberCardModule, BarChartModule, LineChartModule, PieChartModule, InViewportModule, AreaChartModule, DropdownModule, TableModule, AnimateOnScrollModule, ScrollTopModule, SpeedDialModule, ProgressSpinnerModule, ToggleButtonModule, FloatLabelModule],
  providers: [KpiReportService],
})
export class KpiReportModule {}
