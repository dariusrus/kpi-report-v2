package com.blc.kpiReport.repository.ghl;

import com.blc.kpiReport.schema.ghl.SalesPersonConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesPersonConversationRepository extends JpaRepository<SalesPersonConversation, Long> {
    void deleteByGoHighLevelReport_Id(Long goHighLevelReportId);
    List<SalesPersonConversation> findAllByGoHighLevelReport_Id(Long goHighLevelReportId);
}
