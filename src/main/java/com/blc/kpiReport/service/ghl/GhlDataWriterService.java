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
import java.util.stream.Collectors;

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
    private final SalesPersonConversionService salesPersonConversionService;
    private final SalesPersonConversationService salesPersonConversationService;
    private final ConversationMessageService conversationMessageService;
    private final GhlContactService ghlContactService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteGhlApiData(GoHighLevelReport goHighLevelReport) {
        var id = goHighLevelReport.getId();

        List<GhlContact> contactsToDelete = new ArrayList<>();

        contactsToDelete.addAll(leadSourceService.findAllByGoHighLevelReportId(id).stream()
                .flatMap(leadSource -> leadSource.getLeadContacts().stream())
                .map(LeadContact::getGhlContact)
                .filter(contact -> contact != null)
                .collect(Collectors.toList()));

        contactsToDelete.addAll(contactWonService.findAllByGoHighLevelReportId(id).stream()
                .map(ContactWon::getGhlContact)
                .filter(contact -> contact != null)
                .collect(Collectors.toList()));

        contactsToDelete.addAll(salesPersonConversationService.findAllByGoHighLevelReportId(id).stream()
                .map(SalesPersonConversation::getGhlContact)
                .filter(contact -> contact != null)
                .collect(Collectors.toList()));

        contactsToDelete.addAll(pipelineStageService.findAllByGoHighLevelReportId(id).stream()
                .flatMap(pipelineStage -> pipelineStage.getSalesPersonConversions().stream())
                .flatMap(conversion -> conversion.getConvertedGhlContacts().stream())
                .filter(contact -> contact != null)
                .collect(Collectors.toList()));

        contactsToDelete = contactsToDelete.stream().distinct().collect(Collectors.toList());

        if (!contactsToDelete.isEmpty()) {
            log.info("Deleting {} GHL contacts associated with GoHighLevelReport ID: {}", contactsToDelete.size(), id);
            ghlContactService.deleteAll(contactsToDelete);
        }

        leadSourceService.deleteByGoHighLevelReportId(id);
        calendarService.deleteByGoHighLevelReportId(id);
        pipelineStageService.deleteByGoHighLevelReportId(id);
        contactWonService.deleteByGoHighLevelReportId(id);
        salesPersonConversationService.deleteByGoHighLevelReportId(id);
        log.info("Successfully deleted data for GoHighLevelReport with ID: {}", id);
    }

    private void saveGhlContacts(GhlReportData ghlReportData) {
        List<GhlContact> contacts = new ArrayList<>();

        ghlReportData.getContactsWon().forEach(contactWon -> {
            if (contactWon.getGhlContact() != null) {
                contacts.add(contactWon.getGhlContact());
            }
        });

        ghlReportData.getLeadSources().forEach(leadSource -> {
            leadSource.getLeadContacts().forEach(leadContact -> {
                if (leadContact.getGhlContact() != null) {
                    contacts.add(leadContact.getGhlContact());
                }
            });
        });

        ghlReportData.getSalesPersonConversations().forEach(conversation -> {
            if (conversation.getGhlContact() != null) {
                contacts.add(conversation.getGhlContact());
            }
        });

        List<GhlContact> uniqueContacts = contacts.stream()
                .distinct()
                .collect(Collectors.toList());

        if (!uniqueContacts.isEmpty()) {
            log.info("Saving {} unique GHL contacts", uniqueContacts.size());
            ghlContactService.saveAll(uniqueContacts);
        }
    }

    public GhlReportData saveGhlData(GhlReportData ghlReportData) {
        log.info("Saving GHL report data...");

        saveGhlContacts(ghlReportData);

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

        List<PipelineStage> savedPipelineStages = pipelineStageService.saveAll(pipelineStages);

        for (PipelineStage pipelineStage : savedPipelineStages) {
            List<SalesPersonConversion> salesPersonConversions = pipelineStage.getSalesPersonConversions();
            if (salesPersonConversions != null && !salesPersonConversions.isEmpty()) {
                for (SalesPersonConversion conversion : salesPersonConversions) {
                    if (conversion.getConvertedGhlContacts() != null && !conversion.getConvertedGhlContacts().isEmpty()) {
                        for (GhlContact contact : conversion.getConvertedGhlContacts()) {
                            contact.setSalesPersonConversion(conversion);
                        }

                        log.info("Saving {} GHL contacts for SalesPersonConversion ID: {}",
                                conversion.getConvertedGhlContacts().size(), conversion.getId());
                        ghlContactService.saveAll(conversion.getConvertedGhlContacts());
                    }
                }
                salesPersonConversionService.saveAll(salesPersonConversions);
            }
        }
        return savedPipelineStages;
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