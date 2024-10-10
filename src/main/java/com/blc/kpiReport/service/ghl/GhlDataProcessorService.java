package com.blc.kpiReport.service.ghl;

import com.blc.kpiReport.models.pojo.GhlApiData;
import com.blc.kpiReport.models.pojo.GhlReportData;
import com.blc.kpiReport.models.pojo.PipelineStageInfo;
import com.blc.kpiReport.schema.ghl.Calendar;
import com.blc.kpiReport.schema.ghl.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
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

    public GhlReportData processGhlData(GhlApiData ghlApiData, GoHighLevelReport goHighLevelReport) {
        log.info("Processing GHL data for report: {}", goHighLevelReport.getId());
        GhlReportData reportData = GhlReportData.builder()
            .leadSources(processLeadSources(ghlApiData, goHighLevelReport))
            .calendars(processAppointments(ghlApiData, goHighLevelReport))
            .pipelineStages(processPipelineStages(ghlApiData, goHighLevelReport))
            .contactsWon(processContactsWon(ghlApiData, goHighLevelReport))
            .build();

        log.info("GHL data processing completed successfully for report: {}", goHighLevelReport.getId());
        return reportData;
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

    private List<LeadSource> processLeadSources(GhlApiData ghlApiData, GoHighLevelReport goHighLevelReport) {
        log.debug("Processing lead sources for report: {}", goHighLevelReport.getId());
        Map<String, LeadSource> leadSourceMap = new HashMap<>();
        var contactMap = ghlApiData.getCreatedAtContactMap();
        var ownerMap = ghlApiData.getOwnerMap();

        for (JsonNode opportunity : ghlApiData.getCreatedAtOpportunityList()) {
            log.trace("Processing opportunity: {}", opportunity);

            String source = opportunity.path("source").asText();
            if ("".equals(source) || "null".equals(source) || source == null) {
                source = UNSPECIFIED;
            } else {
                source = toTitleCase(source.trim());
            }

            Set<String> attributionMediums = new HashSet<>();
            for (JsonNode attribution : opportunity.path("attributions")) {
                String medium = attribution.path("medium").asText().toLowerCase();
                if (!medium.isEmpty()) {
                    attributionMediums.add(toTitleCase(formatString(medium)));
                }

                if (medium.contains("facebook") || medium.contains("instagram") || medium.contains("gbp")) {
                    source = "Social Media";
                }
            }

            var attributionSource = String.join(", ", attributionMediums);

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

            var ownerNode = ownerMap.get(opportunity.path("assignedTo").asText());

            var leadContact = LeadContact.builder()
                .contactName(opportunity.path("contact").path("name").asText())
                .contactSource(contactNode.path("source").asText())
                .createdBySource(contactNode.path("createdBy").path("source").asText())
                .attributionSource(attributionSource)
                .dateAdded(contactNode.path("dateAdded").asText().substring(0, 10))
                .ownerName(ownerNode != null ? ownerNode.path("name").asText() : "")
                .ownerPhotoUrl(ownerNode != null ? ownerNode.path("profilePhoto").asText() : "")
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

    private List<Calendar> processAppointments(GhlApiData ghlApiData, GoHighLevelReport goHighLevelReport) {
        log.debug("Processing appointments for report: {}", goHighLevelReport.getId());

        Map<JsonNode, List<JsonNode>> calendarMap = ghlApiData.getCalendarMap();
        List<Calendar> calendars = new ArrayList<>();

        for (Map.Entry<JsonNode, List<JsonNode>> entry : calendarMap.entrySet()) {
            JsonNode calendarJsonNode = entry.getKey();
            List<JsonNode> appointmentJsonNodes = entry.getValue();

            // Create Calendar object
            Calendar calendar = Calendar.builder()
                .calendarGhlId(calendarJsonNode.get("id").asText())
                .calendarName(calendarJsonNode.get("name").asText())
                .goHighLevelReport(goHighLevelReport)
                .build();

            // Process appointments within the current calendar
            Map<String, Integer> statusCountMap = new HashMap<>();

            for (JsonNode appointmentNode : appointmentJsonNodes) {
                String status = appointmentNode.path("appointmentStatus").asText();
                statusCountMap.put(status, statusCountMap.getOrDefault(status, 0) + 1);
            }

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

            // Assign the appointments to the calendar
            calendar.setAppointments(appointments);
            calendar.getAppointments().forEach(appointment -> appointment.setCalendar(calendar));
            calendars.add(calendar);

            if (appointments.size() > 0) {
                log.debug("Processed {} appointments for calendar: {}", appointments.size(), calendar.getCalendarGhlId());
            }
        }
        return calendars;
    }

    private List<PipelineStage> processPipelineStages(GhlApiData ghlApiData, GoHighLevelReport goHighLevelReport) {
        log.debug("Processing pipeline stages for report: {}", goHighLevelReport.getId());
        Map<String, PipelineStageInfo> pipelineStageMap = new HashMap<>();
        Map<String, Map<String, SalesPersonConversion>> salesPersonConversionMap = new HashMap<>();
        var ownerMap = ghlApiData.getOwnerMap();

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

            String salesPersonId = opportunity.path("assignedTo").asText();
            var ownerNode = ownerMap.get(salesPersonId);
            String salesPersonName = ownerNode != null ? ownerNode.path("name").asText() : "Unknown";

            Map<String, SalesPersonConversion> stageSalesMap = salesPersonConversionMap.get(stageId);
            SalesPersonConversion conversion = stageSalesMap.getOrDefault(salesPersonId, SalesPersonConversion.builder()
                    .salesPersonId(salesPersonId)
                    .salesPersonName(salesPersonName)
                    .count(0)
                    .monetaryValue(0.0)
                    .build());
            conversion.setCount(conversion.getCount() + 1);
            conversion.setMonetaryValue(conversion.getMonetaryValue() + monetaryValue);
            stageSalesMap.put(salesPersonId, conversion);
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
                conversion.setPipelineStage(pipelineStage);  // Ensure each conversion references its parent stage
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

                var contactName = opportunityContactNode.path("name").asText();
                var source = contactNode.path("source").asText();

                if (!processedContactNames.contains(contactName)) {
                    ContactWon contactWon = ContactWon.builder()
                        .contactName(contactName)
                        .source(source)
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
}
