package com.blc.kpiReport.service;

import com.blc.kpiReport.models.pojo.openai.MonthlyPrompt;
import com.blc.kpiReport.models.request.GenerateKpiReportByLocationRequest;
import com.blc.kpiReport.models.response.GenerateKpiReportResponse;
import com.blc.kpiReport.models.response.KpiReportResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIGeneratorService {

    @Value("${openai.api.secret-key}")
    private String apiKey;

    @Value("${openai.api.base-url}")
    private String baseUrl;

    @Value("${openai.api.model}")
    private String model;

    @Value("${openai.api.max-tokens}")
    private int maxTokens;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final KpiReportRetrievalService kpiReportRetrievalService;

    public CompletableFuture<GenerateKpiReportResponse> generateOpenAISummary(GenerateKpiReportByLocationRequest request) {
        var jsonPrompt = generateJsonPrompt(request);
        return null;
    }

    private String generateJsonPrompt(GenerateKpiReportByLocationRequest request) {
        var kpiReport = kpiReportRetrievalService.getKpiReport(request.getGhlLocationId(), request.getMonth(), request.getMonth());
        var monthlyPrompt = generateMonthlyPrompt(kpiReport);
        return null;
    }

    private Object generateMonthlyPrompt(KpiReportResponse kpiReport) {
        var monthlyPrompt = MonthlyPrompt.builder()
                .subAgency(kpiReport.getSubAgency())
                .ghlLocationId(kpiReport.getGhlLocationId())
                .country(kpiReport.getCountry())
                .monthAndYear(kpiReport.getMonthAndYear())
                .clientType(kpiReport.getClientType())
                .build();
        return monthlyPrompt;
    }

    public String chatGPTRequest(String prompt) throws IOException {
        log.info("Preparing request for OpenAI API...");

        String data = "We are a SAAS company helping builders get good quality leads and ultimately convert them into sales. We have developed an in-house application that gathers data for each month a client uses our service. An example of monthly data is the following: {\"subAgency\":\"Regency Homes\",\"ghlLocationId\":\"lWZrtj8byFZrUI9Cg2eX\",\"country\":\"United States\",\"monthAndYear\":\"December, 2024\",\"clientType\":\"CUSTOM_HOMES\",\"googleAnalytics\":{\"totalUniqueSiteVisitors\":988,\"industryAverageUniqueSiteVisitors\":1000,\"previousMonthsTotalUniqueSiteVisitors\":[{\"monthAndYear\":\"November, 2024\",\"totalUniqueSiteVisitors\":900},{\"monthAndYear\":\"October, 2024\",\"totalUniqueSiteVisitors\":843},{\"monthAndYear\":\"August, 2024\",\"totalUniqueSiteVisitors\":887},{\"monthAndYear\":\"September, 2024\",\"totalUniqueSiteVisitors\":721}],\"topCities\":[{\"city\":\"Omaha\",\"uniqueSiteVisitors\":289},{\"city\":\"Chicago\",\"uniqueSiteVisitors\":207},{\"city\":\"(not set)\",\"uniqueSiteVisitors\":55},{\"city\":\"La Vista\",\"uniqueSiteVisitors\":45},{\"city\":\"Dallas\",\"uniqueSiteVisitors\":27}]},\"websiteAnalytics\":{\"deviceClarityAggregate\":[{\"deviceName\":\"PC\",\"collectiveAverageScrollDepth\":50.84,\"totalSessions\":2368,\"totalActiveTime\":105612},{\"deviceName\":\"Email\",\"collectiveAverageScrollDepth\":29.5,\"totalSessions\":2,\"totalActiveTime\":93},{\"deviceName\":\"Tablet\",\"collectiveAverageScrollDepth\":49.23,\"totalSessions\":482,\"totalActiveTime\":23253},{\"deviceName\":\"Mobile\",\"collectiveAverageScrollDepth\":40.83,\"totalSessions\":4000,\"totalActiveTime\":45453}],\"topTenUrls\":[{\"id\":1604602,\"url\":\"https://regencyhomesomaha.com/11912-s-113-st/\",\"devices\":[{\"id\":1604652,\"deviceType\":\"PC\",\"averageScrollDepth\":48.21,\"totalTime\":28189,\"activeTime\":27731,\"totalSessionCount\":30},{\"id\":1604653,\"deviceType\":\"Tablet\",\"averageScrollDepth\":41.02,\"totalTime\":1353,\"activeTime\":342,\"totalSessionCount\":10},{\"id\":1604654,\"deviceType\":\"Mobile\",\"averageScrollDepth\":38.44,\"totalTime\":1585,\"activeTime\":498,\"totalSessionCount\":50}],\"averageScrollDepth\":48.21,\"activeTime\":27731,\"totalSessionCount\":30},{\"id\":1604603,\"url\":\"https://regencyhomesomaha.com/sterling-1893/\",\"devices\":[{\"id\":1604655,\"deviceType\":\"PC\",\"averageScrollDepth\":48.67,\"totalTime\":9361,\"activeTime\":7963,\"totalSessionCount\":30},{\"id\":1604656,\"deviceType\":\"Tablet\",\"averageScrollDepth\":42.42,\"totalTime\":281,\"activeTime\":261,\"totalSessionCount\":6},{\"id\":1604657,\"deviceType\":\"Mobile\",\"averageScrollDepth\":21.12,\"totalTime\":960,\"activeTime\":529,\"totalSessionCount\":37}],\"averageScrollDepth\":48.67,\"activeTime\":7963,\"totalSessionCount\":30},{\"id\":1604604,\"url\":\"https://regencyhomesomaha.com/communities/\",\"devices\":[{\"id\":1604658,\"deviceType\":\"PC\",\"averageScrollDepth\":41.96,\"totalTime\":3804,\"activeTime\":2672,\"totalSessionCount\":106},{\"id\":1604659,\"deviceType\":\"Tablet\",\"averageScrollDepth\":49.7,\"totalTime\":1887,\"activeTime\":1880,\"totalSessionCount\":12},{\"id\":1604660,\"deviceType\":\"Mobile\",\"averageScrollDepth\":35.93,\"totalTime\":5486,\"activeTime\":1649,\"totalSessionCount\":141}],\"averageScrollDepth\":41.96,\"activeTime\":2672,\"totalSessionCount\":106},{\"id\":1604605,\"url\":\"https://regencyhomesomaha.com/sandlewood-1756/\",\"devices\":[{\"id\":1604661,\"deviceType\":\"PC\",\"averageScrollDepth\":48.09,\"totalTime\":4741,\"activeTime\":3887,\"totalSessionCount\":27},{\"id\":1604662,\"deviceType\":\"Tablet\",\"averageScrollDepth\":36.85,\"totalTime\":1333,\"activeTime\":1029,\"totalSessionCount\":10},{\"id\":1604663,\"deviceType\":\"Mobile\",\"averageScrollDepth\":19.41,\"totalTime\":977,\"activeTime\":448,\"totalSessionCount\":52}],\"averageScrollDepth\":48.09,\"activeTime\":3887,\"totalSessionCount\":27},{\"id\":1604606,\"url\":\"https://regencyhomesomaha.com/patriot-1690/\",\"devices\":[{\"id\":1604664,\"deviceType\":\"PC\",\"averageScrollDepth\":54.57,\"totalTime\":3525,\"activeTime\":2607,\"totalSessionCount\":26},{\"id\":1604665,\"deviceType\":\"Tablet\",\"averageScrollDepth\":48.43,\"totalTime\":1049,\"activeTime\":741,\"totalSessionCount\":10},{\"id\":1604666,\"deviceType\":\"Mobile\",\"averageScrollDepth\":30.65,\"totalTime\":3305,\"activeTime\":1699,\"totalSessionCount\":89}],\"averageScrollDepth\":54.57,\"activeTime\":2607,\"totalSessionCount\":26},{\"id\":1604607,\"url\":\"https://regencyhomesomaha.com/available-homes/\",\"devices\":[{\"id\":1604667,\"deviceType\":\"PC\",\"averageScrollDepth\":48.78,\"totalTime\":7267,\"activeTime\":2073,\"totalSessionCount\":243},{\"id\":1604668,\"deviceType\":\"Tablet\",\"averageScrollDepth\":42.42,\"totalTime\":2152,\"activeTime\":1284,\"totalSessionCount\":51},{\"id\":1604669,\"deviceType\":\"Mobile\",\"averageScrollDepth\":29.01,\"totalTime\":2739,\"activeTime\":1320,\"totalSessionCount\":412}],\"averageScrollDepth\":48.78,\"activeTime\":2073,\"totalSessionCount\":243},{\"id\":1604608,\"url\":\"https://regencyhomesomaha.com/hamilton-1512/\",\"devices\":[{\"id\":1604670,\"deviceType\":\"PC\",\"averageScrollDepth\":55.85,\"totalTime\":2271,\"activeTime\":1971,\"totalSessionCount\":23},{\"id\":1604671,\"deviceType\":\"Tablet\",\"averageScrollDepth\":52.86,\"totalTime\":1351,\"activeTime\":590,\"totalSessionCount\":6},{\"id\":1604672,\"deviceType\":\"Mobile\",\"averageScrollDepth\":45.25,\"totalTime\":7269,\"activeTime\":1563,\"totalSessionCount\":72}],\"averageScrollDepth\":55.85,\"activeTime\":1971,\"totalSessionCount\":23},{\"id\":1604609,\"url\":\"https://regencyhomesomaha.com/northbrook-2657/\",\"devices\":[{\"id\":1604673,\"deviceType\":\"PC\",\"averageScrollDepth\":57.15,\"totalTime\":3118,\"activeTime\":2500,\"totalSessionCount\":21},{\"id\":1604674,\"deviceType\":\"Tablet\",\"averageScrollDepth\":51,\"totalTime\":165,\"activeTime\":165,\"totalSessionCount\":1},{\"id\":1604675,\"deviceType\":\"Mobile\",\"averageScrollDepth\":29.35,\"totalTime\":4308,\"activeTime\":1117,\"totalSessionCount\":57}],\"averageScrollDepth\":57.15,\"activeTime\":2500,\"totalSessionCount\":21},{\"id\":1604610,\"url\":\"https://regencyhomesomaha.com/washington-1782/\",\"devices\":[{\"id\":1604676,\"deviceType\":\"PC\",\"averageScrollDepth\":49.02,\"totalTime\":2114,\"activeTime\":1363,\"totalSessionCount\":25},{\"id\":1604677,\"deviceType\":\"Tablet\",\"averageScrollDepth\":37,\"totalTime\":332,\"activeTime\":332,\"totalSessionCount\":11},{\"id\":1604678,\"deviceType\":\"Mobile\",\"averageScrollDepth\":33.48,\"totalTime\":2562,\"activeTime\":1319,\"totalSessionCount\":64}],\"averageScrollDepth\":49.02,\"activeTime\":1363,\"totalSessionCount\":25},{\"id\":1604611,\"url\":\"https://regencyhomesomaha.com/available-homes/?e-filter-63fa159-category=available-ranchs\",\"devices\":[{\"id\":1604679,\"deviceType\":\"PC\",\"averageScrollDepth\":50.22,\"totalTime\":2968,\"activeTime\":1300,\"totalSessionCount\":35},{\"id\":1604680,\"deviceType\":\"Tablet\",\"averageScrollDepth\":63.1,\"totalTime\":737,\"activeTime\":412,\"totalSessionCount\":12},{\"id\":1604681,\"deviceType\":\"Mobile\",\"averageScrollDepth\":49.26,\"totalTime\":2215,\"activeTime\":1150,\"totalSessionCount\":100}],\"averageScrollDepth\":50.22,\"activeTime\":1300,\"totalSessionCount\":35}]},\"goHighLevel\":{\"totalLeadsCaptured\":46,\"industryAverageTotalLeadsCaptured\":25,\"previousMonthTotalLeadsCaptured\":[{\"monthAndYear\":\"November, 2024\",\"totalLeadsCaptured\":54},{\"monthAndYear\":\"October, 2024\",\"totalLeadsCaptured\":42},{\"monthAndYear\":\"August, 2024\",\"totalLeadsCaptured\":37},{\"monthAndYear\":\"September, 2024\",\"totalLeadsCaptured\":30}],\"totalWebsiteLeadsCaptured\":34,\"totalManualUserInputLeadsCaptured\":12,\"topLeadSources\":[{\"leadSource\":\"Planning Guide Form\",\"count\":12},{\"leadSource\":\"Scope, Budget, and Booking Wizard\",\"count\":4}],\"topSessionChannels\":[{\"sessionChannel\":\"Direct traffic\",\"count\":22},{\"sessionChannel\":\"Paid Social\",\"count\":20}],\"newLeadAppointments\":[{\"scheduledACallCount\":8,\"noScheduledCallsCount\":35}],\"pipelineStageConversions\":{\"totalStageConversions\":66,\"totalFollowups\":89,\"salesPersonBreakdown\":[{\"salesPersonName\":\"Bruce Campora\",\"stageConversions\":17,\"followUps\":16},{\"salesPersonName\":\"Rosie Camaj\",\"stageConversions\":11,\"followUps\":15},{\"salesPersonName\":\"Sean Plunkett\",\"stageConversions\":9,\"followUps\":37},{\"salesPersonName\":\"Sue Dowie\",\"stageConversions\":9,\"followUps\":12},{\"salesPersonName\":\"Ron Siebert\",\"stageConversions\":8,\"followUps\":2}]},\"contactsWon\":[{\"contactName\":\"Mike & Michelle Homme\",\"sessionChannel\":\"CRM UI\"},{\"contactName\":\"Thangaraj Paulpandi\",\"sessionChannel\":\"Direct traffic, CRM Workflows\"},{\"contactName\":\"Eric & Paulette Dernovish\",\"sessionChannel\":\"CRM Workflows, CRM UI\"},{\"contactName\":\"Aaron and Jessica  Parson\",\"sessionChannel\":\"Direct traffic\"}]}}. SubAgency refers to the name of a builder, this would be one of our clients. ClientType can be categorized as CUSTOM_HOMES, REMODELING, or CUSTOM_HOMES_AND_REMODELING. They are categorized as such as to provide distinction when we do the comparison to an \"Industry Average\". GoogleAnalytics: totalUniqueSiteVisitors refers to the amount of people who visited and initiated a session for the first time for a builder's site; industryAverageUniqueSiteVisitors refers to the average, based on client type, of all builders under that client type; previousMonthsTotalUniqueSiteVisitors refers to the previous months' data; topCities are the top 5 cities by unique site visitors. WebsiteAnalytics: deviceClarityAggregate contains data on which devices users access the site; topTenUrls refers to the top 10 URLs by totalTime. You are a business analyst, and your task is to provide an \"Executive Summary\" on how a builder's website performed this month based on the data provided to you. It is important to point out the contacts won on which session channels, trends on monthly visitors, highlight the comparison to the industry average, analyze which device is performing the best, and from which cities have the best chance to land a lead. The goal of the executive summary is to identify how leads are being captured and how salespeople are performing to get those leads converted. Indicate the number of contacts won for the month. Session channels that bring in the most leads and top lead sources must be emphasized, as well as the contacts won. The response must be concise, easy to read, and digest in under a minute, and be titled \"Executive Summary\" and ready to be put in a report (therefore, no underscores, percentages must be rounded to up to two decimals if necessary, formatted as concise and user-friendly as possible). A sample of a response would be something like: For December 2024, 4 contacts were won, primarily through CRM Workflows and Direct Traffic. Regency Homes attracted 988 unique site visitors, slightly below the industry average of 1000 for Custom Homes builders. Visitor trends show a steady increase compared to November (900) and October (843). Top-performing cities include Omaha (289 visitors) and Chicago (207 visitors), presenting strong lead opportunities. PC devices led engagement with 2368 sessions, an average scroll depth of 50.84, and 29.33 hours of active time, outperforming Mobile and Tablet users. The top URL was \"11912 S 113 St\" with 30 sessions and 7 hours, 42 minutes, and 11 seconds of active time, primarily accessed via PC. Lead capture exceeded industry norms with 46 total leads captured (industry average: 25). Most leads originated from the Planning Guide Form (12 leads) and Direct Traffic (22 sessions). Bruce Campora led salesperson performance with 17 stage conversions and 16 follow-ups, contributing significantly to overall performance.";
        prompt = data;
        String url = baseUrl + "/chat/completions";

        log.debug("API URL: {}", url);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "max_tokens", maxTokens
        );

        String jsonBody = objectMapper.writeValueAsString(requestBody);
        log.debug("Request payload: {}", jsonBody);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonBody, MediaType.get("application/json")))
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("API call failed with status: {} and body: {}", response.code(), response.body().string());
                throw new IOException("Unexpected response code: " + response.code());
            }

            String responseBody = response.body().string();
            log.info("API call successful. Response received.");

            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            String formattedContent = (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");

            log.info("Formatted Response Content:\n{}", formattedContent);

            return formattedContent;
        } catch (IOException e) {
            log.error("Error during API call: {}", e.getMessage(), e);
            throw e;
        }

    }
}
