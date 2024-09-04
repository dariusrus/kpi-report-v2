package com.blc.kpiReport.config;

import com.blc.kpiReport.models.request.GenerateKpiReportsRequest;
import com.blc.kpiReport.service.KpiReportGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
public class KpiReportGeneratorScheduler {

    private final KpiReportGeneratorService kpiReportGeneratorService;

    public KpiReportGeneratorScheduler(KpiReportGeneratorService kpiReportGeneratorService) {
        this.kpiReportGeneratorService = kpiReportGeneratorService;
    }

    /**
     * Scheduled job to run the batch report on the first day of every month at 12:30 AM.
     * This will generate the report for the previous month.
     */
    @Scheduled(cron = "0 30 0 1 * *")
    public void runMonthlyBatchReport() {
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
    }

    /**
     * Scheduled job to run the daily batch report every day at 12:15 AM.
     */
    @Scheduled(cron = "0 15 0 * * *")
    public void runDailyBatchReport() {
        log.info("Daily batch report cron job triggered at {}", LocalDate.now());

        // Trigger the daily report generation
        kpiReportGeneratorService.generateDailyMicrosoftAnalyticsReports(true);
        log.info("Daily batch report generation started for date: {}", LocalDate.now());
    }
}
