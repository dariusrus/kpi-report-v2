package com.blc.kpiReport.service.ghl;

import com.blc.kpiReport.models.pojo.GhlApiData;
import com.blc.kpiReport.models.pojo.GhlReportData;
import com.blc.kpiReport.models.pojo.PipelineStageInfo;
import com.blc.kpiReport.schema.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class GhlDataProcessorService {

    public GhlReportData processGhlData(GhlApiData ghlApiData, GoHighLevelReport goHighLevelReport) {
        log.info("Processing GHL data for report: {}", goHighLevelReport.getId());
        GhlReportData reportData = GhlReportData.builder()
            .leadSources(processLeadSources(ghlApiData, goHighLevelReport))
            .appointments(processAppointments(ghlApiData, goHighLevelReport))
            .pipelineStages(processPipelineStages(ghlApiData, goHighLevelReport))
            .contactsWon(processContactsWon(ghlApiData, goHighLevelReport))
            .build();

        log.info("GHL data processing completed successfully for report: {}", goHighLevelReport.getId());
        return reportData;
    }

    private List<LeadSource> processLeadSources(GhlApiData ghlApiData, GoHighLevelReport goHighLevelReport) {
        log.debug("Processing lead sources for report: {}", goHighLevelReport.getId());
        Map<String, LeadSource> leadSourceMap = new HashMap<>();

        for (JsonNode opportunity : ghlApiData.getOpportunityList()) {
            // Logging individual opportunity processing
            log.trace("Processing opportunity: {}", opportunity);

            String source = opportunity.path("source").asText();
            String status = opportunity.path("status").asText();
            double monetaryValue = opportunity.path("monetaryValue").asDouble();

            LeadSource leadSource = leadSourceMap.getOrDefault(source, new LeadSource());
            leadSource.setSource(source);
            leadSource.setTotalLeads(leadSource.getTotalLeads() + 1);
            leadSource.setTotalValues(leadSource.getTotalValues() + monetaryValue);
            leadSource.setGoHighLevelReport(goHighLevelReport);

            switch (status) {
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
        }

        leadSourceMap.values().forEach(leadSource -> {
            int totalAttempts = leadSource.getTotalLeads();
            leadSource.setWinPercentage(totalAttempts > 0 ? (double) leadSource.getWon() / totalAttempts * 100 : 0.0);
        });

        List<LeadSource> leadSources = new ArrayList<>(leadSourceMap.values());
        leadSources.sort(Comparator.comparing(LeadSource::getSource));

        log.debug("Processed {} lead sources for report: {}", leadSources.size(), goHighLevelReport.getId());
        return leadSources;
    }

    private List<Appointment> processAppointments(GhlApiData ghlApiData, GoHighLevelReport goHighLevelReport) {
        log.debug("Processing appointments for report: {}", goHighLevelReport.getId());
        Map<String, Integer> statusCountMap = new HashMap<>();

        for (JsonNode eventNode : ghlApiData.getEventsJson()) {
            for (JsonNode appointmentNode : eventNode.path("events")) {
                String status = appointmentNode.path("appointmentStatus").asText();
                statusCountMap.put(status, statusCountMap.getOrDefault(status, 0) + 1);
            }
        }

        int totalAppointments = statusCountMap.values().stream().mapToInt(Integer::intValue).sum();
        log.debug("Total appointments found: {}", totalAppointments);

        List<Appointment> appointments = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : statusCountMap.entrySet()) {
            String status = entry.getKey();
            int count = entry.getValue();
            double percentage = (double) count / totalAppointments * 100;

            Appointment appointment = Appointment.builder()
                .status(status)
                .count(count)
                .percentage(percentage)
                .goHighLevelReport(goHighLevelReport)
                .build();
            appointments.add(appointment);
        }
        appointments.sort(Comparator.comparing(Appointment::getStatus));

        log.debug("Processed {} appointments for report: {}", appointments.size(), goHighLevelReport.getId());
        return appointments;
    }

    private List<PipelineStage> processPipelineStages(GhlApiData ghlApiData, GoHighLevelReport goHighLevelReport) {
        log.debug("Processing pipeline stages for report: {}", goHighLevelReport.getId());
        Map<String, PipelineStageInfo> pipelineStageMap = new HashMap<>();

        for (JsonNode pipelineNode : ghlApiData.getPipelineJson().path("pipelines")) {
            String pipelineName = pipelineNode.path("name").asText();
            for (JsonNode stageNode : pipelineNode.path("stages")) {
                String stageId = stageNode.path("id").asText();
                String stageName = stageNode.path("name").asText();
                int position = stageNode.path("position").asInt();
                pipelineStageMap.put(stageId, new PipelineStageInfo(stageName, pipelineName, position));
            }
        }

        Map<String, Integer> stageCountMap = new HashMap<>();
        Map<String, Double> stageMonetaryValueMap = new HashMap<>();
        Map<String, Integer> pipelineTotalCountMap = new HashMap<>();

        for (JsonNode opportunity : ghlApiData.getOpportunityList()) {
            String stageId = opportunity.path("pipelineStageId").asText();
            double monetaryValue = opportunity.path("monetaryValue").asDouble();

            PipelineStageInfo stageInfo = pipelineStageMap.get(stageId);
            if (stageInfo == null) continue;

            String pipelineName = stageInfo.getPipelineName();

            stageCountMap.put(stageId, stageCountMap.getOrDefault(stageId, 0) + 1);
            stageMonetaryValueMap.put(stageId, stageMonetaryValueMap.getOrDefault(stageId, 0.0) + monetaryValue);
            pipelineTotalCountMap.put(pipelineName, pipelineTotalCountMap.getOrDefault(pipelineName, 0) + 1);
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

            PipelineStage pipelineStage = PipelineStage.builder()
                .pipelineName(pipelineName)
                .stageName(stageName)
                .count(count)
                .percentage(percentage)
                .monetaryValue(monetaryValue)
                .position(position)
                .goHighLevelReport(goHighLevelReport)
                .build();
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

        for (JsonNode opportunity : ghlApiData.getOpportunityList()) {
            String status = opportunity.path("status").asText().toLowerCase();
            if ("won".equals(status)) {
                JsonNode contactNode = opportunity.path("contact");
                String contactName = contactNode.path("name").asText();
                String contactEmail = contactNode.path("email").asText();

                ContactWon contactWon = ContactWon.builder()
                    .contactName(contactName)
                    .contactEmail(contactEmail)
                    .goHighLevelReport(goHighLevelReport)
                    .build();
                contactsWon.add(contactWon);
            }
        }

        log.debug("Processed {} contacts won for report: {}", contactsWon.size(), goHighLevelReport.getId());
        return contactsWon;
    }
}
