package com.blc.kpiReport.repository.ghl;

import com.blc.kpiReport.schema.ghl.ContactWon;
import com.blc.kpiReport.schema.ghl.SalesPersonConversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesPersonConversationRepository extends JpaRepository<SalesPersonConversation, Long> {
    void deleteByGoHighLevelReport_Id(Long goHighLevelReportId);
}
