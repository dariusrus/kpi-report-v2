package com.blc.kpiReport.schema.ga;

import com.blc.kpiReport.schema.KpiReport;
import com.blc.kpiReport.schema.ghl.LeadSource;
import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "city_analytics")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CityAnalytics extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @JsonIgnore
    private Long id;

    @NotNull
    @Column(name = "city", nullable = false)
    private String city;

    @NotNull
    @Column(name = "unique_site_visitors", nullable = false)
    private int uniqueSiteVisitors;
    @NotNull
    @Column(name = "average_session_duration", nullable = false)
    private double averageSessionDuration;

    @ManyToOne
    @JoinColumn(name = "google_analytics_metric_id", nullable = false)
    @JsonBackReference
    private GoogleAnalyticsMetric googleAnalyticsMetric;
}
