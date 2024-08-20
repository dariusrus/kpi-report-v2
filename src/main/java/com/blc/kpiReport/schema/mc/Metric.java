package com.blc.kpiReport.schema.mc;

import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "metric")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Metric extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Size(max = 200)
    @Column(name = "metric_name", length = 200, nullable = false)
    private String metricName;

    @OneToMany(mappedBy = "metric", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Information> informationList;

    @ManyToOne
    @JoinColumn(name = "daily_metric_id")
    @JsonBackReference
    private DailyMetric dailyMetric;
}

