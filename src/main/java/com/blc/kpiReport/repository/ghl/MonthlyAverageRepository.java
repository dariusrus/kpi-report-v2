package com.blc.kpiReport.repository.ghl;

import com.blc.kpiReport.models.ClientType;
import com.blc.kpiReport.schema.MonthlyAverage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonthlyAverageRepository extends JpaRepository<MonthlyAverage, Long> {
    Optional<MonthlyAverage> findByMonthAndYearAndClientType(int month, int year, ClientType clientType);
}