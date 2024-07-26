package com.blc.kpiReport.service;

import com.blc.kpiReport.models.mapper.AppointmentMapper;
import com.blc.kpiReport.models.mapper.ContactWonMapper;
import com.blc.kpiReport.models.mapper.PipelineStageMapper;
import com.blc.kpiReport.models.mapper.LeadSourceMapper;
import com.blc.kpiReport.models.response.*;
import com.blc.kpiReport.repository.KpiReportRepository;
import com.blc.kpiReport.schema.GhlLocation;
import com.blc.kpiReport.schema.LeadSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.blc.kpiReport.util.DateUtil.formatMonthAndYear;
import static com.blc.kpiReport.util.NumberUtil.roundToTwoDecimalPlaces;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableAsync
public class KpiReportRetrievalService {

    private final GhlLocationService ghlLocationService;
    private final KpiReportRepository repository;
    private final AppointmentMapper appointmentMapper;
    private final PipelineStageMapper pipelineStageMapper;
    private final ContactWonMapper contactWonMapper;
    private final LeadSourceMapper leadSourceMapper;

    public KpiReportResponse getKpiReport(String ghlLocationId, int month, int year) {
        var ghlLocation = ghlLocationService.findByLocationId(ghlLocationId);
        if (!ObjectUtils.isEmpty(ghlLocation)) {
            var id = ghlLocation.getId();
            var kpiReportOptional = repository.findByMonthAndYearAndGhlLocation_Id(month, year, id);
            if (kpiReportOptional.isPresent()) {
                var kpiReport = kpiReportOptional.get();
                var googleAnalyticsMetric = kpiReport.getGoogleAnalyticsMetric();
                var goHighLevelReport = kpiReport.getGoHighLevelReport();
                return buildReportResponse(ghlLocation,
                    formatMonthAndYear(month, year),
                    googleAnalyticsMetric.getUniqueSiteVisitors(),
                    websiteLeadResponse(goHighLevelReport.getLeadSources()),
                    appointmentMapper.toResponseList(goHighLevelReport.getAppointments()),
                    pipelineStageMapper.toResponseList(goHighLevelReport.getPipelineStages()),
                    contactWonMapper.toResponseList(goHighLevelReport.getContactsWon()));
            }
        }
        return null;
    }

    private KpiReportResponse buildReportResponse(GhlLocation location,
                                                  String monthAndYear,
                                                  Integer uniqueSiteVisitors,
                                                  WebsiteLeadResponse websiteLead,
                                                  List<AppointmentResponse> appointments,
                                                  List<PipelineResponse> pipelines,
                                                  List<ContactsWonResponse> contactsWon) {
        return KpiReportResponse.builder()
            .subAgency(location.getName())
            .ghlLocationId(location.getLocationId())
            .monthAndYear(monthAndYear)
            .uniqueSiteVisitors(uniqueSiteVisitors)
            .opportunityToLead(roundToTwoDecimalPlaces(((double) websiteLead.getTotalLeads() / uniqueSiteVisitors) * 100))
            .websiteLead(websiteLead)
            .appointments(appointments)
            .pipelines(pipelines)
            .contactsWon(contactsWon)
            .build();
    }

    private WebsiteLeadResponse websiteLeadResponse(List<LeadSource> leadSources) {
        var totalLeads = leadSources.stream().mapToInt(LeadSource::getTotalLeads).sum();
        var totalValues = leadSources.stream().mapToDouble(LeadSource::getTotalValues).sum();
        var totalOpen = leadSources.stream().mapToInt(LeadSource::getOpen).sum();
        var totalWon = leadSources.stream().mapToInt(LeadSource::getWon).sum();
        var totalLost = leadSources.stream().mapToInt(LeadSource::getLost).sum();
        var totalAbandoned = leadSources.stream().mapToInt(LeadSource::getAbandoned).sum();

        return WebsiteLeadResponse.builder()
            .totalLeads(totalLeads)
            .totalValues(totalValues)
            .totalOpen(totalOpen)
            .totalWon(totalWon)
            .totalLost(totalLost)
            .totalAbandoned(totalAbandoned)
            .leadSource(leadSourceMapper.toResponseList(leadSources))
            .build();
    }
}
