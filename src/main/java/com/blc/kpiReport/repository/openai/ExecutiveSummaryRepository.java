package com.blc.kpiReport.repository.openai;

import com.blc.kpiReport.schema.openai.ExecutiveSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExecutiveSummaryRepository extends JpaRepository<ExecutiveSummary, Long> {
    Optional<ExecutiveSummary> findByKpiReportId(Long kpiReportId);
}
