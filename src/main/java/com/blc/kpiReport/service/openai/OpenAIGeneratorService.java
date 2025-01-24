package com.blc.kpiReport.service.openai;

import com.blc.kpiReport.config.OpenAIProperties;
import com.blc.kpiReport.models.ClientType;
import com.blc.kpiReport.models.pojo.openai.*;
import com.blc.kpiReport.models.request.GenerateKpiReportByLocationRequest;
import com.blc.kpiReport.models.response.GenerateKpiReportResponse;
import com.blc.kpiReport.models.response.KpiReportResponse;
import com.blc.kpiReport.models.response.MonthlyAverageResponse;
import com.blc.kpiReport.models.response.ghl.ContactScheduledAppointmentResponse;
import com.blc.kpiReport.models.response.ghl.FollowUpConversionResponse;
import com.blc.kpiReport.models.response.mc.MonthlyClarityReportResponse;
import com.blc.kpiReport.repository.openai.ExecutiveSummaryRepository;
import com.blc.kpiReport.schema.openai.ExecutiveSummary;
import com.blc.kpiReport.service.GhlLocationService;
import com.blc.kpiReport.service.KpiReportGeneratorService;
import com.blc.kpiReport.service.KpiReportRetrievalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIGeneratorService {

    private final ExecutiveSummaryRepository executiveSummaryRepository;
    private final OpenAIProperties openAIProperties;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final GhlLocationService ghlLocationService;
    private final KpiReportGeneratorService kpiReportGeneratorService;
    private final KpiReportRetrievalService kpiReportRetrievalService;
    private final ResourceLoader resourceLoader;

    public String generateExecutiveSummary(GenerateKpiReportByLocationRequest request) {
        var monthlyPrompt = generateJsonPrompt(request);
        try {
            var executiveSummary = generateExecutiveSummary(monthlyPrompt);
            saveExecutiveSummary(executiveSummary, request);
            return executiveSummary;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    private void saveExecutiveSummary(String summary, GenerateKpiReportByLocationRequest request) {
        if (summary == null || summary.isBlank()) {
            throw new IllegalArgumentException("Executive summary cannot be null or blank.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null.");
        }

        var ghlLocation = ghlLocationService.findByLocationId(request.getGhlLocationId());
        if (ghlLocation == null) {
            throw new IllegalStateException("GHL Location not found for ID: " + request.getGhlLocationId());
        }

        var kpiReport = kpiReportGeneratorService.getOrCreateKpiReport(ghlLocation, request.getMonth(), request.getYear());
        if (kpiReport == null) {
            throw new IllegalStateException("Failed to fetch or create KPI Report.");
        }

        var existingExecutiveSummary = executiveSummaryRepository.findByKpiReportId(kpiReport.getId());
        ExecutiveSummary executiveSummary = null;
        if (existingExecutiveSummary.isPresent()) {
            executiveSummary = existingExecutiveSummary.get();
            executiveSummary.setSummary(summary);
        } else {
            executiveSummary = ExecutiveSummary.builder()
                    .kpiReport(kpiReport)
                    .summary(summary)
                    .build();
        }

        try {
            executiveSummaryRepository.save(executiveSummary);
            log.info("Successfully saved executive summary for KPI Report ID: {}", kpiReport.getId());
        } catch (Exception e) {
            log.error("Failed to save executive summary for KPI Report ID: {}", kpiReport.getId(), e);
            throw new RuntimeException("Unable to save executive summary. Please try again later.", e);
        }
    }


    private MonthlyPrompt generateJsonPrompt(GenerateKpiReportByLocationRequest request) {
        var kpiReports = IntStream.range(0, 6)
                .mapToObj(offset -> {
                    var yearMonth = YearMonth.of(request.getYear(), request.getMonth()).minusMonths(offset);
                    return kpiReportRetrievalService.getKpiReport(request.getGhlLocationId(), yearMonth.getMonthValue(), yearMonth.getYear());
                })
                .toList();
        var monthlyAverage = kpiReportRetrievalService.getMonthlyAverage(request.getMonth(), request.getYear(), ClientType.valueOf(kpiReports.stream().findFirst().get().getClientType()));
        return generateMonthlyPrompt(kpiReports, monthlyAverage);
    }

    private MonthlyPrompt generateMonthlyPrompt(List<KpiReportResponse> kpiReports, MonthlyAverageResponse monthlyAverage) {
        var kpiReport =  kpiReports.stream().findFirst().get();
        return MonthlyPrompt.builder()
                .subAgency(kpiReport.getSubAgency())
                .ghlLocationId(kpiReport.getGhlLocationId())
                .country(kpiReport.getCountry())
                .monthAndYear(kpiReport.getMonthAndYear())
                .clientType(kpiReport.getClientType())
                .googleAnalytics(generateGoogleAnalyticsPrompt(kpiReports, monthlyAverage))
                .websiteAnalytics(generateWebsiteAnalyticsPrompt(kpiReport))
                .goHighLevel(generateGoHighLevelPrompt(kpiReports, monthlyAverage))
                .contactsWon(kpiReport.getContactsWon())
                .build();
    }

    private GoHighLevel generateGoHighLevelPrompt(List<KpiReportResponse> kpiReports, MonthlyAverageResponse monthlyAverage) {
        var kpiReport = kpiReports.stream().findFirst().orElse(null);
        if (kpiReport == null) {
            return GoHighLevel.builder().build();
        }

        NewLeadAppointment newLeadAppointment = buildNewLeadAppointment(kpiReport);
        List<TopLeadSource> topLeadSources = buildTopLeadSources(kpiReport);
        List<TopSessionChannel> topSessionChannels = buildTopSessionChannels(kpiReport);
        List<SalesPersonBreakdown> salesPersonBreakdown = buildSalesPersonBreakdown(kpiReport);
        PipelineStageConversions pipelineStageConversions = buildPipelineStageConversions(kpiReport, salesPersonBreakdown);
        List<PreviousMonthLeads> previousMonthLeads = buildPreviousMonthLeads(kpiReports);

        return GoHighLevel.builder()
                .totalLeadsCaptured(kpiReport.getWebsiteLead().getTotalLeads())
                .industryAverageTotalLeadsCaptured(monthlyAverage.getAverageTotalLeads())
                .previousMonthTotalLeadsCaptured(previousMonthLeads)
                .totalWebsiteLeadsCaptured(kpiReport.getWebsiteLead().getTotalWebsiteLeads())
                .totalManualUserInputLeadsCaptured(kpiReport.getWebsiteLead().getTotalManualLeads())
                .topLeadSources(topLeadSources)
                .topSessionChannels(topSessionChannels)
                .newLeadAppointment(newLeadAppointment)
                .pipelineStageConversions(pipelineStageConversions)
                .build();
    }

    private NewLeadAppointment buildNewLeadAppointment(KpiReportResponse kpiReport) {
        List<ContactScheduledAppointmentResponse> appointments = kpiReport.getContactScheduledAppointments();
        int scheduledCallCount = appointments != null
                ? (int) appointments.stream()
                .filter(ContactScheduledAppointmentResponse::isScheduledACall)
                .count()
                : 0;

        int noScheduledCallCount = appointments != null
                ? (int) appointments.stream()
                .filter(appointment -> !appointment.isScheduledACall())
                .count()
                : 0;

        return NewLeadAppointment.builder()
                .scheduledACallCount(scheduledCallCount)
                .noScheduledCallsCount(noScheduledCallCount)
                .build();
    }

    private List<TopLeadSource> buildTopLeadSources(KpiReportResponse kpiReport) {
        return kpiReport.getWebsiteLead().getLeadSource().stream()
                .map(source -> TopLeadSource.builder()
                        .leadSource(source.getSource())
                        .count(source.getTotalLeads())
                        .build())
                .toList();
    }

    private List<TopSessionChannel> buildTopSessionChannels(KpiReportResponse kpiReport) {
        Map<String, Integer> sessionChannelCounts = kpiReport.getWebsiteLead().getLeadSource().stream()
                .flatMap(source -> source.getLeadContacts().stream()
                        .flatMap(contact -> Arrays.stream(contact.getAttributionSource().split(",\\s*"))))
                .collect(Collectors.toMap(
                        attribution -> attribution,
                        value -> 1,
                        Integer::sum
                ));

        return sessionChannelCounts.entrySet().stream()
                .map(entry -> TopSessionChannel.builder()
                        .sessionChannel(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .toList();
    }

    private List<SalesPersonBreakdown> buildSalesPersonBreakdown(KpiReportResponse kpiReport) {
        return kpiReport.getFollowUpConversions() != null
                ? kpiReport.getFollowUpConversions().stream()
                .map(conversion -> SalesPersonBreakdown.builder()
                        .salesPersonName(conversion.getGhlUserName())
                        .stageConversions(conversion.getTotalConversions())
                        .followUps(conversion.getTotalFollowUps())
                        .build())
                .toList()
                : List.of();
    }

    private PipelineStageConversions buildPipelineStageConversions(KpiReportResponse kpiReport, List<SalesPersonBreakdown> salesPersonBreakdown) {
        int totalStageConversions = kpiReport.getFollowUpConversions() != null
                ? kpiReport.getFollowUpConversions().stream()
                .mapToInt(FollowUpConversionResponse::getTotalConversions)
                .sum()
                : 0;

        int totalFollowups = kpiReport.getFollowUpConversions() != null
                ? kpiReport.getFollowUpConversions().stream()
                .mapToInt(FollowUpConversionResponse::getTotalFollowUps)
                .sum()
                : 0;

        return PipelineStageConversions.builder()
                .totalStageConversions(totalStageConversions)
                .totalFollowups(totalFollowups)
                .salesPersonBreakdown(salesPersonBreakdown)
                .build();
    }

    private List<PreviousMonthLeads> buildPreviousMonthLeads(List<KpiReportResponse> kpiReports) {
        return kpiReports.stream()
                .map(report -> PreviousMonthLeads.builder()
                        .monthAndYear(report.getMonthAndYear())
                        .totalLeadsCaptured(report.getWebsiteLead().getTotalLeads())
                        .build())
                .toList();
    }

    private GoogleAnalytics generateGoogleAnalyticsPrompt(List<KpiReportResponse> kpiReports, MonthlyAverageResponse monthlyAverage) {
        var currentMonthReport = kpiReports.stream().findFirst().get();

        var previousMonthsTotalUniqueSiteVisitors = kpiReports.stream()
                .skip(1)
                .map(report -> PreviousMonthVisitors.builder()
                        .monthAndYear(report.getMonthAndYear())
                        .totalUniqueSiteVisitors(report.getUniqueSiteVisitors())
                        .build())
                .toList();

        return GoogleAnalytics.builder()
                .totalUniqueSiteVisitors(currentMonthReport.getUniqueSiteVisitors())
                .industryAverageUniqueSiteVisitors(monthlyAverage.getAverageUniqueSiteVisitors())
                .previousMonthsTotalUniqueSiteVisitors(previousMonthsTotalUniqueSiteVisitors)
                .topCities(currentMonthReport.getCityAnalytics().stream()
                        .sorted((city1, city2) -> Integer.compare(city2.getUniqueSiteVisitors(), city1.getUniqueSiteVisitors()))
                        .limit(5)
                        .map(cityAnalytics -> TopCity.builder()
                                .city(cityAnalytics.getCity())
                                .uniqueSiteVisitors(cityAnalytics.getUniqueSiteVisitors())
                                .build())
                        .toList())
                .build();
    }

    private WebsiteAnalytics generateWebsiteAnalyticsPrompt(KpiReportResponse kpiReport) {
        MonthlyClarityReportResponse monthlyClarityReport = kpiReport.getMonthlyClarityReport();

        if (monthlyClarityReport == null) {
            return WebsiteAnalytics.builder()
                    .deviceClarityAggregate(null)
                    .topTenUrls(null)
                    .build();
        }

        List<DeviceClarityAggregate> deviceClarityAggregates = monthlyClarityReport.getDeviceClarityAggregate().stream()
                .map(device -> DeviceClarityAggregate.builder()
                        .deviceName(device.getDeviceName())
                        .collectiveAverageScrollDepth(device.getCollectiveAverageScrollDepth() != null ? device.getCollectiveAverageScrollDepth() : 0)
                        .totalSessions(device.getTotalSessions() != null ? device.getTotalSessions() : 0)
                        .totalActiveTime(device.getTotalActiveTime() != null ? device.getTotalActiveTime() : 0)
                        .build())
                .toList();

        List<TopTenUrl> topTenUrls = monthlyClarityReport.getUrls().stream()
                .sorted((url1, url2) -> Integer.compare(
                        url2.getDevices().stream().mapToInt(device -> device.getTotalSessionCount() != null ? device.getTotalSessionCount() : 0).sum(),
                        url1.getDevices().stream().mapToInt(device -> device.getTotalSessionCount() != null ? device.getTotalSessionCount() : 0).sum()))
                .limit(10)
                .map(url -> TopTenUrl.builder()
                        .url(url.getUrl())
                        .devices(url.getDevices().stream()
                                .map(device -> Device.builder()
                                        .deviceType(device.getDeviceType())
                                        .averageScrollDepth(device.getAverageScrollDepth() != null ? device.getAverageScrollDepth() : 0)
                                        .totalTime(device.getTotalTime() != null ? device.getTotalTime() : 0)
                                        .activeTime(device.getActiveTime() != null ? device.getActiveTime() : 0)
                                        .totalSessionCount(device.getTotalSessionCount() != null ? device.getTotalSessionCount() : 0)
                                        .build())
                                .toList())
                        .averageScrollDepth(url.getDevices().stream().mapToDouble(device -> device.getAverageScrollDepth() != null ? device.getAverageScrollDepth() : 0).average().orElse(0))
                        .activeTime(url.getDevices().stream().mapToInt(device -> device.getActiveTime() != null ? device.getActiveTime() : 0).sum())
                        .totalSessionCount(url.getDevices().stream().mapToInt(device -> device.getTotalSessionCount() != null ? device.getTotalSessionCount() : 0).sum())
                        .build())
                .toList();

        return WebsiteAnalytics.builder()
                .deviceClarityAggregate(deviceClarityAggregates)
                .topTenUrls(topTenUrls)
                .build();
    }

    public String generateExecutiveSummary(MonthlyPrompt monthlyPrompt) throws IOException {
        log.info("Preparing request for OpenAI API...");

        String templatePath = "classpath:openai/executive-summary-prompt.txt";
        String promptTemplate = readPromptFromFile(templatePath);

        String data = String.format(promptTemplate, objectMapper.writeValueAsString(monthlyPrompt));

        String url = openAIProperties.getBaseUrl() + "/chat/completions";
        log.debug("API URL: {}", url);

        Map<String, Object> requestBody = Map.of(
                "model", openAIProperties.getModel(),
                "messages", List.of(Map.of("role", "user", "content", data)),
                "max_tokens", openAIProperties.getMaxTokens()
        );

        String jsonBody = objectMapper.writeValueAsString(requestBody);
        log.debug("Request payload: {}", jsonBody);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonBody, MediaType.get("application/json")))
                .addHeader("Authorization", "Bearer " + openAIProperties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("API call failed with status: {} and body: {}", response.code(), response.body().string());
                throw new IOException("Unexpected response code: " + response.code());
            }

            String responseBody = response.body().string();
            log.info("API call successful. Response received.");

            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");

            return content;
        } catch (IOException e) {
            log.error("Error during API call: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String readPromptFromFile(String filePath) throws IOException {
        Resource resource = resourceLoader.getResource(filePath);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

}
