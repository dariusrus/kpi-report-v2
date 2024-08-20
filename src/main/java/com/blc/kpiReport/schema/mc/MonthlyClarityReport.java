package com.blc.kpiReport.schema.mc;

import com.blc.kpiReport.schema.KpiReport;
import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "monthly_clarity_report")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class MonthlyClarityReport extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @OneToMany(mappedBy = "monthlyClarityReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UrlMetric> urls;

    @OneToOne
    @JoinColumn(name = "kpi_report_id", nullable = false)
    @JsonBackReference
    private KpiReport kpiReport;
}