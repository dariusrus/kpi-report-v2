package com.blc.kpiReport.repository.ghl;

import com.blc.kpiReport.schema.ghl.GoHighLevelReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoHighLevelReportRepository extends JpaRepository<GoHighLevelReport, Long> {
    Optional<GoHighLevelReport> findByKpiReport_Id(Long kpiReportId);
}
