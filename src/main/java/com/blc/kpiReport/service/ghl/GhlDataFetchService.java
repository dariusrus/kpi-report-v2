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
import java.util.List;

import static com.blc.kpiReport.util.DateUtil.formatDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class GhlDataFetchService {

    private static final String GHL_API_BASE_URL = "https://services.leadconnectorhq.com";
    private static final String OPPORTUNITY_URL_TEMPLATE = "%s/opportunities/search?location_id=%s&endDate=%s&date=%s&page=%d&limit=100";
    private static final String EVENTS_URL_TEMPLATE = "%s/calendars/events?locationId=%s&calendarId=%s&startTime=%d&endTime=%d";
    private static final String PIPELINE_URL_TEMPLATE = "%s/opportunities/pipelines?locationId=%s";
    private static final String CALENDAR_URL_TEMPLATE = "%s/calendars/?locationId=%s";

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final RetryTemplate retryTemplate;
    private final GhlTokenService tokenService;

    public GhlApiData getGhlData(GhlLocation location, String startDate, String endDate) throws GhlApiException {
        log.info("Fetching GHL data for location ID: {}, from {} to {}", location.getLocationId(), startDate, endDate);

        location = tokenService.getAccessToken(location);
        var locationId = location.getLocationId();
        var accessToken = location.getAccessToken();
        log.debug("Obtained access token for location ID: {}", locationId);

        try {
            var opportunityList = fetchOpportunities(locationId, accessToken, startDate, endDate);
            var eventsJson = fetchEvents(locationId, accessToken, startDate, endDate);
            var pipelineJson = fetchPipelineStages(accessToken, locationId);

            log.info("Successfully fetched GHL data for location ID: {}", locationId);
            return GhlApiData.builder()
                .opportunityList(opportunityList)
                .eventsJson(eventsJson)
                .pipelineJson(pipelineJson)
                .build();
        } catch (IOException e) {
            log.error("Failed to fetch GHL data: {}", e.getMessage());
            throw new GhlApiException("Failed to fetch GHL data after multiple attempts", e);
        }
    }

    private List<JsonNode> fetchOpportunities(String locationId, String accessToken, String startDate, String endDate) throws IOException {
        return retryTemplate.execute(context -> {
            log.info("Attempt {} to fetch opportunities for location ID: {}, from {} to {}", context.getRetryCount() + 1, locationId, startDate, endDate);

            var allOpportunities = new ArrayList<JsonNode>();
            var formattedStartDate = formatDate(startDate);
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
                    opportunities.forEach(allOpportunities::add);

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

    private List<JsonNode> fetchEvents(String locationId, String accessToken, String startTime, String endTime) throws IOException {
        return retryTemplate.execute(context -> {
            log.info("Attempt {} to fetch events for location ID: {}, from {} to {}", context.getRetryCount() + 1, locationId, startTime, endTime);

            LocalDate startDate = LocalDate.parse(startTime);
            LocalDate endDate = LocalDate.parse(endTime);
            long epochStartTime = startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000; // milliseconds
            long epochEndTime = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(); // milliseconds
            List<String> calendarIds = fetchCalendarIds(locationId, accessToken);
            List<JsonNode> events = new ArrayList<>();

            for (String calendarId : calendarIds) {
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
                    events.add(eventsJson);
                }
            }
            log.info("Successfully fetched {} events for location ID: {}", events.size(), locationId);
            return events;
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

    private List<String> fetchCalendarIds(String locationId, String accessToken) throws IOException {
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
                List<String> calendarIds = new ArrayList<>();
                for (JsonNode calendarNode : calendarJson.path("calendars")) {
                    calendarIds.add(calendarNode.path("id").asText());
                }
                log.info("Successfully fetched {} calendar IDs for location ID: {}", calendarIds.size(), locationId);
                return calendarIds;
            }
        });
    }
}
