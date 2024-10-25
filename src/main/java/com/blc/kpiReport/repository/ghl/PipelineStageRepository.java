package com.blc.kpiReport.repository.ghl;

import com.blc.kpiReport.schema.ghl.PipelineStage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineStageRepository extends JpaRepository<PipelineStage, Long> {
    void deleteByGoHighLevelReport_Id(Long goHighLevelReportId);
}
