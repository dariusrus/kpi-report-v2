package com.blc.kpiReport.service;

import com.blc.kpiReport.config.GeneratorConfig;
import com.blc.kpiReport.config.GhlLocationsToGenerateProperties;
import com.blc.kpiReport.exception.MicrosoftClarityApiException;
import com.blc.kpiReport.models.ReportStatus;
import com.blc.kpiReport.models.request.GenerateClarityReportRequest;
import com.blc.kpiReport.models.request.GenerateKpiReportByLocationRequest;
import com.blc.kpiReport.models.request.GenerateKpiReportsRequest;
import com.blc.kpiReport.models.response.GenerateKpiReportBatchResponse;
import com.blc.kpiReport.models.response.GenerateKpiReportResponse;
import com.blc.kpiReport.repository.KpiReportRepository;
import com.blc.kpiReport.repository.MonthlyAverageRepository;
import com.blc.kpiReport.schema.GhlLocation;
import com.blc.kpiReport.schema.KpiReport;
import com.blc.kpiReport.schema.LeadSource;
import com.blc.kpiReport.schema.MonthlyAverage;
import com.blc.kpiReport.service.ga.GoogleAnalyticsService;
import com.blc.kpiReport.service.ghl.GoHighLevelApiService;
import com.blc.kpiReport.service.mc.MicrosoftClarityApiService;
import com.blc.kpiReport.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import static com.blc.kpiReport.util.DateUtil.formatMonthAndYear;
import static com.blc.kpiReport.util.NumberUtil.roundToTwoDecimalPlaces;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableAsync
public class KpiReportGeneratorService {

    private final GhlLocationsToGenerateProperties ghlLocations;
    private final GhlLocationService ghlLocationService;
    private final GoogleAnalyticsService googleAnalyticsService;
    private final GoHighLevelApiService goHighLevelApiService;
    private final MicrosoftClarityApiService microsoftClarityApiService;
    private final KpiReportRepository repository;
    private final MonthlyAverageRepository monthlyAverageRepository;
    private final GeneratorConfig generatorConfig;

    private Semaphore semaphore;

    @PostConstruct
    public void init() {
        this.semaphore = new Semaphore(generatorConfig.getSemaphorePermits());
    }

    @Transactional
    public CompletableFuture<List<GenerateKpiReportResponse>> generateAllKpiReports(GenerateKpiReportsRequest request) {
        List<GenerateKpiReportResponse> initialResponses = new ArrayList<>();
        List<String> uniqueLocationIds = new ArrayList<>(new HashSet<>(ghlLocations.getGhlLocationIds()));

        List<CompletableFuture<Void>> futures = uniqueLocationIds.stream()
            .map(locationId -> {
                var response = prepareInitialResponse(locationId, request.getMonth(), request.getYear(), initialResponses);
                return runAsyncReportGeneration(response, request.getMonth(), request.getYear());
            })
            .collect(Collectors.toList());
        return CompletableFuture.completedFuture(initialResponses);
    }

    @Transactional
    public CompletableFuture<GenerateKpiReportResponse> generateKpiReportByLocation(GenerateKpiReportByLocationRequest request) {
        var response = prepareInitialResponse(request.getGhlLocationId(), request.getMonth(), request.getYear(), new ArrayList<>());
        runAsyncReportGeneration(response, request.getMonth(), request.getYear());
        return CompletableFuture.completedFuture(response);
    }

    private GenerateKpiReportResponse prepareInitialResponse(String locationId, int month, int year, List<GenerateKpiReportResponse> initialResponses) {
        var ghlLocation = ghlLocationService.findByLocationId(locationId);
        var kpiReport = getOrCreateKpiReport(ghlLocation, month, year);
        var response = GenerateKpiReportResponse.builder()
            .id(kpiReport.getId())
            .subAgency(ghlLocation.getName())
            .ghlLocationId(locationId)
            .status(kpiReport.getLastRunStatus())
            .timeElapsed("N/A")
            .build();

        if (initialResponses != null) {
            initialResponses.add(response);
        }

        return response;
    }

