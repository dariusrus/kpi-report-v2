import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import {provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';  // Import HttpClientModule

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { KpiReportModule } from './kpi-report/kpi-report.module';
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import {InViewportModule} from "@elvirus/angular-inviewport";
import {environment} from "../environments/environment";
import {APP_CONFIG} from "./app.config";

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    KpiReportModule,
    BrowserAnimationsModule,
    InViewportModule
  ],
  providers: [provideHttpClient(withInterceptorsFromDi()), provideAnimationsAsync(), { provide: APP_CONFIG, useValue: environment.appConfig}],
  bootstrap: [AppComponent]
})
export class AppModule { }