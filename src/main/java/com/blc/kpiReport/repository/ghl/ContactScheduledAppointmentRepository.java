package com.blc.kpiReport.repository.ghl;

import com.blc.kpiReport.schema.ghl.ContactScheduledAppointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactScheduledAppointmentRepository extends JpaRepository<ContactScheduledAppointment, Long> {
    void deleteByGoHighLevelReport_Id(Long goHighLevelReportId);
    List<ContactScheduledAppointment> findAllByGoHighLevelReport_Id(Long goHighLevelReportId);
}