    private CompletableFuture<Void> runAsyncReportGeneration(GenerateKpiReportResponse response, int month, int year) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Acquiring semaphore for location ID: {} (Name: {})", response.getGhlLocationId(), response.getSubAgency());
                semaphore.acquire();
                log.info("Semaphore acquired for location ID: {} (Name: {})", response.getGhlLocationId(), response.getSubAgency());
                Thread.sleep(2000); // Simulate delay
                var ghlLocation = ghlLocationService.findByLocationId(response.getGhlLocationId());
                var kpiReport = getOrCreateKpiReport(ghlLocation, month, year);
                generateKpiReport(ghlLocation, month, year, kpiReport).join();
            } catch (InterruptedException e) {
                log.error("Interrupted while acquiring semaphore for location ID: {} (Name: {})", response.getGhlLocationId(), response.getSubAgency(), e);
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
                log.info("Semaphore released for location ID: {} (Name: {})", response.getGhlLocationId(), response.getSubAgency());
            }
        });
    }

    @Async
    private CompletableFuture<Void> generateKpiReport(GhlLocation ghlLocation, int month, int year, KpiReport kpiReport) {
        var locationId = ghlLocation.getLocationId();
        kpiReport.setLastStartTime(Instant.now());
        setStatus(kpiReport, ReportStatus.ONGOING);
        try {
            log.info("Starting asynchronous report generation for location ID: {}", locationId);

            var dateRange = DateUtil.getFormattedDateRange(month, year);
            var startDate = dateRange.startDate();
            var endDate = dateRange.endDate();

            log.debug("Date range for KPI report: {} to {}", startDate, endDate);

            var googleAnalyticsMetric = googleAnalyticsService
                .fetchUniqueSiteVisitors(startDate, endDate, ghlLocation.getGaPropertyId(), kpiReport);

            log.debug("Fetched Google Analytics metric for KPI report ID: {}", kpiReport.getId());

            var goHighLevelReport = goHighLevelApiService
                .fetchGhlReporting(startDate, endDate, ghlLocation, kpiReport);

            log.debug("Fetched GoHighLevel report for KPI report ID: {}", kpiReport.getId());

            log.debug("Fetched GoHighLevel report for KPI report ID: {}", kpiReport.getId());

            kpiReport.setGoogleAnalyticsMetric(googleAnalyticsMetric);
            kpiReport.setGoHighLevelReport(goHighLevelReport);
            setStatus(kpiReport, ReportStatus.SUCCESS);
            logSuccessfulGeneration(ghlLocation, month, year, kpiReport);
        } catch (Exception e) {
            log.error("Error during report generation for location ID: {}", locationId, e);
            setStatus(kpiReport, ReportStatus.FAILED);
            throw new RuntimeException("Error generating KPI report for location ID: " + locationId, e);
        }
        return CompletableFuture.completedFuture(null);
    }

    private static void logSuccessfulGeneration(GhlLocation ghlLocation, int month, int year, KpiReport kpiReport) {
        log.info("================================================================" +
            "=====================================================================");
        log.info("SUCCESS! KPI report with ID: {} generated for Location - {} with ID {} for {}!",
            kpiReport.getId(),
            ghlLocation.getName(),
            ghlLocation.getLocationId(),
            formatMonthAndYear(month, year));
        log.info("================================================================" +
            "=====================================================================");
    }

    public GenerateKpiReportResponse getKpiReportStatus(Long id) {
        var kpiReportOptional = repository.findById(id);

        if (kpiReportOptional.isEmpty()) {
            log.error("KPI report not found with ID: {}", id);
            throw new RuntimeException("KpiReport not found with id: " + id);
        }

        var kpiReport = kpiReportOptional.get();
        return mapToResponse(kpiReport);
    }

    public GenerateKpiReportBatchResponse getKpiReportStatusByMonthAndYear(int month, int year) {
        List<KpiReport> reports = repository.findByMonthAndYear(month, year);

        List<GenerateKpiReportResponse> reportResponses = reports.stream()
            .sorted(Comparator.comparingLong(KpiReport::getId)) // Sort by ID
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        long totalReports = reportResponses.size();
        long successfulReports = reportResponses.stream()
            .filter(r -> r.getStatus() == ReportStatus.SUCCESS)
            .count();

        double percentageDone = (double) successfulReports / (double) totalReports * 100;
        String successRatio = String.format("%d of %d reports successfully generated", successfulReports, totalReports);

        List<String> failedReports = reportResponses.stream()
            .filter(r -> r.getStatus() == ReportStatus.FAILED)
            .map(r -> String.format("%s (%s)", r.getGhlLocationId(), r.getSubAgency()))
            .collect(Collectors.toList());

        String totalTimeElapsed;
        if (reportResponses.stream().anyMatch(r -> r.getStatus() == ReportStatus.ONGOING || r.getStatus() == ReportStatus.PENDING)) {
            totalTimeElapsed = "N/A";
        } else {
            // Sum the elapsed time from successful reports
            long totalDurationMillis = reportResponses.stream()
                .filter(r -> r.getStatus() == ReportStatus.SUCCESS)
                .mapToLong(r -> {
                    var kpiReport = repository.findById(r.getId()).orElse(null);
                    return kpiReport != null ? Duration.between(kpiReport.getLastStartTime(), kpiReport.getLastEndTime()).toMillis() : 0;
                })
                .sum();

            long seconds = (totalDurationMillis / 1000) % 60;
            long minutes = (totalDurationMillis / (1000 * 60)) % 60;
            long millis = totalDurationMillis % 1000;

            totalTimeElapsed = String.format("%02d:%02d.%03d", minutes, seconds, millis);
        }

        ReportStatus overallStatus = reportResponses.stream()
            .anyMatch(r -> r.getStatus() == ReportStatus.ONGOING || r.getStatus() == ReportStatus.PENDING)
            ? ReportStatus.ONGOING
            : (failedReports.isEmpty() ? ReportStatus.COMPLETED : ReportStatus.COMPLETED_W_FAILURES);

        return GenerateKpiReportBatchResponse.builder()
            .monthAndYear(formatMonthAndYear(month, year))
            .status(overallStatus)
            .percentageDone(roundToTwoDecimalPlaces(percentageDone))
            .successRatio(successRatio)
            .failedReports(failedReports)
            .totalTimeElapsed(totalTimeElapsed)
            .kpiReports(reportResponses)
            .build();
    }

    private GenerateKpiReportResponse mapToResponse(KpiReport kpiReport) {
        var response = GenerateKpiReportResponse.builder()
            .id(kpiReport.getId())
            .subAgency(kpiReport.getGhlLocation().getName())
            .ghlLocationId(kpiReport.getGhlLocation().getLocationId())
            .status(kpiReport.getLastRunStatus())
            .build();

        log.debug("KPI report status: {}", kpiReport.getLastRunStatus());
        if (isReportCompleted(kpiReport.getLastRunStatus())) {
            response.setTimeElapsed(calculateTimeElapsed(kpiReport));
        } else {
            response.setTimeElapsed("N/A");
            log.debug("Elapsed time not available for KPI report ID {} due to status: {}", kpiReport.getId(), kpiReport.getLastRunStatus());
        }
        return response;
    }

    private boolean isReportCompleted(ReportStatus status) {
        return status == ReportStatus.SUCCESS || status == ReportStatus.FAILED;
    }

    private String calculateTimeElapsed(KpiReport report) {
        if (report.getLastStartTime() != null && report.getLastEndTime() != null) {
            long durationMillis = Duration.between(report.getLastStartTime(), report.getLastEndTime()).toMillis();
            long seconds = (durationMillis / 1000) % 60;
            long minutes = (durationMillis / (1000 * 60)) % 60;
            long millis = durationMillis % 1000;

            return String.format("%02d:%02d.%03d", minutes, seconds, millis);
        }
        return "N/A";
    }

    private KpiReport getOrCreateKpiReport(GhlLocation ghlLocation, int month, int year) {
        log.debug("Checking for existing KPI report for month: {}, year: {}, location ID: {}", month, year, ghlLocation.getId());
        var existingKpiReport = repository.findByMonthAndYearAndGhlLocation_Id(month, year, ghlLocation.getId());

        if (existingKpiReport.isPresent()) {
            var kpiReport = existingKpiReport.get();
            log.info("Existing KPI report found with ID: {}", kpiReport.getId());
            setStatus(kpiReport, ReportStatus.PENDING);
            return kpiReport;
        } else {
            log.info("No existing KPI report found. Creating new report for location ID: {}", ghlLocation.getId());
            var kpiReport = repository.save(KpiReport.builder()
                .ghlLocation(ghlLocation)
                .month(month)
                .year(year)
                .lastRunStatus(ReportStatus.PENDING)
                .build());
            log.info("Created new KPI report for {} with ID: {}", ghlLocation.getName(), kpiReport.getId());
            return kpiReport;
        }
    }

    @Transactional
    public CompletableFuture<GenerateKpiReportResponse> generateDailyMicrosoftAnalyticsReport(GenerateClarityReportRequest request) {
        var ghlLocation = ghlLocationService.findByLocationId(request.getGhlLocationId());
        var requestDate = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        int day = requestDate.getDayOfMonth();
        int month = requestDate.getMonthValue();
        int year = requestDate.getYear();
        var kpiReport = getOrCreateKpiReport(ghlLocation, month, year);

        // Update KpiReport status and timing before fetching metrics
        kpiReport.setLastStartTime(Instant.now());
        setStatus(kpiReport, ReportStatus.ONGOING);

        var response = prepareInitialResponse(request.getGhlLocationId(), month, year, new ArrayList<>());
        runAsyncDailyReportGeneration(response, month, year, day);
        return CompletableFuture.completedFuture(response);
    }

    private CompletableFuture<Void> runAsyncDailyReportGeneration(GenerateKpiReportResponse response, int month, int year, int day) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Acquiring semaphore for location ID: {} (Name: {})", response.getGhlLocationId(), response.getSubAgency());
                semaphore.acquire();
                log.info("Semaphore acquired for location ID: {} (Name: {})", response.getGhlLocationId(), response.getSubAgency());
                Thread.sleep(2000); // Simulate delay
                var ghlLocation = ghlLocationService.findByLocationId(response.getGhlLocationId());
                var kpiReport = getOrCreateKpiReport(ghlLocation, month, year);
                generateDailyMicrosoftAnalyticsReport(ghlLocation, kpiReport, day).join();
            } catch (InterruptedException e) {
                log.error("Interrupted while acquiring semaphore for location ID: {} (Name: {})", response.getGhlLocationId(), response.getSubAgency(), e);
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
                log.info("Semaphore released for location ID: {} (Name: {})", response.getGhlLocationId(), response.getSubAgency());
            }
        });
    }

    @Async
    public CompletableFuture<Void> generateDailyMicrosoftAnalyticsReport(GhlLocation ghlLocation, KpiReport kpiReport, int day) {
        kpiReport.setLastStartTime(Instant.now());
        setStatus(kpiReport, ReportStatus.ONGOING);
        try {
            // Fetch metrics for the previous day
            var dailyMetric = microsoftClarityApiService.fetchMetricsForPreviousDay(ghlLocation, kpiReport, day);
            // for some reason, the program execute the code below
            log.info("Fetched metrics for day {}: {}", day, dailyMetric);
            setStatus(kpiReport, ReportStatus.SUCCESS);
        } catch (IOException | MicrosoftClarityApiException e) {
            log.error("Error during report generation for location ID: {}", ghlLocation.getLocationId(), e);
            setStatus(kpiReport, ReportStatus.FAILED);
        } finally {
            kpiReport.setLastEndTime(Instant.now());
            log.info("Report generation process finished for location ID: {}", ghlLocation.getLocationId());
        }
        return CompletableFuture.completedFuture(null);
    }

    private void setStatus(KpiReport kpiReport, ReportStatus status) {
        kpiReport.setLastRunStatus(status);
        if (ReportStatus.SUCCESS == status || ReportStatus.FAILED == status) {
            kpiReport.setLastEndTime(Instant.now());
        }
        repository.save(kpiReport);
        log.info("KPI Report was saved with status {}", kpiReport.getLastRunStatus());
    }

    @Transactional
    public void calculateAverageOpportunityToLead(GenerateKpiReportsRequest request) {
        List<KpiReport> reports = repository.findByMonthAndYear(request.getMonth(), request.getYear());

        if (reports.isEmpty()) {
            log.info("No reports found for the specified month and year.");
            return;
        }

        double totalUniqueSiteVisitors = 0;
        int totalLeadSources = 0;
        int reportCount = reports.size();

        double sumOpportunityToLead = 0;

        for (KpiReport report : reports) {
            if (report.getGoogleAnalyticsMetric() != null) {
                totalUniqueSiteVisitors += report.getGoogleAnalyticsMetric().getUniqueSiteVisitors();
            }

            if (report.getGoHighLevelReport() != null) {
                List<LeadSource> leadSources = report.getGoHighLevelReport().getLeadSources();
                for (LeadSource leadSource : leadSources) {
                    totalLeadSources += leadSource.getTotalLeads();
                }
            }

            if (report.getGoogleAnalyticsMetric() != null && report.getGoHighLevelReport() != null) {
                double opportunityToLead = (report.getGoHighLevelReport().getLeadSources().stream().mapToInt(LeadSource::getTotalLeads).sum()
                    / (double) report.getGoogleAnalyticsMetric().getUniqueSiteVisitors()) * 100;
                sumOpportunityToLead += opportunityToLead;
            }
        }

        double averageUniqueSiteVisitors = totalUniqueSiteVisitors / reportCount;
        double averageTotalLeads = totalLeadSources / (double) reportCount;
        double weightedAverageOpportunityToLead = (totalLeadSources / totalUniqueSiteVisitors) * 100;
        double nonWeightedAverageOpportunityToLead = sumOpportunityToLead / reportCount;

        log.info("Average Unique Site Visitors: {}", averageUniqueSiteVisitors);
        log.info("Average Total Leads: {}", averageTotalLeads);
        log.info("Weighted Average Opportunity-to-Lead: {}", weightedAverageOpportunityToLead);
        log.info("Non-Weighted Average Opportunity-to-Lead: {}", nonWeightedAverageOpportunityToLead);

        MonthlyAverage monthlyAverage = monthlyAverageRepository.findByMonthAndYear(request.getMonth(), request.getYear())
            .orElse(new MonthlyAverage());

        monthlyAverage.setMonth(request.getMonth());
        monthlyAverage.setYear(request.getYear());
        monthlyAverage.setAverageUniqueSiteVisitors((int) averageUniqueSiteVisitors);
        monthlyAverage.setAverageTotalLeads((int) averageTotalLeads);
        monthlyAverage.setAverageOpportunityToLead(nonWeightedAverageOpportunityToLead);
        monthlyAverage.setWeightedAverageOpportunityToLead(weightedAverageOpportunityToLead);

        monthlyAverageRepository.save(monthlyAverage);
    }
}