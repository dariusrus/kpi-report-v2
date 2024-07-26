package com.blc.kpiReport.repository;

import com.blc.kpiReport.schema.GhlLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GhlLocationRepository extends JpaRepository<GhlLocation, Long> {
    GhlLocation findByLocationId(String locationId);

    List<GhlLocation> findAllByLocationIdIn(List<String> locationIds);
}
