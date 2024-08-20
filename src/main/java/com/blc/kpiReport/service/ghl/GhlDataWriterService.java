package com.blc.kpiReport.service.ghl;

import com.blc.kpiReport.models.pojo.GhlReportData;
import com.blc.kpiReport.schema.ghl.*;
import com.blc.kpiReport.service.ghl.models.AppointmentService;
import com.blc.kpiReport.service.ghl.models.ContactWonService;
import com.blc.kpiReport.service.ghl.models.LeadSourceService;
import com.blc.kpiReport.service.ghl.models.PipelineStageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GhlDataWriterService {
    private final LeadSourceService leadSourceService;
    private final AppointmentService appointmentService;
    private final PipelineStageService pipelineStageService;
    private final ContactWonService contactWonService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteGhlApiData(GoHighLevelReport goHighLevelReport) {
        var id = goHighLevelReport.getId();
        leadSourceService.deleteByGoHighLevelReportId(id);
        appointmentService.deleteByGoHighLevelReportId(id);
        pipelineStageService.deleteByGoHighLevelReportId(id);
        contactWonService.deleteByGoHighLevelReportId(id);
        log.info("Successfully deleted data for GoHighLevelReport with ID: {}", id);
    }

    public GhlReportData saveGhlData(GhlReportData ghlReportData) {
        log.info("Saving GHL report data...");

        GhlReportData savedData = GhlReportData.builder()
            .leadSources(saveLeadSources(ghlReportData.getLeadSources()))
            .appointments(saveAppointments(ghlReportData.getAppointments()))
            .pipelineStages(savePipelineStages(ghlReportData.getPipelineStages()))
            .contactsWon(saveContactsWon(ghlReportData.getContactsWon()))
            .build();

        log.info("GHL report data saved successfully");
        return savedData;
    }

    private List<LeadSource> saveLeadSources(List<LeadSource> leadSources) {
        log.debug("Saving {} lead sources", leadSources.size());
        return leadSourceService.saveAll(leadSources);
    }

    private List<Appointment> saveAppointments(List<Appointment> appointments) {
        log.debug("Saving {} appointments", appointments.size());
        return appointmentService.saveAll(appointments);
    }

    private List<PipelineStage> savePipelineStages(List<PipelineStage> pipelineStages) {
        log.debug("Saving {} pipeline stages", pipelineStages.size());
        return pipelineStageService.saveAll(pipelineStages);
    }

    private List<ContactWon> saveContactsWon(List<ContactWon> contactsWon) {
        log.debug("Saving {} contacts won", contactsWon.size());
        return contactWonService.saveAll(contactsWon);
    }
}
