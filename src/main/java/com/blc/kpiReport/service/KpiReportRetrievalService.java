package com.blc.kpiReport.service;

import com.blc.kpiReport.models.ClientType;
import com.blc.kpiReport.models.mapper.MonthlyAverageMapper;
import com.blc.kpiReport.models.mapper.ga.CityAnalyticsMapper;
import com.blc.kpiReport.models.mapper.ghl.*;
import com.blc.kpiReport.models.mapper.mc.DailyMetricMapper;
import com.blc.kpiReport.models.mapper.mc.MonthlyClarityReportMapper;
import com.blc.kpiReport.models.response.KpiReportResponse;
import com.blc.kpiReport.models.response.MonthlyAverageResponse;
import com.blc.kpiReport.models.response.ga.CityAnalyticsResponse;
import com.blc.kpiReport.models.response.ghl.*;
import com.blc.kpiReport.models.response.mc.DailyMetricResponse;
import com.blc.kpiReport.models.response.mc.MonthlyClarityReportResponse;
import com.blc.kpiReport.repository.KpiReportRepository;
import com.blc.kpiReport.repository.ghl.MonthlyAverageRepository;
import com.blc.kpiReport.schema.GhlLocation;
import com.blc.kpiReport.schema.MonthlyAverage;
import com.blc.kpiReport.schema.ghl.GhlUser;
import com.blc.kpiReport.schema.ghl.LeadSource;
import com.blc.kpiReport.schema.ghl.SalesPersonConversation;
import com.blc.kpiReport.schema.mc.DeviceMetric;
import com.blc.kpiReport.schema.mc.MonthlyClarityReport;
import com.blc.kpiReport.schema.mc.UrlMetric;
import com.blc.kpiReport.service.ghl.models.GhlUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.blc.kpiReport.util.DateUtil.formatMonthAndYear;
import static com.blc.kpiReport.util.NumberUtil.roundToTwoDecimalPlaces;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableAsync
public class KpiReportRetrievalService {

    private final GhlLocationService ghlLocationService;
    private final GhlUserService ghlUserService;
    private final KpiReportRepository repository;
    private final CalendarMapper calendarMapper;
    private final PipelineStageMapper pipelineStageMapper;
    private final ContactWonMapper contactWonMapper;
    private final LeadSourceMapper leadSourceMapper;
    private final LeadContactMapper leadContactMapper;
    private final SalesPersonConversationMapper salesPersonConversationMapper;
    private final DailyMetricMapper dailyMetricMapper;
    private final CityAnalyticsMapper cityAnalyticsMapper;
    private final MonthlyClarityReportMapper monthlyClarityReportMapper;
    private final MonthlyAverageRepository monthlyAverageRepository;
    private final MonthlyAverageMapper monthlyAverageMapper;
    private final GhlUserMapper ghlUserMapper;
    private final FollowUpConversionMapper followUpConversionMapper;

    public KpiReportResponse getKpiReport(String ghlLocationId, int month, int year) {
        var ghlLocation = ghlLocationService.findByLocationId(ghlLocationId);
        var ghlUsers = ghlUserService.findByGhlLocationId(ghlLocation.getId());
        try {
            if (!ObjectUtils.isEmpty(ghlLocation)) {
                var id = ghlLocation.getId();
                var kpiReportOptional = repository.findByMonthAndYearAndGhlLocation_Id(month, year, id);
                if (kpiReportOptional.isPresent()) {
                    var kpiReport = kpiReportOptional.get();
                    var googleAnalyticsMetric = kpiReport.getGoogleAnalyticsMetric();
                    var goHighLevelReport = kpiReport.getGoHighLevelReport();

                    MonthlyClarityReportResponse monthlyClarityResponse = null;
                    if (ObjectUtils.isNotEmpty(kpiReport.getMonthlyClarityReport())) {
                        // TODO: Work on temporary code
                        monthlyClarityResponse = monthlyClarityReportMapper.toResponse(filterTop100ActiveUrls(kpiReport.getMonthlyClarityReport()));
                        monthlyClarityResponse.setDeviceClarityAggregate(monthlyClarityReportMapper.aggregateDataByDeviceType(kpiReport.getMonthlyClarityReport()));
                    }

                    return buildReportResponse(ghlLocation,
                        formatMonthAndYear(month, year),
                        googleAnalyticsMetric.getUniqueSiteVisitors(),
                        cityAnalyticsMapper.toResponseList(googleAnalyticsMetric.getCityAnalytics()),
                        websiteLeadResponse(goHighLevelReport.getLeadSources()),
                        calendarMapper.toResponseList(goHighLevelReport.getCalendars()),
                        pipelineStageMapper.toResponseList(goHighLevelReport.getPipelineStages()),
                        contactWonMapper.toResponseList(goHighLevelReport.getContactsWon()),
                        salesPersonConversationMapper.toResponseList(goHighLevelReport.getSalesPersonConversations()),
                        ghlUserMapper.toResponseList(ghlUsers),
                        followUpConversionMapper.toResponseList(goHighLevelReport.getFollowUpConversions()),
                        monthlyClarityResponse);
                }
            }
        } catch (NullPointerException e) {
            log.warn("Report for {} for {}, {} is incomplete. Please regenerate this report.",
                ghlLocation.getName(), month, year);
            return null;
        }
        return null;
    }

