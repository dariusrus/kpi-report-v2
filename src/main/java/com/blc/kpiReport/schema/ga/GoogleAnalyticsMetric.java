package com.blc.kpiReport.schema.ga;

import com.blc.kpiReport.schema.KpiReport;
import com.blc.kpiReport.schema.ghl.LeadContact;
import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "google_analytics_metric")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class GoogleAnalyticsMetric extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @JsonIgnore
    private Long id;

    @NotNull
    @Column(name = "unique_site_visitors", nullable = false)
    private int uniqueSiteVisitors;

    @OneToMany(mappedBy = "googleAnalyticsMetric", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<CityAnalytics> cityAnalytics;

    @OneToOne
    @JoinColumn(name = "kpi_report_id", nullable = false)
    @JsonBackReference
    private KpiReport kpiReport;
}
