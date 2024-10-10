package com.blc.kpiReport.service.ghl;

import com.blc.kpiReport.exception.GhlApiException;
import com.blc.kpiReport.models.pojo.GhlApiData;
import com.blc.kpiReport.schema.GhlLocation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.blc.kpiReport.util.DateUtil.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class GhlDataFetchService {

    private static final String GHL_API_BASE_URL = "https://services.leadconnectorhq.com";
    private static final String OPPORTUNITY_URL_TEMPLATE = "%s/opportunities/search?location_id=%s&endDate=%s&date=%s&page=%d&limit=100";
    private static final String EVENTS_URL_TEMPLATE = "%s/calendars/events?locationId=%s&calendarId=%s&startTime=%d&endTime=%d";
    private static final String PIPELINE_URL_TEMPLATE = "%s/opportunities/pipelines?locationId=%s";
    private static final String CALENDAR_URL_TEMPLATE = "%s/calendars/?locationId=%s";
    private static final String CONTACT_URL_TEMPLATE = "%s/contacts/%s";
    private static final String OWNER_URL_TEMPLATE = "%s/users/%s";


    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final RetryTemplate retryTemplate;
    private final GhlTokenService tokenService;

    public GhlApiData getGhlData(GhlLocation location, String startDate, String endDate) throws GhlApiException {
        log.info("Fetching GHL data for location ID: {}, from {} to {}", location.getLocationId(), startDate, endDate);

        location = tokenService.getAccessToken(location);
        var locationId = location.getLocationId();
        var accessToken = location.getGhlAccessToken();
        log.debug("Obtained access token for location ID: {}", locationId);

        try {
            var opportunityList = fetchOpportunities(locationId, accessToken, startDate, endDate);

            // filter for this month only
            var createdAtOpportunityList = filterCreatedAt(opportunityList, startDate, endDate);
            var createdAtContactMap = fetchContactsAndMap(createdAtOpportunityList, accessToken);

            // filter status changes this month
            var lastStageChangeOpportunityList = filterLastStageChange(opportunityList, startDate, endDate);

            // filter contacts where won status lands on this month
            var contactsWonOpportunityList = filterContactsWon(opportunityList, startDate, endDate);
            var contactsWonContactMap = fetchContactsAndMap(contactsWonOpportunityList, accessToken);

            var ownersMap = fetchOwnersAndMap(opportunityList, accessToken);

            var eventsMap = fetchEventForEachCalendar(locationId, accessToken, startDate, endDate);
            var pipelineJson = fetchPipelineStages(accessToken, locationId);

            log.info("Successfully fetched GHL data for location ID: {}", locationId);
            return GhlApiData.builder()
                .opportunityList(opportunityList)
                .createdAtOpportunityList(createdAtOpportunityList)
                .createdAtContactMap(createdAtContactMap)
                .lastStageChangeOpportunityList(lastStageChangeOpportunityList)
                .contactsWonOpportunityList(contactsWonOpportunityList)
                .contactsWonContactMap(contactsWonContactMap)
                .calendarMap(eventsMap)
                .ownerMap(ownersMap)
                .pipelineJson(pipelineJson)
                .build();
        } catch (IOException e) {
            log.error("Failed to fetch GHL data: {}", e.getMessage());
            throw new GhlApiException("Failed to fetch GHL data after multiple attempts", e);
        }
    }

    private Map<String, JsonNode> fetchOwnersAndMap(List<JsonNode> opportunityList, String accessToken) throws IOException {
        Map<String, JsonNode> ownerMap = new HashMap<>();

        for (JsonNode opportunity : opportunityList) {
            String userId = opportunity.path("assignedTo").asText();
            if (userId != null && !userId.isEmpty() && !ownerMap.containsKey(userId)) {
                String url = String.format(OWNER_URL_TEMPLATE, GHL_API_BASE_URL, userId);
                log.debug("Fetching owner from URL: {}", url);

                Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Version", "2021-07-28")
                    .addHeader("Accept", "application/json")
                    .build();

                try (Response response = okHttpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        log.error("Error fetching owner with ID {}: {}", userId, response);
                        continue;
                    }

                    JsonNode ownerJson = objectMapper.readTree(response.body().string());
                    ownerMap.put(userId, ownerJson);
                    log.debug("Successfully fetched and added owner with ID {} to the map", userId);
                } catch (IOException e) {
                    log.error("Failed to fetch owner with ID {}: {}", userId, e.getMessage());
                }
            }
        }

        log.info("Successfully fetched and mapped {} owners", ownerMap.size());
        return ownerMap;
    }

    private Map<String, JsonNode> fetchContactsAndMap(List<JsonNode> opportunityList, String accessToken) throws IOException {
        Map<String, JsonNode> contactMap = new HashMap<>();
        int requestCount = 0;

        for (JsonNode opportunity : opportunityList) {
            String contactId = opportunity.path("contact").path("id").asText();
            if (contactId != null && !contactId.isEmpty()) {
                String url = String.format(CONTACT_URL_TEMPLATE, GHL_API_BASE_URL, contactId);
                log.debug("Fetching contact from URL: {}", url);

                Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Version", "2021-07-28")
                    .addHeader("Accept", "application/json")
                    .build();

                try (Response response = okHttpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        log.error("Error fetching contact with ID {}: {}", contactId, response);
                        continue;
                    }

                    JsonNode contactJson = objectMapper.readTree(response.body().string()).path("contact");
                    contactMap.put(contactId, contactJson);
                    log.debug("Successfully fetched and added contact with ID {} to the map", contactId);
                } catch (IOException e) {
                    log.error("Failed to fetch contact with ID {}: {}", contactId, e.getMessage());
                }

                requestCount++;
                if (requestCount % 25 == 0) {
                    try {
                        log.info("Pausing for 5 seconds after 25 requests...");
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        log.error("Thread interrupted during sleep: {}", e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        log.info("Successfully fetched and mapped {} contacts", contactMap.size());
        return contactMap;
    }

    private static List<JsonNode> filterContactsWon(List<JsonNode> opportunityList, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
        LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);

        return opportunityList.stream()
            .filter(opportunity -> {
                String status = opportunity.get("status").asText();
                String lastStatusChangeAt = opportunity.get("lastStatusChangeAt").asText();
                if (lastStatusChangeAt != null && !"null".equals(lastStatusChangeAt) && lastStatusChangeAt.length() >= 10) {
                    LocalDate statusChangeDate = LocalDate.parse(lastStatusChangeAt.substring(0, 10), DATE_FORMATTER);
                    return "won".equalsIgnoreCase(status) &&
                        (statusChangeDate.isEqual(start) || statusChangeDate.isAfter(start)) &&
                        (statusChangeDate.isEqual(end) || statusChangeDate.isBefore(end));
                }
                return false;
            })
            .collect(Collectors.toList());
    }

    private static List<JsonNode> filterLastStageChange(List<JsonNode> opportunityList, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
        LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);

        return opportunityList.stream()
            .filter(opportunity -> {
                String lastStageChangeAt = opportunity.get("lastStageChangeAt").asText();
                String createdAt = opportunity.get("createdAt").asText();
                if (lastStageChangeAt != null && !"null".equals(lastStageChangeAt) && lastStageChangeAt.length() >= 10) {
                    LocalDate stageChangeDate = LocalDate.parse(lastStageChangeAt.substring(0, 10), DATE_FORMATTER);
                    return (stageChangeDate.isEqual(start) || stageChangeDate.isAfter(start)) &&
                        (stageChangeDate.isEqual(end) || stageChangeDate.isBefore(end));
                } else if (lastStageChangeAt == null || "null".equals(lastStageChangeAt)) {
                    LocalDate createdDate = LocalDate.parse(createdAt.substring(0, 10), DATE_FORMATTER);
                    return (createdDate.isEqual(start) || createdDate.isAfter(start)) &&
                        (createdDate.isEqual(end) || createdDate.isBefore(end));
                }
                return false;
            })
            .collect(Collectors.toList());
    }

    private static List<JsonNode> filterCreatedAt(List<JsonNode> opportunityList, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
        LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);

        return opportunityList.stream()
            .filter(opportunity -> {
                String createdAt = opportunity.get("createdAt").asText();
                LocalDate createdDate = LocalDate.parse(createdAt.substring(0, 10), DATE_FORMATTER);
                return (createdDate.isEqual(start) || createdDate.isAfter(start)) &&
                    (createdDate.isEqual(end) || createdDate.isBefore(end));
            })
            .collect(Collectors.toList());
    }

private List<JsonNode> fetchOpportunities(String locationId, String accessToken, String startDate, String endDate) throws IOException {
    return retryTemplate.execute(context -> {
        log.info("Attempt {} to fetch opportunities for location ID: {}, from {} to {}", context.getRetryCount() + 1, locationId, startDate, endDate);

        var allOpportunities = new ArrayList<JsonNode>();
        var formattedStartDate = formatDate(subtractOneYear(startDate));
        var formattedEndDate = formatDate(endDate);
        var url = buildOpportunityUrl(locationId, formattedStartDate, formattedEndDate, 1);

        while (url != null) {
            log.debug("Fetching opportunities from URL: {}", url);
            var request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Version", "2021-07-28")
                .addHeader("Accept", "application/json")
                .build();

            try (var response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Error fetching opportunities: " + response);

                var responseJson = objectMapper.readTree(response.body().string());
                var opportunities = responseJson.path("opportunities");
                opportunities.forEach(opportunity -> {
                    var contactEmail = opportunity.path("contact").path("email").asText();
                    if (!contactEmail.contains("builderleadconverter.com")) {
                        allOpportunities.add(opportunity);
                    }
                });

                var nextPage = responseJson.path("meta").path("nextPage").asInt();
                url = nextPage > 0 ? buildOpportunityUrl(locationId, formattedStartDate, formattedEndDate, nextPage) : null;
                log.debug("Next page URL: {}", url);
            }
        }
        log.info("Successfully fetched {} opportunities for location ID: {}", allOpportunities.size(), locationId);
        return allOpportunities;
    });
}

    private String buildOpportunityUrl(String locationId, String startDate, String endDate, int page) {
        return String.format(OPPORTUNITY_URL_TEMPLATE, GHL_API_BASE_URL, locationId, endDate, startDate, page);
    }

    private Map<JsonNode, List<JsonNode>> fetchEventForEachCalendar(String locationId, String accessToken, String startTime, String endTime) throws IOException {
        return retryTemplate.execute(context -> {
            log.info("Attempt {} to fetch events for location ID: {}, from {} to {}", context.getRetryCount() + 1, locationId, startTime, endTime);

            LocalDate startDate = LocalDate.parse(startTime);
            LocalDate endDate = LocalDate.parse(endTime);
            long epochStartTime = startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000; // milliseconds
            long epochEndTime = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(); // milliseconds
            List<JsonNode> calendars = fetchCalendars(locationId, accessToken);

            Map<JsonNode, List<JsonNode>> eventsMap = new HashMap<>();

            for (JsonNode calendar : calendars) {
                String calendarId = calendar.path("id").asText();
                List<JsonNode> events = new ArrayList<>();
                var url = String.format(EVENTS_URL_TEMPLATE, GHL_API_BASE_URL, locationId, calendarId, epochStartTime, epochEndTime);
                log.debug("Fetching events from URL: {}", url);
                Request appointmentRequest = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Version", "2021-04-15")
                    .addHeader("Accept", "application/json")
                    .build();

                try (Response eventResponse = okHttpClient.newCall(appointmentRequest).execute()) {
                    if (!eventResponse.isSuccessful()) {
                        throw new IOException("Error encountered fetching events: " + eventResponse);
                    }

                    JsonNode eventsJson = objectMapper.readTree(eventResponse.body().string());
                    eventsJson.path("events").forEach(events::add);
                }
                if (events.size() > 0) {
                    log.info("Successfully fetched {} events for calendar ID: {} for location ID: {}", events.size(), calendarId, locationId);
                }
                eventsMap.put(calendar, events);
            }
            return eventsMap;
        });
    }

    private JsonNode fetchPipelineStages(String accessToken, String locationId) throws IOException {
        return retryTemplate.execute(context -> {
            log.info("Attempt {} to fetch pipeline stages for location ID: {}", context.getRetryCount() + 1, locationId);

            var url = String.format(PIPELINE_URL_TEMPLATE, GHL_API_BASE_URL, locationId);
            log.debug("Fetching pipeline stages from URL: {}", url);
            Request pipelineRequest = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Version", "2021-07-28")
                .addHeader("Accept", "application/json")
                .build();

            try (Response pipelineResponse = okHttpClient.newCall(pipelineRequest).execute()) {
                if (!pipelineResponse.isSuccessful()) {
                    throw new IOException("Error encountered fetching pipelines: " + pipelineResponse);
                }
                log.info("Successfully fetched pipeline stages for location ID: {}", locationId);
                return objectMapper.readTree(pipelineResponse.body().string());
            }
        });
    }

    private List<JsonNode> fetchCalendars(String locationId, String accessToken) throws IOException {
        return retryTemplate.execute(context -> {
            log.info("Attempt {} to fetch calendar IDs for location ID: {}", context.getRetryCount() + 1, locationId);

            var url = String.format(CALENDAR_URL_TEMPLATE, GHL_API_BASE_URL, locationId);
            log.debug("Fetching calendar IDs from URL: {}", url);
            Request calendarRequest = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Version", "2021-04-15")
                .addHeader("Accept", "application/json")
                .build();

            try (Response calendarResponse = okHttpClient.newCall(calendarRequest).execute()) {
                if (!calendarResponse.isSuccessful()) {
                    throw new IOException("Error encountered fetching calendars: " + calendarResponse);
                }

                JsonNode calendarJson = objectMapper.readTree(calendarResponse.body().string());
                List<JsonNode> calendars = new ArrayList<>();
                for (JsonNode calendarNode : calendarJson.path("calendars")) {
                    calendars.add(calendarNode);
                }
                log.info("Successfully fetched {} calendar IDs for location ID: {}", calendars.size(), locationId);
                return calendars;
            }
        });
    }
}
