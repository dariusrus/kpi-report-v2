package com.blc.kpiReport.repository;

import com.blc.kpiReport.schema.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    void deleteByGoHighLevelReport_Id(Long goHighLevelReportId);
}