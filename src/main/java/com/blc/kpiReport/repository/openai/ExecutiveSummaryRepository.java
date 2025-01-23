package com.blc.kpiReport.repository.openai;

import com.blc.kpiReport.schema.openai.ExecutiveSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutiveSummaryRepository extends JpaRepository<ExecutiveSummary, Long> {
}
