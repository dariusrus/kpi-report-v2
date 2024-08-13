package com.blc.kpiReport.repository;

import com.blc.kpiReport.schema.MonthlyAverage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonthlyAverageRepository extends JpaRepository<MonthlyAverage, Long> {
    Optional<MonthlyAverage> findByMonthAndYear(int month, int year);
}