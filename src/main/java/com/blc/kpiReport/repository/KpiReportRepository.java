package com.blc.kpiReport.repository;

import com.blc.kpiReport.schema.KpiReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KpiReportRepository extends JpaRepository<KpiReport, Long> {
    Optional<KpiReport> findByMonthAndYearAndGhlLocation_Id(int month, int year, Long ghlLocationId);

    List<KpiReport> findByMonthAndYear(int month, int year);
}
