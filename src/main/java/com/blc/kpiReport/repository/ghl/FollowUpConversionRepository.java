package com.blc.kpiReport.repository.ghl;

import com.blc.kpiReport.schema.ghl.FollowUpConversion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowUpConversionRepository extends JpaRepository<FollowUpConversion, Long> {
    void deleteByGoHighLevelReport_Id(Long goHighLevelReportId);
    List<FollowUpConversion> findAllByGoHighLevelReport_Id(Long goHighLevelReportId);
}
