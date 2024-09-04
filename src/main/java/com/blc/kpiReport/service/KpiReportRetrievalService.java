package com.blc.kpiReport.service;

import com.blc.kpiReport.models.ClientType;
import com.blc.kpiReport.models.mapper.MonthlyAverageMapper;
import com.blc.kpiReport.models.mapper.ghl.*;
import com.blc.kpiReport.models.mapper.mc.DailyMetricMapper;
import com.blc.kpiReport.models.mapper.mc.MonthlyClarityReportMapper;
import com.blc.kpiReport.models.response.KpiReportResponse;
import com.blc.kpiReport.models.response.MonthlyAverageResponse;
import com.blc.kpiReport.models.response.ghl.*;
import com.blc.kpiReport.models.response.mc.DailyMetricResponse;
import com.blc.kpiReport.models.response.mc.MonthlyClarityReportResponse;
import com.blc.kpiReport.repository.KpiReportRepository;
import com.blc.kpiReport.repository.MonthlyAverageRepository;
import com.blc.kpiReport.schema.GhlLocation;
import com.blc.kpiReport.schema.MonthlyAverage;
import com.blc.kpiReport.schema.ghl.LeadSource;
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
    private final KpiReportRepository repository;
    private final CalendarMapper calendarMapper;
    private final PipelineStageMapper pipelineStageMapper;
    private final ContactWonMapper contactWonMapper;
    private final LeadSourceMapper leadSourceMapper;
    private final LeadContactMapper leadContactMapper;
    private final DailyMetricMapper dailyMetricMapper;
    private final MonthlyClarityReportMapper monthlyClarityReportMapper;
    private final MonthlyAverageRepository monthlyAverageRepository;
    private final MonthlyAverageMapper monthlyAverageMapper;

    public KpiReportResponse getKpiReport(String ghlLocationId, int month, int year) {
        var ghlLocation = ghlLocationService.findByLocationId(ghlLocationId);
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
                        monthlyClarityResponse = monthlyClarityReportMapper.toResponse(kpiReport.getMonthlyClarityReport());
                        monthlyClarityResponse.setDeviceClarityAggregate(monthlyClarityReportMapper.aggregateDataByDeviceType(kpiReport.getMonthlyClarityReport()));
                    }

                    return buildReportResponse(ghlLocation,
                        formatMonthAndYear(month, year),
                        googleAnalyticsMetric.getUniqueSiteVisitors(),
                        websiteLeadResponse(goHighLevelReport.getLeadSources()),
                        calendarMapper.toResponseList(goHighLevelReport.getCalendars()),
                        pipelineStageMapper.toResponseList(goHighLevelReport.getPipelineStages()),
                        contactWonMapper.toResponseList(goHighLevelReport.getContactsWon()),
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

    private KpiReportResponse buildReportResponse(GhlLocation location,
                                                  String monthAndYear,
                                                  Integer uniqueSiteVisitors,
                                                  WebsiteLeadResponse websiteLead,
                                                  List<CalendarResponse> calendars,
                                                  List<PipelineResponse> pipelines,
                                                  List<ContactsWonResponse> contactsWon,
                                                  MonthlyClarityReportResponse monthlyClarityReport) {
        return KpiReportResponse.builder()
            .subAgency(location.getName())
            .ghlLocationId(location.getLocationId())
            .monthAndYear(monthAndYear)
            .clientType(location.getClientType().toString())
            .uniqueSiteVisitors(uniqueSiteVisitors)
            .opportunityToLead(roundToTwoDecimalPlaces(((double) websiteLead.getTotalLeads() / uniqueSiteVisitors) * 100))
            .websiteLead(websiteLead)
            .calendars(calendars)
            .pipelines(pipelines)
            .contactsWon(contactsWon)
            .monthlyClarityReport(monthlyClarityReport)
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