    public MonthlyClarityReport filterTop100ActiveUrls(MonthlyClarityReport report) {
        List<UrlMetric> top100Urls = report.getUrls().stream()
                .sorted((url1, url2) -> {
                    int totalActiveTime1 = url1.getDevices().stream()
                            .mapToInt(deviceMetric -> deviceMetric.getActiveTime() != null ? deviceMetric.getActiveTime() : 0)
                            .sum();

                    int totalActiveTime2 = url2.getDevices().stream()
                            .mapToInt(deviceMetric -> deviceMetric.getActiveTime() != null ? deviceMetric.getActiveTime() : 0)
                            .sum();

                    return Integer.compare(totalActiveTime2, totalActiveTime1);
                })
                .limit(100)
                .collect(Collectors.toList());

        report.setUrls(top100Urls);
        return report;
    }

    private KpiReportResponse buildReportResponse(GhlLocation location,
                                                  String monthAndYear,
                                                  Integer uniqueSiteVisitors,
                                                  List<CityAnalyticsResponse> cityAnalytics,
                                                  WebsiteLeadResponse websiteLead,
                                                  List<CalendarResponse> calendars,
                                                  List<PipelineResponse> pipelines,
                                                  List<ContactsWonResponse> contactsWon,
                                                  List<SalesPersonConversationResponse> salesPersonConversation,
                                                  List<GhlUserResponse> ghlUsers,
                                                  List<FollowUpConversionResponse> followUpConversions,
                                                  MonthlyClarityReportResponse monthlyClarityReport) {
        double opportunityToLead = (uniqueSiteVisitors == 0 || websiteLead.getTotalLeads() == 0)
            ? 0
            : roundToTwoDecimalPlaces(((double) websiteLead.getTotalLeads() / uniqueSiteVisitors) * 100);
        var country = "US".equals(location.getGaCountryCode()) ? "United States" :
            ("CA".equals(location.getGaCountryCode()) ? "Canada" : "United States");
        return KpiReportResponse.builder()
            .subAgency(location.getName())
            .ghlLocationId(location.getLocationId())
            .country(country)
            .monthAndYear(monthAndYear)
            .clientType(location.getClientType().toString())
            .uniqueSiteVisitors(uniqueSiteVisitors)
            .cityAnalytics(cityAnalytics)
            .opportunityToLead(opportunityToLead)
            .websiteLead(websiteLead)
            .calendars(calendars)
            .pipelines(pipelines)
            .contactsWon(contactsWon)
            .salesPersonConversations(salesPersonConversation)
            .monthlyClarityReport(monthlyClarityReport)
            .ghlUsers(ghlUsers)
            .followUpConversions(followUpConversions)
            .build();
    }

    private WebsiteLeadResponse websiteLeadResponse(List<LeadSource> leadSources) {
        int totalLeads = 0;
        int totalWebsiteLeads = 0;
        int totalManualLeads = 0;
        double totalValues = 0.0;
        double totalWebsiteValuation = 0.0;
        double totalManualValuation = 0.0;
        int totalOpen = 0;
        int totalWon = 0;
        int totalLost = 0;
        int totalAbandoned = 0;

        for (LeadSource leadSource : leadSources) {
            int leads = leadSource.getTotalLeads();
            double values = leadSource.getTotalValues();

            totalLeads += leads;
            totalValues += values;
            totalOpen += leadSource.getOpen();
            totalWon += leadSource.getWon();
            totalLost += leadSource.getLost();
            totalAbandoned += leadSource.getAbandoned();

            if ("Website Lead".equals(leadSource.getLeadType())) {
                totalWebsiteLeads += leads;
                totalWebsiteValuation += values;
            } else {
                totalManualLeads += leads;
                totalManualValuation += values;
            }
        }

        return WebsiteLeadResponse.builder()
            .totalLeads(totalLeads)
            .totalWebsiteLeads(totalWebsiteLeads)
            .totalManualLeads(totalManualLeads)
            .totalValues(totalValues)
            .totalWebsiteValuation(totalWebsiteValuation)
            .totalManualValuation(totalManualValuation)
            .totalOpen(totalOpen)
            .totalWon(totalWon)
            .totalLost(totalLost)
            .totalAbandoned(totalAbandoned)
            .leadSource(leadSources.stream()
                .map(leadSource -> {
                    var leadSourceResponse = leadSourceMapper.toResponse(leadSource);
                    leadSourceResponse.setLeadContacts(leadContactMapper.toResponseList(leadSource.getLeadContacts()));
                    return leadSourceResponse;
                })
                .collect(Collectors.toList()))
            .build();
    }

    public DailyMetricResponse getDailyMetric(String ghlLocationId, int day, int month, int year) {
        var ghlLocation = ghlLocationService.findByLocationId(ghlLocationId);
        if (ghlLocation != null) {
            var id = ghlLocation.getId();
            var kpiReportOptional = repository.findByMonthAndYearAndGhlLocation_Id(month, year, id);
            if (kpiReportOptional.isPresent()) {
                var kpiReport = kpiReportOptional.get();
                var dailyMetrics = kpiReport.getDailyMetrics();

                // Find the DailyMetric that matches the day
                var dailyMetric = dailyMetrics.stream()
                    .filter(dm -> dm.getDay() == day)
                    .findFirst()
                    .orElse(null);

                if (dailyMetric != null) {
                    // Map DailyMetric to DailyMetricResponse
                    return dailyMetricMapper.toResponse(dailyMetric, ghlLocation);
                }
            }
        }
        return null;
    }

    public MonthlyAverageResponse getMonthlyAverage(int month, int year, ClientType clientType) {
        Optional<MonthlyAverage> monthlyAverageOptional = monthlyAverageRepository.findByMonthAndYearAndClientType(month, year, clientType);
        if (monthlyAverageOptional.isPresent()) {
            return monthlyAverageMapper.toResponse(monthlyAverageOptional.get());
        } else {
            return null;
        }
    }
}