package com.blc.kpiReport.service.ghl;

import com.blc.kpiReport.models.pojo.GhlReportData;
import com.blc.kpiReport.schema.ghl.*;
import com.blc.kpiReport.service.ghl.models.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GhlDataWriterService {
    private final LeadSourceService leadSourceService;
    private final LeadContactService leadContactService;
    private final CalendarService calendarService;
    private final AppointmentService appointmentService;
    private final PipelineStageService pipelineStageService;
    private final ContactWonService contactWonService;
    private final SalesPersonConversationService salesPersonConversationService;
    private final ConversationMessageService conversationMessageService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteGhlApiData(GoHighLevelReport goHighLevelReport) {
        var id = goHighLevelReport.getId();
        leadSourceService.deleteByGoHighLevelReportId(id);
        calendarService.deleteByGoHighLevelReportId(id);
        pipelineStageService.deleteByGoHighLevelReportId(id);
        contactWonService.deleteByGoHighLevelReportId(id);
        salesPersonConversationService.deleteByGoHighLevelReportId(id);
        log.info("Successfully deleted data for GoHighLevelReport with ID: {}", id);
    }

    public GhlReportData saveGhlData(GhlReportData ghlReportData) {
        log.info("Saving GHL report data...");

        GhlReportData savedData = GhlReportData.builder()
            .leadSources(saveLeadSources(ghlReportData.getLeadSources()))
            .calendars(saveCalendars(ghlReportData.getCalendars()))
            .pipelineStages(savePipelineStages(ghlReportData.getPipelineStages()))
            .contactsWon(saveContactsWon(ghlReportData.getContactsWon()))
            .salesPersonConversations(saveSalesPersonConversation(ghlReportData.getSalesPersonConversations()))
            .build();

        log.info("GHL report data saved successfully");
        return savedData;
    }

    private List<LeadSource> saveLeadSources(List<LeadSource> leadSources) {
        log.info("Saving {} lead sources", leadSources.size());

        List<LeadSource> savedLeadSources = leadSourceService.saveAll(leadSources);

        savedLeadSources.forEach(leadSource -> {
            if (leadSource.getLeadContacts() != null && !leadSource.getLeadContacts().isEmpty()) {
                leadSource.getLeadContacts().forEach(leadContact -> leadContact.setLeadSource(leadSource));
                leadContactService.saveAll(leadSource.getLeadContacts());
            }
        });
        return savedLeadSources;
    }

    private List<Calendar> saveCalendars(List<Calendar> calendars) {
        log.info("Saving {} calendars", calendars.size());

        List<Calendar> savedCalendars = calendarService.saveAll(calendars);

        savedCalendars.forEach(calendar -> {
            if (calendar.getAppointments() != null && !calendar.getAppointments().isEmpty()) {
                calendar.getAppointments().forEach(appointment -> appointment.setCalendar(calendar));
                appointmentService.saveAll(calendar.getAppointments());
            }
        });
        return calendarService.saveAll(calendars);
    }

    private List<PipelineStage> savePipelineStages(List<PipelineStage> pipelineStages) {
        log.info("Saving {} pipeline stages", pipelineStages.size());
        return pipelineStageService.saveAll(pipelineStages);
    }

    private List<ContactWon> saveContactsWon(List<ContactWon> contactsWon) {
        log.info("Saving {} contacts won", contactsWon.size());
        return contactWonService.saveAll(contactsWon);
    }

    private List<SalesPersonConversation> saveSalesPersonConversation(List<SalesPersonConversation> salesPersonConversations) {
        log.info("Saving {} sales person conversations", salesPersonConversations.size());
        int batchSize = 500;
        List<SalesPersonConversation> allSavedConversations = new ArrayList<>();

        for (int i = 0; i < salesPersonConversations.size(); i += batchSize) {
            int end = Math.min(i + batchSize, salesPersonConversations.size());
            List<SalesPersonConversation> batch = salesPersonConversations.subList(i, end);
            log.info("Saving batch from index {} to {}", i, end - 1);

            List<SalesPersonConversation> savedBatch = salesPersonConversationService.saveAll(batch);
            allSavedConversations.addAll(savedBatch);

            List<ConversationMessage> allMessagesInBatch = new ArrayList<>();
            savedBatch.forEach(salesPersonConversation -> {
                if (salesPersonConversation.getConversationMessages() != null && !salesPersonConversation.getConversationMessages().isEmpty()) {
                    salesPersonConversation.getConversationMessages().forEach(conversationMessage -> conversationMessage.setSalesPersonConversation(salesPersonConversation));
                    allMessagesInBatch.addAll(salesPersonConversation.getConversationMessages());
                }
            });

            if (!allMessagesInBatch.isEmpty()) {
                log.info("Saving {} messages for batch from index {} to {}", allMessagesInBatch.size(), i, end - 1);
                conversationMessageService.saveAll(allMessagesInBatch);
            }

            log.info("Batch from index {} to {} saved successfully", i, end - 1);
        }
        return allSavedConversations;
    }
}