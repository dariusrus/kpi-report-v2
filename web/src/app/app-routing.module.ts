import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {KpiReportComponent} from "./kpi-report/kpi-report/kpi-report.component";

const routes: Routes = [
  { path: 'kpi-report/:id', component: KpiReportComponent },
  { path: '', redirectTo: '/kpi-report/lWZrtj8byFZrUI9Cg2eX', pathMatch: 'full' },
  { path: '**', redirectTo: '/kpi-report/lWZrtj8byFZrUI9Cg2eX' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
