package com.blc.kpiReport.repository.ghl;

import com.blc.kpiReport.schema.ghl.PipelineStage;
import com.blc.kpiReport.schema.ghl.SalesPersonConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PipelineStageRepository extends JpaRepository<PipelineStage, Long> {
    void deleteByGoHighLevelReport_Id(Long goHighLevelReportId);
    List<PipelineStage> findAllByGoHighLevelReport_Id(Long goHighLevelReportId);
}
