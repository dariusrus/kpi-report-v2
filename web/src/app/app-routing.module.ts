import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {KpiReportComponent} from "./kpi-report/kpi-report/kpi-report.component";

const routes: Routes = [
  { path: 'kpi-report/:id', component: KpiReportComponent },
  { path: '', redirectTo: '/kpi-report/lWZrtj8byFZrUI9Cg2eX', pathMatch: 'full' },  // Default route with a default ID
  { path: '**', redirectTo: '/kpi-report/lWZrtj8byFZrUI9Cg2eX' }  // Wildcard route to catch undefined paths
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
