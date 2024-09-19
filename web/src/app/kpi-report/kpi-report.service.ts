import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { KpiReport } from '../models/kpi-report';
import { MonthlyAverage } from '../models/monthly-average'; // Adjust the import path as needed
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class KpiReportService {
  private apiUrl = environment.apiUrl;
  private apiKey = environment.apiKey;

  constructor(private http: HttpClient) {}

  getReportData(ghlLocationId: string, month: number, year: number): Observable<KpiReport> {
    const headers = new HttpHeaders({
      'accept': 'application/json',
      'X-BLC-API-KEY': this.apiKey
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
      'X-BLC-API-KEY': this.apiKey
    });

    const params = {
      month: month.toString(),
      year: year.toString(),
      clientType: clientType
    };

    return this.http.get<MonthlyAverage>(`${this.apiUrl}/monthly/average`, { headers, params });
  }

}
