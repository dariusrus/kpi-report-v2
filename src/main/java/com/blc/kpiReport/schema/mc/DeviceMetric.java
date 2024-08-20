package com.blc.kpiReport.schema.mc;

import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "device_metric")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DeviceMetric extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(nullable = false)
    private String deviceType;

    @Column(name = "average_scroll_depth")
    private Double averageScrollDepth;

    @Column(name = "total_time")
    private Integer totalTime;

    @Column(name = "active_time")
    private Integer activeTime;

    @Column(name = "total_session_count")
    private Integer totalSessionCount;

    @ManyToOne
    @JoinColumn(name = "url_metric_id", nullable = false)
    @JsonBackReference
    private UrlMetric urlMetric;
}