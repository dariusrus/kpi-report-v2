package com.blc.kpiReport.service;

import com.blc.kpiReport.schema.GhlLocation;
import com.blc.kpiReport.repository.GhlLocationRepository;
import org.springframework.stereotype.Service;

@Service
public class GhlLocationService {

    private final GhlLocationRepository ghlLocationRepository;

    public GhlLocationService(GhlLocationRepository ghlLocationRepository) {
        this.ghlLocationRepository = ghlLocationRepository;
    }

    public GhlLocation findByLocationId(String locationId) {
        return ghlLocationRepository.findByLocationId(locationId);
    }

    public GhlLocation save(GhlLocation ghlLocation) {
        return ghlLocationRepository.save(ghlLocation);
    }
}
