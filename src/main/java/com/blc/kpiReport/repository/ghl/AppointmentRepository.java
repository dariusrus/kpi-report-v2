package com.blc.kpiReport.repository.ghl;

import com.blc.kpiReport.schema.ghl.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}
