package com.blc.kpiReport.config;

import com.blc.kpiReport.models.request.GenerateKpiReportsRequest;
import com.blc.kpiReport.service.KpiReportGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@RequiredArgsConstructor
@Component
@Slf4j
public class KpiReportGeneratorScheduler {

    private final KpiReportGeneratorService kpiReportGeneratorService;

    private boolean monthlyCronEnabled = true;
    private boolean dailyCronEnabled = true;

    @Scheduled(cron = "0 30 0 1 * *")
    public void runMonthlyBatchReport() {
        if (monthlyCronEnabled) {
            LocalDate now = LocalDate.now();
            LocalDate previousMonth = now.minusMonths(1);
            int month = previousMonth.getMonthValue();
            int year = previousMonth.getYear();

            log.info("Monthly batch report cron job triggered for month: {}, year: {}", month, year);

            GenerateKpiReportsRequest request = new GenerateKpiReportsRequest();
            request.setMonth(month);
            request.setYear(year);

            kpiReportGeneratorService.generateAllKpiReports(request, true);
            log.info("Monthly batch report generation started for month: {}, year: {}", month, year);
        } else {
            log.info("Monthly cron job is disabled.");
        }
    }

    @Scheduled(cron = "0 15 0 * * *")
    public void runDailyBatchReport() {
        if (dailyCronEnabled) {
            log.info("Daily batch report cron job triggered at {}", LocalDate.now());

            kpiReportGeneratorService.generateDailyMicrosoftAnalyticsReports(true);
            log.info("Daily batch report generation started for date: {}", LocalDate.now());
        } else {
            log.info("Daily cron job is disabled.");
        }
    }

    public void setMonthlyCronEnabled(boolean enabled) {
        this.monthlyCronEnabled = enabled;
    }

    public void setDailyCronEnabled(boolean enabled) {
        this.dailyCronEnabled = enabled;
    }
}