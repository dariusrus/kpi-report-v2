package com.blc.kpiReport.service.ghl;

import com.blc.kpiReport.models.pojo.ghl.GhlApiData;
import com.blc.kpiReport.models.pojo.ghl.GhlReportData;
import com.blc.kpiReport.models.pojo.ghl.PipelineStageInfo;
import com.blc.kpiReport.schema.ghl.Calendar;
import com.blc.kpiReport.schema.ghl.*;
import com.blc.kpiReport.service.ghl.models.GhlContactService;
import com.blc.kpiReport.service.ghl.models.GhlUserService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class GhlDataProcessorService {

    public static final String[] websiteLeadSource = new String[] {
        "chat widget",
        "contact us form",
        "database reactivation",
        "looking for more plans form",
        "planning guide form",
        "schedule a talk",
        "schedule a tour",
        "schedule a call",
        "idea book",
        "scope, budget and booking wizard",
        "social media"
    };
    public static final String WEBSITE_LEAD = "Website Lead";
    public static final String MANUAL_USER_INPUT = "Manual User Input";
    public static final String UNSPECIFIED = "Unspecified";
    public static final Set<String> allowedMessageTypes = Set.of("TYPE_CALL", "TYPE_SMS", "TYPE_EMAIL", "TYPE_LIVE_CHAT");

    private final GhlContactService ghlContactService;
    private final GhlUserService ghlUserService;

    public GhlReportData processGhlData(GhlApiData ghlApiData, GoHighLevelReport goHighLevelReport) {
        log.info("Processing GHL data for report: {}", goHighLevelReport.getId());

        Map<String, GhlUser> ghlUsers = preProcessGhlUsers(ghlApiData, goHighLevelReport);

        GhlReportData reportData = GhlReportData.builder()
            .leadSources(processLeadSources(ghlApiData, goHighLevelReport, ghlUsers))
            .calendars(processAppointments(ghlApiData, goHighLevelReport))
            .pipelineStages(processPipelineStages(ghlApiData, goHighLevelReport, ghlUsers))
            .contactsWon(processContactsWon(ghlApiData, goHighLevelReport))
            .salesPersonConversations(processSalesPersonConversations(ghlApiData, goHighLevelReport, ghlUsers))
            .contactScheduledAppointments(processContactScheduledAppointments(ghlApiData, goHighLevelReport, ghlUsers))
            .build();

        reportData.setFollowUpConversions(processFollowUpConversions(goHighLevelReport,
                reportData.getPipelineStages(),
                reportData.getSalesPersonConversations()));

        log.info("GHL data processing completed successfully for report: {}", goHighLevelReport.getId());
        return reportData;
    }

    private List<FollowUpConversion> processFollowUpConversions(
            GoHighLevelReport goHighLevelReport,
            List<PipelineStage> pipelineStages,
            List<SalesPersonConversation> salesPersonConversations) {

        Map<GhlUser, FollowUpConversion> followUpConversionMap = new HashMap<>();

        Set<String> convertedContactIds = pipelineStages.stream()
                .flatMap(stage -> stage.getSalesPersonConversions().stream())
                .flatMap(conversion -> conversion.getConvertedGhlContacts().stream())
                .map(GhlContact::getGhlId)
                .collect(Collectors.toSet());

        for (SalesPersonConversation conversation : salesPersonConversations) {
            GhlUser ghlUser = conversation.getGhlUser();
            GhlContact ghlContact = conversation.getGhlContact();

            if (ghlUser == null || ghlContact == null) {
                continue;
            }

            FollowUpConversion followUpConversion = followUpConversionMap.computeIfAbsent(ghlUser, user -> FollowUpConversion.builder()
                    .ghlUser(user)
                    .sms(0)
                    .emails(0)
                    .calls(0)
                    .liveChatMessages(0)
                    .followUps(0)
                    .conversions(0)
                    .totalSms(0)
                    .totalEmails(0)
                    .totalCalls(0)
                    .totalLiveChatMessages(0)
                    .totalFollowUps(0)
                    .totalConversions(0)
                    .build()
            );

            boolean isConvertedContact = convertedContactIds.contains(ghlContact.getGhlId());

            for (ConversationMessage message : conversation.getConversationMessages()) {
                switch (message.getMessageType()) {
                    case "TYPE_SMS":
                        followUpConversion.setTotalSms(followUpConversion.getTotalSms() + 1);
                        if (isConvertedContact) {
                            followUpConversion.setSms(followUpConversion.getSms() + 1);
                        }
                        break;
                    case "TYPE_EMAIL":
                        followUpConversion.setTotalEmails(followUpConversion.getTotalEmails() + 1);
                        if (isConvertedContact) {
                            followUpConversion.setEmails(followUpConversion.getEmails() + 1);
                        }
                        break;
                    case "TYPE_CALL":
                        followUpConversion.setTotalCalls(followUpConversion.getTotalCalls() + 1);
                        if (isConvertedContact) {
                            followUpConversion.setCalls(followUpConversion.getCalls() + 1);
                        }
                        break;
                    case "TYPE_LIVE_CHAT":
                        followUpConversion.setTotalLiveChatMessages(followUpConversion.getTotalLiveChatMessages() + 1);
                        if (isConvertedContact) {
                            followUpConversion.setLiveChatMessages(followUpConversion.getLiveChatMessages() + 1);
                        }
                        break;
                }
            }

            followUpConversion.setTotalFollowUps(
                    followUpConversion.getTotalSms() +
                            followUpConversion.getTotalEmails() +
                            followUpConversion.getTotalCalls() +
                            followUpConversion.getTotalLiveChatMessages()
            );

            if (isConvertedContact) {
                followUpConversion.setFollowUps(
                        followUpConversion.getSms() +
                                followUpConversion.getEmails() +
                                followUpConversion.getCalls() +
                                followUpConversion.getLiveChatMessages()
                );
            }
        }

        for (PipelineStage pipelineStage : pipelineStages) {
            for (SalesPersonConversion conversion : pipelineStage.getSalesPersonConversions()) {
                GhlUser ghlUser = conversion.getGhlUser();
                if (ghlUser == null) continue;

                FollowUpConversion followUpConversion = followUpConversionMap.computeIfAbsent(ghlUser, user -> FollowUpConversion.builder()
                        .ghlUser(user)
                        .sms(0)
                        .emails(0)
                        .calls(0)
                        .liveChatMessages(0)
                        .followUps(0)
                        .conversions(0)
                        .totalSms(0)
                        .totalEmails(0)
                        .totalCalls(0)
                        .totalLiveChatMessages(0)
                        .totalFollowUps(0)
                        .totalConversions(0)
                        .build()
                );

                int convertedContactsCount = conversion.getConvertedGhlContacts().size();

                followUpConversion.setConversions(followUpConversion.getConversions() + convertedContactsCount);
                followUpConversion.setTotalConversions(
                        followUpConversion.getTotalConversions() + convertedContactsCount
                );
            }
        }

        for (FollowUpConversion followUpConversion : followUpConversionMap.values()) {
            followUpConversion.setGoHighLevelReport(goHighLevelReport);

            int conversions = followUpConversion.getConversions();
            int followUps = followUpConversion.getFollowUps();
            if (conversions > 0) {
                followUpConversion.setFollowUpPerConversion((double) followUps / conversions);
            } else {
                followUpConversion.setFollowUpPerConversion(0.0);
            }

            int totalConversions = followUpConversion.getTotalConversions();
            int totalFollowUps = followUpConversion.getTotalFollowUps();
            if (totalConversions > 0) {
                followUpConversion.setTotalFollowUpPerConversion((double) totalFollowUps / totalConversions);
            } else {
                followUpConversion.setTotalFollowUpPerConversion(0.0);
            }
        }

        return new ArrayList<>(followUpConversionMap.values());
    }

    private Map<String, GhlUser> preProcessGhlUsers(GhlApiData ghlApiData, GoHighLevelReport goHighLevelReport) {
        log.debug("Preprocessing sales persons for report: {}", goHighLevelReport.getId());
        Map<String, JsonNode> ownerMap = ghlApiData.getOwnerMap();
        Map<String, GhlUser> ghlUserMap = new HashMap<>();

        for (Map.Entry<String, JsonNode> entry : ownerMap.entrySet()) {
            String userId = entry.getKey();
            JsonNode ownerNode = entry.getValue();
            String name = ownerNode.path("name").asText("Unknown");
            String photoUrl = ownerNode.path("profilePhoto").asText("");

            GhlUser ghlUser = ghlUserService.findByUserId(userId)
                    .orElse(GhlUser.builder()
                            .userId(userId)
                            .ghlLocation(goHighLevelReport.getKpiReport().getGhlLocation())
                            .build());

            ghlUser.setName(name);
            ghlUser.setPhotoUrl(photoUrl);
            ghlUser = ghlUserService.saveOrUpdate(ghlUser);
            ghlUserMap.put(userId, ghlUser);
        }

        log.debug("Preprocessed {} sales persons for report: {}", ghlUserMap.size(), goHighLevelReport.getId());
        return ghlUserMap;
    }

    private String formatString(String input) {
        if (input == null || input.length() == 0 || input.trim().isEmpty()) {
            return "-";
        }
        try {
            return Arrays.stream(input.split("[_\\s]+"))
                    .filter(word -> !word.isEmpty())
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
        } catch (Exception e) {
            return input;
        }
    }

    private List<LeadSource> processLeadSources(GhlApiData ghlApiData, GoHighLevelReport goHighLevelReport, Map<String, GhlUser> ghlUserMap) {
        log.debug("Processing lead sources for report: {}", goHighLevelReport.getId());
        Map<String, LeadSource> leadSourceMap = new HashMap<>();
        var contactMap = ghlApiData.getCreatedAtContactMap();

        for (JsonNode opportunity : ghlApiData.getCreatedAtOpportunityList()) {
            log.trace("Processing opportunity: {}", opportunity);

            String source = opportunity.path("source").asText();
            if ("".equals(source) || "null".equals(source) || source == null) {
                source = UNSPECIFIED;
            } else {
                source = toTitleCase(source.trim());
            }

            var attributionSource = getAttributionSources(opportunity);
            var attributionMedium = getAttributionMediums(opportunity);

            if ("Database Reactivation".equalsIgnoreCase(source)) {
                log.debug("Skipping lead with source 'Database Reactivation'");
                continue;
            }

            String status = opportunity.path("status").asText();
            double monetaryValue = opportunity.path("monetaryValue").asDouble();

            LeadSource leadSource = leadSourceMap.getOrDefault(source, new LeadSource());
            if (leadSource.getLeadContacts() == null) {
                leadSource.setLeadContacts(new ArrayList<>());
            }
            leadSource.setSource(source);

            var contactNode = contactMap.get(opportunity.path("contact").path("id").asText());
            if (contactNode == null) continue;

            leadSource.setTotalLeads(leadSource.getTotalLeads() + 1);
            leadSource.setTotalValues(leadSource.getTotalValues() + monetaryValue);
            if (leadSource.getGoHighLevelReport() == null) {
                leadSource.setGoHighLevelReport(goHighLevelReport);
            }

            switch (status.toLowerCase()) {
                case "open":
                    leadSource.setOpen(leadSource.getOpen() + 1);
                    break;
                case "won":
                    leadSource.setWon(leadSource.getWon() + 1);
                    break;
                case "lost":
                    leadSource.setLost(leadSource.getLost() + 1);
                    break;
                case "abandoned":
                    leadSource.setAbandoned(leadSource.getAbandoned() + 1);
                    break;
                default:
                    log.warn("Encountered unknown status '{}' for source '{}'", status, source);
                    break;
            }

            leadSourceMap.put(source, leadSource);

            String contactId = contactNode.path("id").asText();
            String contactName = contactNode.path("firstName").asText() + " " + contactNode.path("lastName").asText();
            String contactEmail = contactNode.path("email").asText();
            String contactPhone = contactNode.path("phone").asText();

            GhlContact ghlContact = ghlContactService.saveOrUpdate(GhlContact.builder()
                    .ghlId(contactId)
                    .name(contactName)
                    .email(contactEmail)
                    .phone(contactPhone)
                    .build());

            var leadContact = LeadContact.builder()
                .ghlContact(ghlContact)
                .contactName(ghlContact.getName())
                .contactSource(contactNode.path("source").asText())
                .createdBySource(contactNode.path("createdBy").path("source").asText())
                .attributionSource(attributionSource)
                .attributionMedium(attributionMedium)
                .dateAdded(contactNode.path("dateAdded").asText().substring(0, 10))
                .ghlUser(ghlUserMap.get(opportunity.path("assignedTo").asText()))
                .status(opportunity.path("status").asText().toUpperCase())
                .build();
            leadSource.getLeadContacts().add(leadContact);
            leadContact.setLeadSource(leadSource);
        }

        List<LeadSource> leadSources = new ArrayList<>(leadSourceMap.values());
        leadSourceMap.values().forEach(leadSource -> {
            determineLeadTypeForSource(leadSource);

            int totalAttempts = leadSource.getTotalLeads();
            leadSource.setWinPercentage(totalAttempts > 0 ? (double) leadSource.getWon() / totalAttempts * 100 : 0.0);
        });

        leadSources.sort(Comparator.comparing(LeadSource::getSource));

        log.debug("Processed {} lead sources for report: {}", leadSources.size(), goHighLevelReport.getId());
        return leadSources;
    }

    private String getAttributionSources(JsonNode opportunity) {
        Set<String> attributionSessionSources = new HashSet<>();
        for (JsonNode attribution : opportunity.path("attributions")) {
            String utmSessionSource = attribution.path("utmSessionSource").asText();
            if (!utmSessionSource.isEmpty()) {
                attributionSessionSources.add(utmSessionSource);
            } else {
                attributionSessionSources.add("Other");
            }
        }
        if (ObjectUtils.isEmpty(attributionSessionSources)) attributionSessionSources.add("Other");
        return String.join(", ", attributionSessionSources);
    }

    private String getAttributionMediums(JsonNode opportunity) {
        Set<String> attributionMediums = new HashSet<>();
        for (JsonNode attribution : opportunity.path("attributions")) {
            String medium = attribution.path("medium").asText().toLowerCase();
            if (!medium.isEmpty()) {
                attributionMediums.add(toTitleCase(formatString(medium)));
            }
        }
        return String.join(", ", attributionMediums);
    }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;
        try {
            String[] words = input.split("\\s+");
            StringBuilder titleCase = new StringBuilder();
            for (String word : words) {
                if (word.length() > 1) {
                    titleCase.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase());
                } else {
                    titleCase.append(word.toUpperCase());
                }
                titleCase.append(" ");
            }
            return titleCase.toString().trim();
        } catch (Exception e) {
            log.error("Error while converting to title case: {}", input, e);
            return input;
        }
    }

    private String determineLeadType(String createdBySource) {
        return switch (createdBySource.toUpperCase()) {
            case "FORM", "INTEGRATION", "PUBLIC_API" -> WEBSITE_LEAD;
            default -> MANUAL_USER_INPUT;
        };
    }

    private void determineLeadTypeForSource(LeadSource leadSource) {
        for (LeadContact leadContact : leadSource.getLeadContacts()) {
            if (UNSPECIFIED.equals(leadSource.getSource())) {
                leadSource.setLeadType(MANUAL_USER_INPUT);
                return;
            }
            for (String source : websiteLeadSource) {
                if (leadSource.getSource() != null && leadSource.getSource().toLowerCase().contains(source.toLowerCase())) {
                    leadSource.setLeadType(WEBSITE_LEAD);
                    return;
                }
            }

            String leadType = determineLeadType(leadContact.getCreatedBySource());
            if (WEBSITE_LEAD.equals(leadType)) {
                leadSource.setLeadType(WEBSITE_LEAD);
                return;
            }
        }
        leadSource.setLeadType(MANUAL_USER_INPUT);
    }

    public List<SalesPersonConversation> processSalesPersonConversations(GhlApiData ghlApiData, GoHighLevelReport goHighLevelReport,
                                                                         Map<String, GhlUser> ghlUserMap) {
        List<SalesPersonConversation> salesPersonConversations = new ArrayList<>();

        Map<JsonNode, List<JsonNode>> conversationsMap = ghlApiData.getConversationsMap();

        for (Map.Entry<JsonNode, List<JsonNode>> entry : conversationsMap.entrySet()) {
            JsonNode conversationNode = entry.getKey();
            List<JsonNode> messages = entry.getValue();

            String salesPersonId = conversationNode.path("assignedTo").asText();

            String contactId = conversationNode.path("contactId").asText();
            String contactName = conversationNode.path("contactName").asText();
            String contactEmail = conversationNode.path("email").asText();
            String contactPhone = conversationNode.path("phone").asText();

            if (contactEmail == null || contactEmail.isEmpty() || "null".equalsIgnoreCase(contactEmail)) {
                continue;
            }

            Instant lastManualMessageDate = conversationNode.has("lastManualMessageDate")
                    ? Instant.ofEpochMilli(conversationNode.path("lastManualMessageDate").asLong()) : null;
            String lastMessageType = conversationNode.path("lastMessageType").asText();

            GhlContact ghlContact = ghlContactService.saveOrUpdate(GhlContact.builder()
                    .ghlId(contactId)
                    .name(contactName)
                    .email(contactEmail)
                    .phone(contactPhone)
                    .build());
            SalesPersonConversation salesPersonConversation = SalesPersonConversation.builder()
                    .ghlContact(ghlContact)
                    .lastManualMessageDate(lastManualMessageDate)
                    .lastMessageType(lastMessageType)
                    .ghlUser(ghlUserMap.get(salesPersonId))
                    .goHighLevelReport(goHighLevelReport)
                    .build();

            List<ConversationMessage> conversationMessages = new ArrayList<>();

            for (JsonNode messageNode : messages) {
                String messageType = messageNode.path("messageType").asText();
                String direction = messageNode.path("direction").asText();
                String status = messageNode.path("status").asText();
                String body = messageNode.path("body").asText();
                String source = messageNode.path("source").asText();
                Instant dateAdded = Instant.parse(messageNode.path("dateAdded").asText());

                if ("TYPE_CALL".equals(messageType) && messageNode.has("meta") &&
                        messageNode.path("meta").has("call") &&
                        messageNode.path("meta").path("call").path("duration").isNull()) {
                    continue;
                }

                if (allowedMessageTypes.contains(messageType) && ("app".equals(source) || "api".equals(source))) {
                    int callDuration = 0;
                    if ("TYPE_CALL".equals(messageType) && messageNode.has("meta") &&
                            messageNode.path("meta").has("call")) {
                        callDuration = messageNode.path("meta").path("call").path("duration").asInt(0);
                    }

                    ConversationMessage conversationMessage = ConversationMessage.builder()
                            .messageType(messageType)
                            .direction(direction)
                            .status(status)
                            .callDuration(callDuration)
                            .messageBody(body)
                            .dateAdded(dateAdded)
                            .salesPersonConversation(salesPersonConversation)
                            .build();
                    conversationMessages.add(conversationMessage);
                }
            }

            if (!conversationMessages.isEmpty()) {
                salesPersonConversation.setConversationMessages(conversationMessages);
                salesPersonConversations.add(salesPersonConversation);
            }
        }
        return salesPersonConversations;
    }

    private List<Calendar> processAppointments(GhlApiData ghlApiData, GoHighLevelReport goHighLevelReport) {
        log.debug("Processing appointments for report: {}", goHighLevelReport.getId());

        Map<JsonNode, List<JsonNode>> calendarMap = ghlApiData.getCalendarMap();
        List<JsonNode> opportunities = ghlApiData.getOpportunityList();
        JsonNode pipelineJson = ghlApiData.getPipelineJson();
        List<Calendar> calendars = new ArrayList<>();

        Map<String, String> pipelineStageToPipelineMap = new HashMap<>();
        Map<String, String> pipelineStageToStageMap = new HashMap<>();
        for (JsonNode pipeline : pipelineJson.path("pipelines")) {
            String pipelineName = pipeline.path("name").asText();
            for (JsonNode stage : pipeline.path("stages")) {
                String stageId = stage.path("id").asText();
                String stageName = stage.path("name").asText();
                pipelineStageToPipelineMap.put(stageId, pipelineName);
                pipelineStageToStageMap.put(stageId, stageName);
            }
        }

        for (Map.Entry<JsonNode, List<JsonNode>> entry : calendarMap.entrySet()) {
            JsonNode calendarJsonNode = entry.getKey();
            List<JsonNode> appointmentJsonNodes = entry.getValue();

            Calendar calendar = Calendar.builder()
                    .calendarGhlId(calendarJsonNode.get("id").asText())
                    .calendarName(calendarJsonNode.get("name").asText())
                    .goHighLevelReport(goHighLevelReport)
                    .build();

            Map<String, Integer> statusCountMap = new HashMap<>();
            List<AppointmentOpportunity> appointmentOpportunities = new ArrayList<>();

            for (JsonNode appointmentNode : appointmentJsonNodes) {
                String appointmentContactId = appointmentNode.path("contactId").asText();
                Optional<JsonNode> opportunityOpt = opportunities.stream()
                        .filter(opportunity -> opportunity.path("contact").path("id").asText().equals(appointmentContactId))
                        .findFirst();

                if (opportunityOpt.isPresent()) {
                    JsonNode opportunity = opportunityOpt.get();
                    String status = appointmentNode.path("appointmentStatus").asText();
                    statusCountMap.put(status, statusCountMap.getOrDefault(status, 0) + 1);

                    String pipelineStageId = opportunity.path("pipelineStageId").asText();
                    String pipelineName = pipelineStageToPipelineMap.getOrDefault(pipelineStageId, "Unknown Pipeline");
                    String stageName = pipelineStageToStageMap.getOrDefault(pipelineStageId, "Unknown Stage");

                    Instant appointmentDate = Instant.parse(appointmentNode.path("startTime").asText());

                    if ("showed".equalsIgnoreCase(status)) {

                        GhlContact ghlContact = ghlContactService.saveOrUpdate(GhlContact.builder()
                                .ghlId(appointmentContactId)
                                .name(opportunity.path("contact").path("name").asText())
                                .email(opportunity.path("contact").path("email").asText(null))
                                .phone(opportunity.path("contact").path("phone").asText(null))
                                .build());

                        AppointmentOpportunity appointmentOpportunity = AppointmentOpportunity.builder()
                                .status(status)
                                .appointmentDate(appointmentDate)
                                .lastStageChangeAt(Instant.parse(opportunity.path("lastStageChangeAt").asText()))
                                .pipelineName(pipelineName)
                                .stageName(stageName)
                                .ghlContact(ghlContact)
                                .calendar(calendar)
                                .build();
                        appointmentOpportunities.add(appointmentOpportunity);
                    }
                }
            }

            if (!statusCountMap.isEmpty()) {
                int totalAppointments = statusCountMap.values().stream().mapToInt(Integer::intValue).sum();
                List<Appointment> appointments = new ArrayList<>();

                for (Map.Entry<String, Integer> statusEntry : statusCountMap.entrySet()) {
                    String status = statusEntry.getKey();
                    int count = statusEntry.getValue();
                    double percentage = (double) count / totalAppointments * 100;

                    Appointment appointment = Appointment.builder()
                            .status(status)
                            .count(count)
                            .percentage(percentage)
                            .calendar(calendar)
                            .build();
                    appointments.add(appointment);
                }

                appointments.sort(Comparator.comparing(Appointment::getStatus));
                calendar.setAppointments(appointments);
                calendar.setAppointmentOpportunities(appointmentOpportunities);

                calendars.add(calendar);

                log.debug("Processed {} appointments and {} opportunities for calendar: {}",
                        appointments.size(), appointmentOpportunities.size(), calendar.getCalendarGhlId());
            } else {
                log.debug("Skipping calendar with no appointments: {}", calendarJsonNode.get("id").asText());
            }
        }

        calendars = calendars.stream()
                .filter(calendar -> !calendar.getAppointments().isEmpty())
                .collect(Collectors.toList());

        return calendars;
    }

    private List<PipelineStage> processPipelineStages(GhlApiData ghlApiData, GoHighLevelReport goHighLevelReport,
                                                      Map<String, GhlUser> ghlUserMap) {
        log.debug("Processing pipeline stages for report: {}", goHighLevelReport.getId());
        Map<String, PipelineStageInfo> pipelineStageMap = new HashMap<>();
        Map<String, Map<String, SalesPersonConversion>> salesPersonConversionMap = new HashMap<>();

        for (JsonNode pipelineNode : ghlApiData.getPipelineJson().path("pipelines")) {
            String pipelineName = pipelineNode.path("name").asText();
            for (JsonNode stageNode : pipelineNode.path("stages")) {
                String stageId = stageNode.path("id").asText();
                String stageName = stageNode.path("name").asText();
                int position = stageNode.path("position").asInt();
                pipelineStageMap.put(stageId, new PipelineStageInfo(stageName, pipelineName, position));
                salesPersonConversionMap.put(stageId, new HashMap<>());
            }
        }

        for (JsonNode pipelineNode : ghlApiData.getPipelineJson().path("pipelines")) {
            String pipelineName = pipelineNode.path("name").asText();
            for (JsonNode stageNode : pipelineNode.path("stages")) {
                String stageId = stageNode.path("id").asText();
                String stageName = stageNode.path("name").asText();
            }
        }

        Map<String, Integer> stageCountMap = new HashMap<>();
        Map<String, Double> stageMonetaryValueMap = new HashMap<>();
        Map<String, Integer> pipelineTotalCountMap = new HashMap<>();

        for (JsonNode opportunity : ghlApiData.getLastStageChangeOpportunityList()) {
            String stageId = opportunity.path("pipelineStageId").asText();
            double monetaryValue = opportunity.path("monetaryValue").asDouble();

            PipelineStageInfo stageInfo = pipelineStageMap.get(stageId);
            if (stageInfo == null) continue;

            String pipelineName = stageInfo.getPipelineName();
            stageCountMap.put(stageId, stageCountMap.getOrDefault(stageId, 0) + 1);
            stageMonetaryValueMap.put(stageId, stageMonetaryValueMap.getOrDefault(stageId, 0.0) + monetaryValue);
            pipelineTotalCountMap.put(pipelineName, pipelineTotalCountMap.getOrDefault(pipelineName, 0) + 1);

            GhlUser ghlUser = ghlUserMap.get(opportunity.path("assignedTo").asText());
            if (ghlUser == null) {
                continue;
            }

            JsonNode contactNode = opportunity.path("contact");
            if (contactNode.isMissingNode()) {
                log.warn("Contact information is missing for opportunity: {}", opportunity);
                continue;
            }

            GhlContact ghlContact = GhlContact.builder()
                    .ghlId(contactNode.path("id").asText())
                    .name(contactNode.path("name").asText())
                    .email(contactNode.path("email").asText(null))
                    .phone(contactNode.path("phone").asText(null))
                    .build();

            Map<String, SalesPersonConversion> stageSalesMap = salesPersonConversionMap.get(stageId);
            SalesPersonConversion conversion = stageSalesMap.getOrDefault(ghlUser.getUserId(), SalesPersonConversion.builder()
                    .count(0)
                    .monetaryValue(0.0)
                    .ghlUser(ghlUser)
                    .convertedGhlContacts(new ArrayList<>())
                    .build());

            conversion.getConvertedGhlContacts().add(ghlContact);
            conversion.setCount(conversion.getCount() + 1);
            conversion.setMonetaryValue(conversion.getMonetaryValue() + monetaryValue);
            stageSalesMap.put(ghlUser.getUserId(), conversion);
        }

        List<PipelineStage> pipelineStages = new ArrayList<>();

        for (Map.Entry<String, PipelineStageInfo> entry : pipelineStageMap.entrySet()) {
            String stageId = entry.getKey();
            PipelineStageInfo stageInfo = entry.getValue();

            String stageName = stageInfo.getStageName();
            String pipelineName = stageInfo.getPipelineName();
            int position = stageInfo.getPosition();

            int count = stageCountMap.getOrDefault(stageId, 0);
            double monetaryValue = stageMonetaryValueMap.getOrDefault(stageId, 0.0);
            int pipelineTotalCount = pipelineTotalCountMap.getOrDefault(pipelineName, 0);
            double percentage = pipelineTotalCount > 0 ? (double) count / pipelineTotalCount * 100 : 0;

            List<SalesPersonConversion> salesPersonConversions = new ArrayList<>(salesPersonConversionMap.get(stageId).values());

            PipelineStage pipelineStage = PipelineStage.builder()
                    .pipelineName(pipelineName)
                    .stageName(stageName)
                    .count(count)
                    .percentage(percentage)
                    .monetaryValue(monetaryValue)
                    .position(position)
                    .goHighLevelReport(goHighLevelReport)
                    .salesPersonConversions(salesPersonConversions)
                    .build();

            for (SalesPersonConversion conversion : salesPersonConversions) {
                conversion.setPipelineStage(pipelineStage);
            }
            pipelineStages.add(pipelineStage);
        }

        pipelineStages.sort(Comparator.comparing(PipelineStage::getPipelineName)
                .thenComparing(PipelineStage::getPosition));

        log.debug("Processed {} pipeline stages for report: {}", pipelineStages.size(), goHighLevelReport.getId());
        return pipelineStages;
    }

    private List<ContactWon> processContactsWon(GhlApiData ghlApiData, GoHighLevelReport goHighLevelReport) {
        log.debug("Processing contacts won for report: {}", goHighLevelReport.getId());
        List<ContactWon> contactsWon = new ArrayList<>();
        var contactMap = ghlApiData.getContactsWonContactMap();

        Set<String> processedContactNames = new HashSet<>();

        for (JsonNode opportunity : ghlApiData.getContactsWonOpportunityList()) {
            String status = opportunity.path("status").asText().toLowerCase();
            if ("won".equals(status)) {
                var opportunityContactNode = opportunity.path("contact");
                var contactNode = contactMap.get(opportunity.path("contact").path("id").asText());

                var source = contactNode.path("source").asText();
                var attributionSource = getAttributionSources(opportunity);

                String contactId = contactNode.path("id").asText();
                String contactName = contactNode.path("firstName").asText() + " " + contactNode.path("lastName").asText();
                String contactEmail = contactNode.path("email").asText();
                String contactPhone = contactNode.path("phone").asText();

                GhlContact ghlContact = ghlContactService.saveOrUpdate(GhlContact.builder()
                        .ghlId(contactId)
                        .name(contactName)
                        .email(contactEmail)
                        .phone(contactPhone)
                        .build());

                if (!processedContactNames.contains(contactName)) {
                    ContactWon contactWon = ContactWon.builder()
                        .ghlContact(ghlContact)
                        .source(source)
                        .attributionSource(attributionSource)
                        .goHighLevelReport(goHighLevelReport)
                        .build();
                    contactsWon.add(contactWon);

                    processedContactNames.add(contactName);
                }
            }
        }

        log.debug("Processed {} contacts won for report: {}", contactsWon.size(), goHighLevelReport.getId());
        return contactsWon;
    }

    private List<ContactScheduledAppointment> processContactScheduledAppointments(GhlApiData ghlApiData, GoHighLevelReport goHighLevelReport, Map<String, GhlUser> ghlUserMap) {
        log.debug("Processing lead scheduled appointments for report: {}", goHighLevelReport.getId());
        var contactMap = ghlApiData.getCreatedAtContactMap();
        List<ContactScheduledAppointment> contactScheduledAppointments = new ArrayList<>();

        for (JsonNode opportunity : ghlApiData.getCreatedAtOpportunityList()) {
            log.trace("Processing opportunity: {}", opportunity);

            String source = opportunity.path("source").asText();
            if ("".equals(source) || "null".equals(source) || source == null) {
                source = UNSPECIFIED;
            } else {
                source = toTitleCase(source.trim());
            }

            if ("Database Reactivation".equalsIgnoreCase(source)) {
                log.debug("Skipping lead with source 'Database Reactivation'");
                continue;
            }

            var contactNode = contactMap.get(opportunity.path("contact").path("id").asText());
            if (contactNode == null) continue;

            String contactId = contactNode.path("id").asText();
            String contactName = contactNode.path("firstName").asText() + " " + contactNode.path("lastName").asText();
            String contactEmail = contactNode.path("email").asText();
            String contactPhone = contactNode.path("phone").asText();

            GhlContact ghlContact = ghlContactService.saveOrUpdate(GhlContact.builder()
                    .ghlId(contactId)
                    .name(contactName)
                    .email(contactEmail)
                    .phone(contactPhone)
                    .build());

            var contactScheduledAppointment = com.blc.kpiReport.schema.ghl.ContactScheduledAppointment.builder()
                    .ghlContact(ghlContact)
                    .contactName(ghlContact.getName())
                    .ghlUser(ghlUserMap.get(opportunity.path("assignedTo").asText()))
                    .scheduledACall(checkScheduledCall(ghlContact.getGhlId(), ghlApiData.getCalendarMap()))
                    .goHighLevelReport(goHighLevelReport)
                    .build();
            contactScheduledAppointments.add(contactScheduledAppointment);

        }
        return contactScheduledAppointments;
    }

    private boolean checkScheduledCall(String ghlId, Map<JsonNode, List<JsonNode>> calendarMap) {
        for (Map.Entry<JsonNode, List<JsonNode>> entry : calendarMap.entrySet()) {
            JsonNode calendarJsonNode = entry.getKey();
            List<JsonNode> appointmentJsonNodes = entry.getValue();

            for (JsonNode appointmentNode : appointmentJsonNodes) {
                String appointmentContactId = appointmentNode.path("contactId").asText();
                if (appointmentContactId.equals(ghlId)) return true;
            }
        }
        return false;
    }
}
