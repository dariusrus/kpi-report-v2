package com.blc.kpiReport.schema;

import com.blc.kpiReport.models.ReportStatus;
import com.blc.kpiReport.schema.ga.GoogleAnalyticsMetric;
import com.blc.kpiReport.schema.ghl.GoHighLevelReport;
import com.blc.kpiReport.schema.mc.DailyMetric;
import com.blc.kpiReport.schema.mc.MonthlyClarityReport;
import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "kpi_report")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class KpiReport extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @JsonIgnore
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ghl_location_id", nullable = false)
    private GhlLocation ghlLocation;

    @NotNull
    @Column(name = "month", nullable = false)
    private int month;

    @NotNull
    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "last_run_status")
    private ReportStatus lastRunStatus;

    @Column(name = "last_start_time")
    private Instant lastStartTime;

    @Column(name = "last_end_time")
    private Instant lastEndTime;

    @OneToOne(mappedBy = "kpiReport")
    @JsonManagedReference
    private GoogleAnalyticsMetric googleAnalyticsMetric;

    @OneToOne(mappedBy = "kpiReport")
    @JsonManagedReference
    private GoHighLevelReport goHighLevelReport;

    @OneToOne(mappedBy = "kpiReport")
    @JsonManagedReference
    private MonthlyClarityReport monthlyClarityReport;

    @OneToMany(mappedBy = "kpiReport", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<DailyMetric> dailyMetrics;
}
