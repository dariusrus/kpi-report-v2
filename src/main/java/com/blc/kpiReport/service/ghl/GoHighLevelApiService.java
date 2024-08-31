package com.blc.kpiReport.service.ghl;

import com.blc.kpiReport.exception.GhlApiException;
import com.blc.kpiReport.models.pojo.GhlApiData;
import com.blc.kpiReport.models.pojo.GhlReportData;
import com.blc.kpiReport.repository.GoHighLevelReportRepository;
import com.blc.kpiReport.schema.GhlLocation;
import com.blc.kpiReport.schema.ghl.GoHighLevelReport;
import com.blc.kpiReport.schema.KpiReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoHighLevelApiService {
    private final GhlDataFetchService fetchService;
    private final GhlDataProcessorService processorService;
    private final GhlDataWriterService writerService;
    private final GoHighLevelReportRepository repository;

    public GoHighLevelReport fetchGhlReporting(String startDate, String endDate, GhlLocation location, KpiReport kpiReport) throws GhlApiException {
        var goHighLevelReport = getOrCreateGoHighLevelReport(kpiReport);

        // Read all data from GoHighLevel API
        GhlApiData ghlApiData = fetchService.getGhlData(location, startDate, endDate);

        // Process from raw data to report data
        GhlReportData ghlReportData = processorService.processGhlData(ghlApiData, goHighLevelReport);

        // Persist monthly report data to DB
        ghlReportData = writerService.saveGhlData(ghlReportData);

        goHighLevelReport.setLeadSources(ghlReportData.getLeadSources());
        goHighLevelReport.setCalendars(ghlReportData.getCalendars());
        goHighLevelReport.setPipelineStages(ghlReportData.getPipelineStages());
        goHighLevelReport.setContactsWon(ghlReportData.getContactsWon());

        return repository.save(goHighLevelReport);
    }

    private GoHighLevelReport getOrCreateGoHighLevelReport(KpiReport kpiReport) {
        var existingReport = repository.findByKpiReport_Id(kpiReport.getId());

        if (existingReport.isPresent()) {
            var ghlReport = existingReport.get();
            writerService.deleteGhlApiData(ghlReport);
            return ghlReport;
        } else {
            return repository.save(GoHighLevelReport.builder()
                .kpiReport(kpiReport)
                .build());
        }
    }
}
