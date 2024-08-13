import { InjectionToken } from '@angular/core';

export interface AppConfig {
  previousMonthsCount: number;
}

export const APP_CONFIG = new InjectionToken<AppConfig>('app.config');
