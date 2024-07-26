package com.blc.kpiReport.repository;

import com.blc.kpiReport.schema.LeadSource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadSourceRepository extends JpaRepository<LeadSource, Long> {
    void deleteByGoHighLevelReport_Id(Long goHighLevelReportId);
}
