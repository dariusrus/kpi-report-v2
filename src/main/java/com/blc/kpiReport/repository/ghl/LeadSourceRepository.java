package com.blc.kpiReport.repository.ghl;

import com.blc.kpiReport.schema.ghl.LeadSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadSourceRepository extends JpaRepository<LeadSource, Long> {
    void deleteByGoHighLevelReport_Id(Long goHighLevelReportId);
    List<LeadSource> findAllByGoHighLevelReport_Id(Long goHighLevelReportId);

}
