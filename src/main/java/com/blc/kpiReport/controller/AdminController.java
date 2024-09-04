package com.blc.kpiReport.controller;

import com.blc.kpiReport.config.GhlLocationsToGenerateProperties;
import com.blc.kpiReport.models.ClientType;
import com.blc.kpiReport.models.response.GhlLocationFullResponse;
import com.blc.kpiReport.models.response.GhlLocationSummaryResponse;
import com.blc.kpiReport.schema.GhlLocation;
import com.blc.kpiReport.service.GhlLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "GHL Location Admin Endpoints", description = "Manage GHL locations for KPI report generation")
public class AdminController {

    private final GhlLocationService ghlLocationService;
    private GhlLocationsToGenerateProperties ghlLocationsToGenerateProperties;

    @Operation(
        summary = "Get all GHL location IDs that will run during batch.",
        description = "Fetches the list of all GHL location IDs that are configured to run during the batch process.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved the list of GHL location IDs",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/batch/ghl-location-ids")
    public List<String> getAllGhlLocationIds() {
        return ghlLocationsToGenerateProperties.getGhlLocationIds();
    }

    @Operation(
        summary = "Add new GHL location IDs (comma separated) to run during batch.",
        description = "Adds new GHL location IDs to the list of locations that will be processed during the batch run. The IDs should be provided as a comma-separated string.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully added the new GHL location IDs",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @PostMapping("/batch/ghl-location-ids")
    public List<String> addGhlLocationIds(@RequestParam String ids) {
        List<String> newIds = Arrays.asList(ids.split(","));
        List<String> currentIds = ghlLocationsToGenerateProperties.getGhlLocationIds();
        currentIds.addAll(newIds);
        return currentIds;
    }

    @Operation(
        summary = "Remove a GHL location from the batch run by ID.",
        description = "Removes a specific GHL location ID from the list of locations that will be processed during the batch run.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully removed the GHL location ID",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(responseCode = "404", description = "GHL location ID not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @DeleteMapping("/batch/ghl-location-ids/{id}")
    public List<String> removeGhlLocationId(@PathVariable String id) {
        List<String> currentIds = ghlLocationsToGenerateProperties.getGhlLocationIds();
        if (!currentIds.remove(id)) {
            throw new IllegalArgumentException("GHL location ID not found: " + id);
        }
        return currentIds;
    }

    @Operation(
        summary = "Create a new GHL location.",
        description = "Creates a new GHL location with the provided details.",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Successfully created the GHL location",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GhlLocation.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @PostMapping("/ghl-location")
    public ResponseEntity<GhlLocation> createGhlLocation(
        @RequestParam String locationId,
        @RequestParam String name,
        @RequestParam(required = false) String gaAccountId,
        @RequestParam(required = false) String gaPropertyId,
        @RequestParam(required = false) String gaCountryCode,
        @RequestParam(required = false) String ghlAccessToken,
        @RequestParam(required = false) String ghlRefreshToken,
        @RequestParam(required = false) String ghlTokenScope,
        @RequestParam(required = false) Instant ghlTokenDate,
        @RequestParam(required = false) String mcApiToken,
        @RequestParam(required = false) ClientType clientType) {

        GhlLocation ghlLocation = GhlLocation.builder()
            .locationId(locationId)
            .gaAccountId(gaAccountId)
            .gaPropertyId(gaPropertyId)
            .gaCountryCode(gaCountryCode)
            .name(name)
            .ghlAccessToken(ghlAccessToken)
            .ghlRefreshToken(ghlRefreshToken)
            .ghlTokenScope(ghlTokenScope)
            .ghlTokenDate(ghlTokenDate)
            .mcApiToken(mcApiToken)
            .clientType(clientType)
            .build();

        return ResponseEntity.status(201).body(ghlLocationService.save(ghlLocation));
    }

    @Operation(
        summary = "Update an existing GHL location by location ID.",
        description = "Updates the details of an existing GHL location by its location ID.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully updated the GHL location",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GhlLocation.class))
            ),
            @ApiResponse(responseCode = "404", description = "GHL location not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @PutMapping("/ghl-location/{locationId}")
    public ResponseEntity<GhlLocation> updateGhlLocation(
        @PathVariable String locationId,
        @RequestParam String name,
        @RequestParam(required = false) String gaAccountId,
        @RequestParam(required = false) String gaPropertyId,
        @RequestParam(required = false) String gaCountryCode,
        @RequestParam(required = false) String ghlAccessToken,
        @RequestParam(required = false) String ghlRefreshToken,
        @RequestParam(required = false) String ghlTokenScope,
        @RequestParam(required = false) Instant ghlTokenDate,
        @RequestParam(required = false) String mcApiToken,
        @RequestParam(required = false) ClientType clientType) {

        GhlLocation updatedGhlLocation = GhlLocation.builder()
            .locationId(locationId)
            .gaAccountId(gaAccountId)
            .gaPropertyId(gaPropertyId)
            .gaCountryCode(gaCountryCode)
            .name(name)
            .ghlAccessToken(ghlAccessToken)
            .ghlRefreshToken(ghlRefreshToken)
            .ghlTokenScope(ghlTokenScope)
            .ghlTokenDate(ghlTokenDate)
            .mcApiToken(mcApiToken)
            .clientType(clientType)
            .build();

        return ghlLocationService.update(locationId, updatedGhlLocation)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Get a GHL location by location ID.",
        description = "Fetches a GHL location by its location ID.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved the GHL location",
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(responseCode = "404", description = "GHL location not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping(value = "/ghl-locations/{locationId}")
    public GhlLocationFullResponse getGhlLocationById(@PathVariable String locationId) {
        GhlLocationFullResponse ghlLocation = ghlLocationService.findByLocationIdResponse(locationId);
        if (ghlLocation != null) {
            return ghlLocation;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "GHL location not found");
        }
    }

    @Operation(
        summary = "Get a list of all GHL locations.",
        description = "Fetches a list of all GHL locations.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved the list of GHL locations",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GhlLocationSummaryResponse.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/ghl-locations")
    public List<GhlLocationSummaryResponse> getAllGhlLocations() {
        return ghlLocationService.findAll();
    }
}