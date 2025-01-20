package com.blc.kpiReport.controller;

import com.blc.kpiReport.models.pojo.ghl.GhlOauthToken;
import com.blc.kpiReport.repository.GhlLocationRepository;
import com.blc.kpiReport.schema.GhlLocation;
import com.blc.kpiReport.service.ghl.GhlTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/pub")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "GoHighLevel OAuth2 Token Management API", description = "Endpoints used for handling and securing GoHighLevel OAuth2 tokens.")
public class GhlOauth2Controller {

    private final ObjectMapper objectMapper;
    private final GhlLocationRepository ghlLocationRepository;
    private final GhlTokenService ghlTokenService;

    @Operation(
        summary = "Handle GoHighLevel OAuth2 callback and process the authorization code.",
        description = "This endpoint processes the GoHighLevel OAuth2 authorization code, exchanges it for an access token, and securely stores it in the database.",
        parameters = {
            @Parameter(name = "code", description = "The authorization code returned from GoHighLevel OAuth2 provider", required = true)
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Authorization successful",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid or missing authorization code"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping(path = { "/oauth/callback" })
    public String getAuthorizationCode(
        HttpServletResponse response,
        @RequestParam("code") Optional<String> code
    ) throws IOException {
        log.info("Received OAuth callback request with code: {}", code.orElse("No code provided"));

        String codeParam = null;
        boolean success = false;

        if (code.isPresent()) {
            codeParam = code.get();
            log.debug("Authorization code received: {}", codeParam);

            Optional<GhlOauthToken> ghlOauthTokenOptional = ghlTokenService.getToken(codeParam);
            if (ghlOauthTokenOptional.isPresent()) {
                GhlOauthToken ghlOauthToken = ghlOauthTokenOptional.get();
                log.info("Token retrieved successfully for code: {}", codeParam);

                String locationId = ghlOauthToken.getLocationId();
                if (StringUtils.isEmpty(locationId) && StringUtils.isNotEmpty(ghlOauthToken.getCompanyId())) {
                    locationId = "admin";
                    log.debug("Location ID is empty. Using 'admin' as the default location ID.");
                }

                GhlLocation ghlLocation = ghlLocationRepository.findByLocationId(locationId);
                if (ObjectUtils.isNotEmpty(ghlLocation)) {
                    ghlLocation.setGhlAccessToken(ghlOauthToken.getAccessToken());
                    ghlLocation.setGhlRefreshToken(ghlOauthToken.getRefreshToken());
                    ghlLocation.setGhlTokenScope(ghlOauthToken.getScope());
                    ghlLocation.setGhlTokenDate(Instant.now());

                    ghlLocationRepository.saveAndFlush(ghlLocation);
                    log.info("Token information saved successfully for location ID: {}", locationId);
                    success = true;
                } else {
                    log.warn("No location found with ID: {}", locationId);
                }
            } else {
                log.warn("Failed to retrieve token for authorization code: {}", codeParam);
            }
        } else {
            log.warn("Authorization code not provided in the request.");
        }

        if (success) {
            log.info("Authorization successful for code: {}", codeParam);
            return "AUTHORIZATION SUCCESSFUL";
        } else {
            log.error("Authorization failed for code: {}", codeParam != null ? codeParam : "No code");
            return "AUTHORIZATION FAILED!";
        }
    }
}