package com.blc.kpiReport.repository;

import com.blc.kpiReport.schema.ghl.LeadContact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadContactRepository extends JpaRepository<LeadContact, Long> {
}
