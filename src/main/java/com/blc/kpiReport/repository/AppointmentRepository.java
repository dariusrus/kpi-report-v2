package com.blc.kpiReport.repository;

import com.blc.kpiReport.schema.ghl.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}
