package com.blc.kpiReport.repository.ghl;

import com.blc.kpiReport.schema.ghl.ContactWon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactWonRepository extends JpaRepository<ContactWon, Long> {
    void deleteByGoHighLevelReport_Id(Long goHighLevelReportId);
    List<ContactWon> findAllByGoHighLevelReport_Id(Long goHighLevelReportId);
}
