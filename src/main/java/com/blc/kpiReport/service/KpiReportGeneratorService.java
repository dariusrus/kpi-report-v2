package com.blc.kpiReport.service;

import com.blc.kpiReport.config.GeneratorConfig;
import com.blc.kpiReport.config.GhlLocationsToGenerateProperties;
import com.blc.kpiReport.exception.MicrosoftClarityApiException;
import com.blc.kpiReport.models.ClientType;
import com.blc.kpiReport.models.ReportStatus;
import com.blc.kpiReport.models.request.GenerateClarityReportRequest;
import com.blc.kpiReport.models.request.GenerateKpiReportByLocationRequest;
import com.blc.kpiReport.models.request.GenerateKpiReportsRequest;
import com.blc.kpiReport.models.response.GenerateKpiReportBatchResponse;
import com.blc.kpiReport.models.response.GenerateKpiReportResponse;
import com.blc.kpiReport.repository.KpiReportRepository;
import com.blc.kpiReport.repository.ghl.MonthlyAverageRepository;
import com.blc.kpiReport.repository.mc.MonthlyClarityReportRepository;
import com.blc.kpiReport.schema.GhlLocation;
import com.blc.kpiReport.schema.KpiReport;
import com.blc.kpiReport.schema.MonthlyAverage;
import com.blc.kpiReport.schema.ghl.FollowUpConversion;
import com.blc.kpiReport.schema.ghl.LeadSource;
import com.blc.kpiReport.schema.mc.*;
import com.blc.kpiReport.service.ga.GoogleAnalyticsService;
import com.blc.kpiReport.service.ghl.GoHighLevelApiService;
import com.blc.kpiReport.service.mc.MicrosoftClarityApiService;
import com.blc.kpiReport.service.openai.OpenAIGeneratorService;
import com.blc.kpiReport.util.DateUtil;
import jakarta.mail.MessagingException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import static com.blc.kpiReport.util.DateUtil.formatDayMonthYear;
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
    private final MonthlyClarityReportRepository monthlyClarityReportRepository;
    private final GeneratorConfig generatorConfig;
    private final MailService mailService;

    private Semaphore semaphore;

    @PostConstruct
    public void init() {
        this.semaphore = new Semaphore(generatorConfig.getSemaphorePermits());
    }

    @Transactional
    public CompletableFuture<List<GenerateKpiReportResponse>> generateAllKpiReports(GenerateKpiReportsRequest request, boolean isCronJob) {
        List<GenerateKpiReportResponse> initialResponses = new ArrayList<>();
        List<String> uniqueLocationIds = new ArrayList<>(new HashSet<>(ghlLocations.getGhlLocationIds()));
        var processedLocationIds = new ConcurrentHashMap<String, Boolean>();

        CompletableFuture<Void> reportGeneration = CompletableFuture.allOf(
                uniqueLocationIds.stream()
                        .map(locationId -> {
                            if (processedLocationIds.putIfAbsent(locationId, true) == null) {
                                var response = prepareInitialResponse(locationId, request.getMonth(), request.getYear(), initialResponses);
                                return runAsyncReportGeneration(response, request.getMonth(), request.getYear());
                            } else {
                                log.debug("Skipping already processed location ID: {}", locationId);
                                return CompletableFuture.completedFuture(null);
                            }
                        })
                        .toArray(CompletableFuture[]::new)
        );

        reportGeneration.whenComplete((result, throwable) -> finalizeBatchReport(request.getMonth(), request.getYear(), isCronJob));

        return CompletableFuture.completedFuture(initialResponses);
    }

    private void finalizeBatchReport(int month, int year, boolean isCronJob) {
        try {
            log.info("Sending batch report results for month: {}, year: {}", month, year);
            var kpiReportStatus = getKpiReportStatusByMonthAndYear(month, year);
            mailService.sendReportNotification(kpiReportStatus, isCronJob);
        } catch (Exception e) {
            log.error("Error during batch finalization for month: {}, year: {}", month, year, e);
        }
    }

    private void finalizeBatchReportDaily(int day, int month, int year, boolean isCronJob) {
        try {
            String formattedDate = formatDayMonthYear(day, month, year);
            log.info("Sending daily report results for date: {}", formattedDate);
            var kpiReportStatus = getKpiReportStatusByMonthAndYear(month, year);
            mailService.sendDailyReportNotification(formattedDate, kpiReportStatus, isCronJob);
        } catch (Exception e) {
            String formattedDate = formatDayMonthYear(day, month, year);
            log.error("Error during daily report finalization for date: {}", formattedDate, e);
        }
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
            } catch (InterruptedException | MessagingException e) {
                log.error("Interrupted while acquiring semaphore for location ID: {} (Name: {})", response.getGhlLocationId(), response.getSubAgency(), e);
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
                log.info("Semaphore released for location ID: {} (Name: {})", response.getGhlLocationId(), response.getSubAgency());
            }
        });
    }

    @Async
    private CompletableFuture<Void> generateKpiReport(GhlLocation ghlLocation, int month, int year, KpiReport kpiReport) throws MessagingException {
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
                .fetchGaReporting(startDate, endDate, ghlLocation.getGaPropertyId(), ghlLocation.getGaCountryCode(), kpiReport);

            log.debug("Fetched Google Analytics metric for KPI report ID: {}", kpiReport.getId());

            var goHighLevelReport = goHighLevelApiService
                .fetchGhlReporting(startDate, endDate, ghlLocation, kpiReport);

            log.debug("Fetched GoHighLevel report for KPI report ID: {}", kpiReport.getId());

            log.debug("Fetched GoHighLevel report for KPI report ID: {}", kpiReport.getId());

            kpiReport.setGoogleAnalyticsMetric(googleAnalyticsMetric);
            kpiReport.setGoHighLevelReport(goHighLevelReport);
            aggregateDailyToMonthlyClarityReport(kpiReport);
            setStatus(kpiReport, ReportStatus.SUCCESS);
            logSuccessfulGeneration(ghlLocation, month, year, kpiReport);
        } catch (Exception e) {
            log.error("Error during report generation for location ID: {}", locationId, e);
            setStatus(kpiReport, ReportStatus.FAILED);
            throw new RuntimeException("Error generating KPI report for location ID: " + locationId, e);
        }
        return CompletableFuture.completedFuture(null);
    }

    private void aggregateDailyToMonthlyClarityReport(KpiReport kpiReport) {
        log.info("Starting aggregation of daily metrics to monthly report for KPI Report ID: {}", kpiReport.getId());
        MonthlyClarityReport monthlyClarityReport = null;

        if (!ObjectUtils.isEmpty(kpiReport.getMonthlyClarityReport())) {
            monthlyClarityReport = kpiReport.getMonthlyClarityReport();
            monthlyClarityReport.setKpiReport(null);
            kpiReport.setMonthlyClarityReport(null);
            repository.save(kpiReport);
            monthlyClarityReportRepository.delete(monthlyClarityReport);
        }

        monthlyClarityReport = new MonthlyClarityReport();
        monthlyClarityReport.setKpiReport(kpiReport);
        monthlyClarityReport = monthlyClarityReportRepository.save(monthlyClarityReport);

        Map<String, Map<String, DeviceMetric>> urlDeviceMetricMap = new HashMap<>();
        Map<String, Map<String, Integer>> urlDeviceDayCountMap = new HashMap<>();

        for (DailyMetric dailyMetric : kpiReport.getDailyMetrics()) {
            log.info("Processing DailyMetric for day: {}", dailyMetric.getDay());

            for (Metric metric : dailyMetric.getMetrics()) {
                log.debug("Processing Metric: {}", metric.getMetricName());

                for (Information info : metric.getInformationList()) {
                    String url = info.getUrl();
                    String deviceType = info.getDevice();

                    if (url == null || deviceType == null) {
                        continue; // Skip if any of these values are null
                    }

                    log.debug("Processing Information for URL: {} and Device: {}", url, deviceType);

                    urlDeviceMetricMap.putIfAbsent(url, new HashMap<>());
                    Map<String, DeviceMetric> deviceMetricMap = urlDeviceMetricMap.get(url);

                    deviceMetricMap.putIfAbsent(deviceType, new DeviceMetric());
                    DeviceMetric deviceMetric = deviceMetricMap.get(deviceType);

                    if ("Traffic".equals(metric.getMetricName())) {
                        Integer totalSessionCount = deviceMetric.getTotalSessionCount() == null ? 0 : deviceMetric.getTotalSessionCount();
                        totalSessionCount += Integer.valueOf(info.getTotalSessionCount());
                        deviceMetric.setTotalSessionCount(totalSessionCount);
                        log.debug("Aggregated Session Count for URL: {} and Device: {}. New total: {}", url, deviceType, totalSessionCount);
                    }
                    if ("ScrollDepth".equals(metric.getMetricName())) {
                        double totalScrollDepth = deviceMetric.getAverageScrollDepth() == null ? 0 : deviceMetric.getAverageScrollDepth();
                        totalScrollDepth += info.getAverageScrollDepth();
                        deviceMetric.setAverageScrollDepth(totalScrollDepth);
                        log.debug("Aggregated ScrollDepth for URL: {} and Device: {}. New total: {}", url, deviceType, totalScrollDepth);
                    }
                    if ("EngagementTime".equals(metric.getMetricName())) {
                        if (info.getTotalTime() != null) {
                            int totalTime = deviceMetric.getTotalTime() == null ? 0 : deviceMetric.getTotalTime();
                            totalTime += info.getTotalTime();
                            deviceMetric.setTotalTime(totalTime);
                            log.debug("Aggregated EngagementTime (Total) for URL: {} and Device: {}. New total time: {}", url, deviceType, totalTime);
                        }

                        if (info.getActiveTime() != null) {
                            int activeTime = deviceMetric.getActiveTime() == null ? 0 : deviceMetric.getActiveTime();
                            activeTime += info.getActiveTime();
                            deviceMetric.setActiveTime(activeTime);
                            log.debug("Aggregated EngagementTime (Active) for URL: {} and Device: {}. New active time: {}", url, deviceType, activeTime);
                        }
                    }
                    deviceMetric.setDeviceType(deviceType);

                    urlDeviceDayCountMap.putIfAbsent(url, new HashMap<>());
                    Map<String, Integer> deviceDayCountMap = urlDeviceDayCountMap.get(url);
                    deviceDayCountMap.put(deviceType, deviceDayCountMap.getOrDefault(deviceType, 0) + 0);
                    if (info.getAverageScrollDepth() != null) {
                        deviceDayCountMap.put(deviceType, deviceDayCountMap.getOrDefault(deviceType, 0) + 1);
                    }
                }
            }
        }

        List<UrlMetric> urlMetrics = new ArrayList<>();
        for (Map.Entry<String, Map<String, DeviceMetric>> entry : urlDeviceMetricMap.entrySet()) {
            String url = entry.getKey();
            Map<String, DeviceMetric> deviceMetricsMap = entry.getValue();
            log.debug("Creating UrlMetric for URL: {}", url);

            if (url == null) {
                log.error("URL is null for entry: {}", entry);
                continue;
            }

            UrlMetric urlMetric = new UrlMetric();
            urlMetric.setUrl(url);
            urlMetric.setMonthlyClarityReport(monthlyClarityReport);

            List<DeviceMetric> deviceMetrics = new ArrayList<>();
            for (Map.Entry<String, DeviceMetric> deviceEntry : deviceMetricsMap.entrySet()) {
                DeviceMetric deviceMetric = deviceEntry.getValue();
                String deviceType = deviceEntry.getKey();

                if (deviceType != null) {
                    deviceMetric.setDeviceType(deviceType);
                } else {
                    log.error("DeviceType is null for URL: {}", url);
                    continue;
                }

                int validDays = urlDeviceDayCountMap.get(url).get(deviceType);

                if (deviceMetric.getAverageScrollDepth() != null && validDays > 0) {
                    double averageScrollDepth = deviceMetric.getAverageScrollDepth() / validDays;
                    deviceMetric.setAverageScrollDepth(averageScrollDepth);
                    log.debug("Calculated average ScrollDepth for Device: {} and URL: {}. Average: {}. Valid Days: {}.", deviceType, url, averageScrollDepth, validDays);
                }

                deviceMetric.setUrlMetric(urlMetric);
                deviceMetrics.add(deviceMetric);
            }
            urlMetric.setDevices(deviceMetrics);
            urlMetrics.add(urlMetric);
        }
        urlMetrics.sort(Comparator.comparingInt((UrlMetric urlMetric) ->
            urlMetric.getDevices().stream()
            .mapToInt(deviceMetric -> deviceMetric.getActiveTime() != null ? deviceMetric.getActiveTime() : 0)
            .sum()
        ).reversed());
        monthlyClarityReport.setUrls(urlMetrics);

        log.info("Saving MonthlyClarityReport for KPI Report ID: {}", kpiReport.getId());
        monthlyClarityReportRepository.save(monthlyClarityReport);
        log.info("Aggregation and persistence completed for KPI Report ID: {}", kpiReport.getId());
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

    public KpiReport getOrCreateKpiReport(String locationId, int month, int year) {
        var ghlLocation = ghlLocationService.findByLocationId(locationId);
        return getOrCreateKpiReport(ghlLocation, month, year);
    }

    public KpiReport getOrCreateKpiReport(GhlLocation ghlLocation, int month, int year) {
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
            var dailyMetric = microsoftClarityApiService.fetchMetricsForPreviousDay(ghlLocation, kpiReport, day);
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

    @Transactional
    public void setStatus(KpiReport kpiReport, ReportStatus status) {
        kpiReport.setLastRunStatus(status);
        if (ReportStatus.SUCCESS == status || ReportStatus.FAILED == status) {
            kpiReport.setLastEndTime(Instant.now());
        }
        repository.save(kpiReport);
        log.info("KPI Report was saved with status {}", kpiReport.getLastRunStatus());
    }

    @Transactional
    public void calculateIndustryAverages(GenerateKpiReportsRequest request) {
        List<KpiReport> reports = repository.findByMonthAndYear(request.getMonth(), request.getYear());

        if (reports.isEmpty()) {
            log.info("No reports found for the specified month and year.");
            return;
        }

        Map<ClientType, Integer> reportCountMap = initializeMapWithDefault(ClientType.values(), 0);

        Map<ClientType, Double> totalUniqueSiteVisitorsMap = initializeMapWithDefault(ClientType.values(), 0.0);
        Map<ClientType, Integer> totalLeadSourcesMap = initializeMapWithDefault(ClientType.values(), 0);
        Map<ClientType, Double> sumOpportunityToLeadMap = initializeMapWithDefault(ClientType.values(), 0.0);
        Map<ClientType, Integer> validOpportunityToLeadCountMap = initializeMapWithDefault(ClientType.values(), 0);

        Map<ClientType, Integer> liveChatMap = initializeMapWithDefault(ClientType.values(), 0);
        Map<ClientType, Integer> smsMap = initializeMapWithDefault(ClientType.values(), 0);
        Map<ClientType, Integer> emailMap = initializeMapWithDefault(ClientType.values(), 0);
        Map<ClientType, Integer> callMap = initializeMapWithDefault(ClientType.values(), 0);

        Map<ClientType, Integer> totalFollowUpsMap = initializeMapWithDefault(ClientType.values(), 0);
        Map<ClientType, Integer> totalConversionsMap = initializeMapWithDefault(ClientType.values(), 0);

        for (KpiReport report : reports) {
            ClientType clientType = report.getGhlLocation().getClientType();
            if (clientType == null) {
                continue;
            }
            reportCountMap.put(clientType, reportCountMap.get(clientType) + 1);

            populateUniqueSiteVisitors(report, totalUniqueSiteVisitorsMap, clientType);
            populateLeadSources(report, totalLeadSourcesMap, clientType);
            populateOpportunityToLead(report, sumOpportunityToLeadMap, clientType, validOpportunityToLeadCountMap);
            populateFollowUpConversionMaps(report, totalFollowUpsMap, totalConversionsMap,
                    liveChatMap, smsMap, emailMap, callMap, clientType);
        }

        for (ClientType clientType : ClientType.values()) {
            if (reportCountMap.get(clientType) == 0) {
                continue;
            }

            double averageUniqueSiteVisitors = totalUniqueSiteVisitorsMap.get(clientType) / reportCountMap.get(clientType);
            double averageTotalLeads = totalLeadSourcesMap.get(clientType) / (double) reportCountMap.get(clientType);

            double weightedAverageOpportunityToLead = 0;
            double nonWeightedAverageOpportunityToLead = 0;

            weightedAverageOpportunityToLead = calculateWeightedAverageO2L(clientType, totalUniqueSiteVisitorsMap, weightedAverageOpportunityToLead, totalLeadSourcesMap);
            nonWeightedAverageOpportunityToLead = calculateNonWeightedAverageO2L(clientType, validOpportunityToLeadCountMap, nonWeightedAverageOpportunityToLead, sumOpportunityToLeadMap);

            int averageSms = smsMap.get(clientType) / reportCountMap.get(clientType);
            int averageCalls = callMap.get(clientType) / reportCountMap.get(clientType);
            int averageEmails = emailMap.get(clientType) / reportCountMap.get(clientType);
            int averageLiveChat = liveChatMap.get(clientType) / reportCountMap.get(clientType);

            int averageTotalFollowUps = averageSms + averageCalls + averageEmails + averageLiveChat;
            int averageTotalConversions = totalConversionsMap.get(clientType) / reportCountMap.get(clientType);

            double averageTotalFollowUpPerConversion = 0;

            if (averageTotalConversions > 0) {
                averageTotalFollowUpPerConversion = (double) totalFollowUpsMap.get(clientType) / totalConversionsMap.get(clientType);
            }

            log.info("ClientType: {}", clientType);
            log.info("Average Unique Site Visitors: {}", averageUniqueSiteVisitors);
            log.info("Average Total Leads: {}", averageTotalLeads);
            log.info("Weighted Average Opportunity-to-Lead: {}", weightedAverageOpportunityToLead);
            log.info("Non-Weighted Average Opportunity-to-Lead: {}", nonWeightedAverageOpportunityToLead);
            log.info("Average SMS: {}", averageSms);
            log.info("Average Live Chat Messages: {}", averageLiveChat);
            log.info("Average Emails: {}", averageEmails);
            log.info("Average Calls: {}", averageCalls);
            log.info("Average Total Follow-Ups: {}", averageTotalFollowUps);
            log.info("Average Total Conversions: {}", averageTotalConversions);
            log.info("Average Total Follow-Up Per Conversion: {}", averageTotalFollowUpPerConversion);

            MonthlyAverage monthlyAverage = monthlyAverageRepository.findByMonthAndYearAndClientType(request.getMonth(), request.getYear(), clientType)
                    .orElse(new MonthlyAverage());

            monthlyAverage.setMonth(request.getMonth());
            monthlyAverage.setYear(request.getYear());
            monthlyAverage.setClientType(clientType);
            monthlyAverage.setAverageUniqueSiteVisitors((int) averageUniqueSiteVisitors);
            monthlyAverage.setAverageTotalLeads((int) averageTotalLeads);
            monthlyAverage.setAverageOpportunityToLead(nonWeightedAverageOpportunityToLead);
            monthlyAverage.setWeightedAverageOpportunityToLead(weightedAverageOpportunityToLead);
            monthlyAverage.setAverageTotalFollowUps(averageTotalFollowUps);
            monthlyAverage.setAverageTotalConversions(averageTotalConversions);
            monthlyAverage.setAverageTotalFollowUpPerConversion(averageTotalFollowUpPerConversion);
            monthlyAverage.setAverageLiveChatMessage(averageLiveChat);
            monthlyAverage.setAverageSms(averageSms);
            monthlyAverage.setAverageEmails(averageEmails);
            monthlyAverage.setAverageCalls(averageCalls);

            monthlyAverageRepository.save(monthlyAverage);
        }
    }

    @Transactional
    public CompletableFuture<List<GenerateKpiReportResponse>> generateDailyMicrosoftAnalyticsReports(boolean isCronJob) {
        List<GenerateKpiReportResponse> initialResponses = new ArrayList<>();
        List<String> uniqueLocationIds = new ArrayList<>(new HashSet<>(ghlLocations.getGhlLocationIds()));

        uniqueLocationIds.forEach(locationId -> {
            var response = prepareInitialResponse(locationId, getCurrentMonth(), getCurrentYear(), initialResponses);
            runAsyncDailyReportGeneration(response, getCurrentMonth(), getCurrentYear(), getCurrentDay());
        });

        CompletableFuture.runAsync(() -> {
            CompletableFuture.allOf(
                    uniqueLocationIds.stream()
                            .map(locationId -> {
                                var response = prepareInitialResponse(locationId, getCurrentMonth(), getCurrentYear(), null);
                                return runAsyncDailyReportGeneration(response, getCurrentMonth(), getCurrentYear(), getCurrentDay());
                            })
                            .toArray(CompletableFuture[]::new)
            ).whenComplete((result, throwable) -> finalizeBatchReportDaily(getCurrentDay(), getCurrentMonth(), getCurrentYear(), isCronJob));
        });

        return CompletableFuture.completedFuture(initialResponses);
    }

    private double calculateNonWeightedAverageO2L(ClientType clientType, Map<ClientType, Integer> validOpportunityToLeadCountMap, double nonWeightedAverageOpportunityToLead, Map<ClientType, Double> sumOpportunityToLeadMap) {
        if (validOpportunityToLeadCountMap.get(clientType) > 0) {
            nonWeightedAverageOpportunityToLead = sumOpportunityToLeadMap.get(clientType) / validOpportunityToLeadCountMap.get(clientType);
        }
        return nonWeightedAverageOpportunityToLead;
    }

    private double calculateWeightedAverageO2L(ClientType clientType, Map<ClientType, Double> totalUniqueSiteVisitorsMap, double weightedAverageOpportunityToLead, Map<ClientType, Integer> totalLeadSourcesMap) {
        if (totalUniqueSiteVisitorsMap.get(clientType) > 0) {
            weightedAverageOpportunityToLead = (totalLeadSourcesMap.get(clientType) / totalUniqueSiteVisitorsMap.get(clientType)) * 100;
        }
        return weightedAverageOpportunityToLead;
    }

    private void populateOpportunityToLead(KpiReport report,
                                           Map<ClientType, Double> sumOpportunityToLeadMap,
                                           ClientType clientType,
                                           Map<ClientType, Integer> validOpportunityToLeadCountMap) {
        if (report.getGoogleAnalyticsMetric() != null && report.getGoogleAnalyticsMetric().getUniqueSiteVisitors() > 0 &&
            report.getGoHighLevelReport() != null && !report.getGoHighLevelReport().getLeadSources().isEmpty()) {

            double uniqueVisitors = report.getGoogleAnalyticsMetric().getUniqueSiteVisitors();
            double totalLeads = report.getGoHighLevelReport().getLeadSources().stream().mapToInt(LeadSource::getTotalLeads).sum();

            if (totalLeads > 0 && uniqueVisitors > 0) {
                double opportunityToLead = (totalLeads / uniqueVisitors) * 100;
                sumOpportunityToLeadMap.put(clientType,
                    sumOpportunityToLeadMap.get(clientType) + opportunityToLead);
                validOpportunityToLeadCountMap.put(clientType, validOpportunityToLeadCountMap.get(clientType) + 1);
            }
        }
    }

    private void populateLeadSources(KpiReport report,
                                     Map<ClientType, Integer> totalLeadSourcesMap,
                                     ClientType clientType) {
        if (report.getGoHighLevelReport() != null) {
            List<LeadSource> leadSources = report.getGoHighLevelReport().getLeadSources();
            for (LeadSource leadSource : leadSources) {
                if (leadSource.getTotalLeads() > 0) {
                    totalLeadSourcesMap.put(clientType,
                        totalLeadSourcesMap.get(clientType) + leadSource.getTotalLeads());
                }
            }
        }
    }

    private void populateUniqueSiteVisitors(KpiReport report,
                                            Map<ClientType, Double> totalUniqueSiteVisitorsMap,
                                            ClientType clientType) {
        if (report.getGoogleAnalyticsMetric() != null && report.getGoogleAnalyticsMetric().getUniqueSiteVisitors() > 0) {
            totalUniqueSiteVisitorsMap.put(clientType,
                totalUniqueSiteVisitorsMap.get(clientType) + report.getGoogleAnalyticsMetric().getUniqueSiteVisitors());
        }
    }

    private void populateFollowUpConversionMaps(
            KpiReport report,
            Map<ClientType, Integer> totalFollowUpsMap,
            Map<ClientType, Integer> totalConversionsMap,
            Map<ClientType, Integer> liveChatMap,
            Map<ClientType, Integer> smsMap,
            Map<ClientType, Integer> emailMap,
            Map<ClientType, Integer> callMap,
            ClientType clientType
    ) {
        if (report.getGoHighLevelReport() != null) {
            List<FollowUpConversion> followUpConversions = report.getGoHighLevelReport().getFollowUpConversions();

            for (FollowUpConversion conversion : followUpConversions) {
                totalFollowUpsMap.put(clientType, totalFollowUpsMap.get(clientType) + conversion.getTotalFollowUps());
                totalConversionsMap.put(clientType, totalConversionsMap.get(clientType) + conversion.getTotalConversions());
                liveChatMap.put(clientType, liveChatMap.get(clientType) + conversion.getTotalLiveChatMessages());
                smsMap.put(clientType, smsMap.get(clientType) + conversion.getTotalSms());
                emailMap.put(clientType, emailMap.get(clientType) + conversion.getTotalEmails());
                callMap.put(clientType, callMap.get(clientType) + conversion.getTotalCalls());
            }
        }
    }

    private int getCurrentDay() {
        return ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).getDayOfMonth();
    }

    private int getCurrentMonth() {
        return ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).getMonthValue();
    }

    private int getCurrentYear() {
        return ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).getYear();
    }

    private <T, V> Map<T, V> initializeMapWithDefault(T[] keys, V defaultValue) {
        return Arrays.stream(keys)
                .collect(Collectors.toMap(key -> key, key -> defaultValue));
    }
}
