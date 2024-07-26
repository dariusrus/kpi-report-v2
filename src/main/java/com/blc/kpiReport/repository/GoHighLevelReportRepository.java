package com.blc.kpiReport.repository;

import com.blc.kpiReport.schema.GoHighLevelReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoHighLevelReportRepository extends JpaRepository<GoHighLevelReport, Long> {
    Optional<GoHighLevelReport> findByKpiReport_Id(Long kpiReportId);
}
