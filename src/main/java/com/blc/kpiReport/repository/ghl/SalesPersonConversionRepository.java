package com.blc.kpiReport.repository.ghl;

import com.blc.kpiReport.schema.ghl.SalesPersonConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesPersonConversionRepository extends JpaRepository<SalesPersonConversion, Long> {
}