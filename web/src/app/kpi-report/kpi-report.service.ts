import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { KpiReport } from '../models/kpi-report';
import {MonthlyAverage} from "../models/monthly-average"; // Adjust the import path as needed

@Injectable({
  providedIn: 'root'
})
export class KpiReportService {
  private apiUrl = 'http://localhost:8325/blc-kpi/reports'; // Updated API endpoint

  constructor(private http: HttpClient) {}

  getReportData(ghlLocationId: string, month: number, year: number): Observable<KpiReport> {
    const headers = new HttpHeaders({
      'accept': 'application/json',
      'X-BLC-API-KEY': '27fc2f8f489e4813a965114be60893d4'
    });

    const params = {
      ghlLocationId,
      month: month.toString(),
      year: year.toString()
    };

    return this.http.get<KpiReport>(this.apiUrl, { headers, params });
  }

  getMonthlyAverage(month: number, year: number, clientType: string): Observable<MonthlyAverage> {
    const headers = new HttpHeaders({
      'accept': 'application/json',
      'X-BLC-API-KEY': '27fc2f8f489e4813a965114be60893d4'
    });

    const params = {
      month: month.toString(),
      year: year.toString(),
      clientType: clientType
    };

    return this.http.get<MonthlyAverage>(`${this.apiUrl}/monthly/average`, { headers, params });
  }
}