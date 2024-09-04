package com.blc.kpiReport.service;

import com.blc.kpiReport.models.response.GhlLocationFullResponse;
import com.blc.kpiReport.models.response.GhlLocationSummaryResponse;
import com.blc.kpiReport.repository.GhlLocationRepository;
import com.blc.kpiReport.schema.GhlLocation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GhlLocationService {

    private final GhlLocationRepository ghlLocationRepository;

    public GhlLocationService(GhlLocationRepository ghlLocationRepository) {
        this.ghlLocationRepository = ghlLocationRepository;
    }

    public GhlLocation findByLocationId(String locationId) {
        return ghlLocationRepository.findByLocationId(locationId);
    }

    public GhlLocationFullResponse findByLocationIdResponse(String locationId) {
        return Optional.ofNullable(ghlLocationRepository.findByLocationId(locationId))
            .map(this::toFullResponse)
            .orElse(null);
    }

    public GhlLocation save(GhlLocation ghlLocation) {
        return ghlLocationRepository.save(ghlLocation);
    }

    public Optional<GhlLocation> update(String locationId, GhlLocation updatedGhlLocation) {
        GhlLocation existingLocation = ghlLocationRepository.findByLocationId(locationId);
        if (existingLocation != null) {
            existingLocation.setLocationId(updatedGhlLocation.getLocationId());
            existingLocation.setGaAccountId(updatedGhlLocation.getGaAccountId());
            existingLocation.setGaPropertyId(updatedGhlLocation.getGaPropertyId());
            existingLocation.setGaCountryCode(updatedGhlLocation.getGaCountryCode());
            existingLocation.setName(updatedGhlLocation.getName());
            existingLocation.setGhlAccessToken(updatedGhlLocation.getGhlAccessToken());
            existingLocation.setGhlRefreshToken(updatedGhlLocation.getGhlRefreshToken());
            existingLocation.setGhlTokenScope(updatedGhlLocation.getGhlTokenScope());
            existingLocation.setGhlTokenDate(updatedGhlLocation.getGhlTokenDate());
            existingLocation.setMcApiToken(updatedGhlLocation.getMcApiToken());
            existingLocation.setClientType(updatedGhlLocation.getClientType());
            ghlLocationRepository.save(existingLocation);
            return Optional.of(existingLocation);
        } else {
            return Optional.empty();
        }
    }

    public List<GhlLocationSummaryResponse> findAll() {
        return ghlLocationRepository.findAll().stream()
            .map(this::toSummaryResponse)
            .collect(Collectors.toList());
    }

    private GhlLocationFullResponse toFullResponse(GhlLocation ghlLocation) {
        return GhlLocationFullResponse.builder()
            .id(ghlLocation.getId())
            .locationId(ghlLocation.getLocationId())
            .gaAccountId(ghlLocation.getGaAccountId())
            .gaPropertyId(ghlLocation.getGaPropertyId())
            .gaCountryCode(ghlLocation.getGaCountryCode())
            .name(ghlLocation.getName())
            .ghlAccessToken(ghlLocation.getGhlAccessToken())
            .ghlRefreshToken(ghlLocation.getGhlRefreshToken())
            .ghlTokenScope(ghlLocation.getGhlTokenScope())
            .ghlTokenDate(ghlLocation.getGhlTokenDate())
            .mcApiToken(ghlLocation.getMcApiToken())
            .clientType(ghlLocation.getClientType())
            .build();
    }

    private GhlLocationSummaryResponse toSummaryResponse(GhlLocation ghlLocation) {
        return GhlLocationSummaryResponse.builder()
            .id(ghlLocation.getId())
            .locationId(ghlLocation.getLocationId())
            .gaAccountId(ghlLocation.getGaAccountId())
            .gaPropertyId(ghlLocation.getGaPropertyId())
            .gaCountryCode(ghlLocation.getGaCountryCode())
            .name(ghlLocation.getName())
            .ghlAccessTokenIsSet(ghlLocation.getGhlAccessToken() != null)
            .ghlRefreshTokenIsSet(ghlLocation.getGhlRefreshToken() != null)
            .mcApiTokenIsSet(ghlLocation.getMcApiToken() != null)
            .clientType(ghlLocation.getClientType())
            .build();
    }
}