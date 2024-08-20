package com.blc.kpiReport.repository;

import com.blc.kpiReport.schema.mc.MonthlyClarityReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyClarityReportRepository extends JpaRepository<MonthlyClarityReport, Long> {
}