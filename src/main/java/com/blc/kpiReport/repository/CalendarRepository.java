package com.blc.kpiReport.repository;

import com.blc.kpiReport.schema.ghl.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarRepository extends JpaRepository<Calendar, Long> {
    void deleteByGoHighLevelReport_Id(Long goHighLevelReportId);
}
