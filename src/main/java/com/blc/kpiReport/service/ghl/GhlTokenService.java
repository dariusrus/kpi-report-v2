package com.blc.kpiReport.service.ghl;

import com.blc.kpiReport.config.GoHighLevelProperties;
import com.blc.kpiReport.models.pojo.ghl.GhlOauthToken;
import com.blc.kpiReport.schema.GhlLocation;
import com.blc.kpiReport.service.GhlLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.Duration;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GhlTokenService {

    private static final String TOKEN_URL = "https://services.leadconnectorhq.com/oauth/token";

    private final GoHighLevelProperties goHighLevelProperties;
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final GhlLocationService ghlLocationService;
    private final RetryTemplate retryTemplate;

    public GhlLocation getAccessToken(GhlLocation ghlLocation) {
        try {
            if (ghlLocation.getGhlTokenDate() == null || isTokenExpired(ghlLocation.getGhlTokenDate())) {
                log.info("Access Token has expired for {}", ghlLocation.getName());
                return refreshAccessToken(ghlLocation);
            } else {
                return ghlLocation;
            }
        } catch (IOException exception) {
            log.error("Failed to secure access token for {}", ghlLocation.getName());
        }
        return ghlLocation;
    }

    private boolean isTokenExpired(Instant tokenDate) {
        return Duration.between(tokenDate, Instant.now()).toDays() >= 1;
    }

    private GhlLocation refreshAccessToken(GhlLocation ghlLocation) throws IOException {
        return retryTemplate.execute(context -> {
            log.info("Attempt {} to refresh access token for GhlLocation: {}", context.getRetryCount() + 1, ghlLocation.getName());

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType,
                "client_id=" + goHighLevelProperties.getClientId() +
                    "&client_secret=" + goHighLevelProperties.getClientSecret() +
                    "&grant_type=refresh_token" +
                    "&refresh_token=" + ghlLocation.getGhlRefreshToken() +
                    "&user_type=Location" +
                    "&redirect_uri=");

            Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();

            log.debug("Sending request to refresh access token. URL: {}", TOKEN_URL);

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    JsonNode jsonResponse = objectMapper.readTree(response.body().string());
                    String newAccessToken = jsonResponse.get("access_token").asText();
                    String newRefreshToken = jsonResponse.get("refresh_token").asText();
                    Instant now = Instant.now();

                    ghlLocation.setGhlAccessToken(newAccessToken);
                    ghlLocation.setGhlRefreshToken(newRefreshToken);

                    ghlLocation.setGhlTokenDate(now);

                    log.info("Successfully refreshed access token for GhlLocation: {}", ghlLocation.getName());
                    return ghlLocationService.save(ghlLocation);
                } else {
                    String errorMessage = "Failed to refresh access token: " + response.message();
                    log.error(errorMessage);
                    throw new IOException(errorMessage);
                }
            }
        });
    }

    public Optional<GhlOauthToken> getToken(String code) throws IOException {

        log.debug("Request Access Token with code: {}", code);

        OkHttpClient client = new OkHttpClient()
            .newBuilder()
            .build();

        // Create the request body
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/x-www-form-urlencoded");


        StringBuffer bodyParams = new StringBuffer();
        bodyParams.append("client_id=").append(goHighLevelProperties.getClientId()).append("&");
        bodyParams.append("client_secret=").append(goHighLevelProperties.getClientSecret()).append("&");
        bodyParams.append("grant_type=authorization_code").append("&");
        bodyParams.append("code=").append(code);

        RequestBody body = RequestBody.Companion.create(bodyParams.toString(), mediaType);

        // Build the request object, with method, headers
        Request okhttpRequest = new Request.Builder().url(TOKEN_URL)
            .method("POST", body).build();

        // Perform the request, this potentially throws an IOException
        GhlOauthToken ghlOauthToken = null;
        try (Response okhttpResponse = client.newCall(okhttpRequest).execute()) {
            String ghlOauthTokenString = okhttpResponse.body().string();
            ghlOauthToken = objectMapper.readValue(ghlOauthTokenString, GhlOauthToken.class);

            log.debug("------------- response --------------");
            log.debug( ghlOauthToken.toString() );
            log.debug("-----------------------------");
            log.debug( ghlOauthTokenString );
            log.debug("-----------------------------");
        } finally {
            log.debug("------------- DONE ----------------");
        }
        return Optional.of(ghlOauthToken);
    }
}
