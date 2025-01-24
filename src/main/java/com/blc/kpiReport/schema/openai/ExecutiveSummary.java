package com.blc.kpiReport.schema.openai;

import com.blc.kpiReport.schema.KpiReport;
import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "executive_summary")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ExecutiveSummary extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(length = 4000)
    private String summary;

    @OneToOne
    @JoinColumn(name = "kpi_report_id", nullable = false)
    @JsonBackReference
    private KpiReport kpiReport;
}
