package com.blc.kpiReport.schema.mc;

import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "information")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Information extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "sessions_count")
    private Integer sessionsCount;

    @Column(name = "sessions_with_metric_percentage")
    private Double sessionsWithMetricPercentage;

    @Column(name = "sessions_without_metric_percentage")
    private Double sessionsWithoutMetricPercentage;

    @Column(name = "pages_views")
    private Integer pagesViews;

    @Column(name = "sub_total")
    private Integer subTotal;

    // Fields for EngagementTime
    @Column(name = "total_time")
    private Integer totalTime;

    @Column(name = "active_time")
    private Integer activeTime;

    // Fields for ScrollDepth
    @Column(name = "average_scroll_depth")
    private Double averageScrollDepth;

    // Fields for Traffic
    @Column(name = "total_session_count")
    private String totalSessionCount;

    @Column(name = "total_bot_session_count")
    private String totalBotSessionCount;

    @Column(name = "distinct_user_count")
    private String distinctUserCount;

    @Column(name = "pages_per_session_percentage")
    private Double pagesPerSessionPercentage;

    @Size(max = 5000)
    @Column(name = "url", length = 5000)
    private String url;

    // Common fields
    @Size(max = 200)
    @Column(name = "device", length = 200)
    private String device;

    @Size(max = 200)
    @Column(name = "channel", length = 200)
    private String channel;

    @Size(max = 200)
    @Column(name = "source", length = 200)
    private String source;

    @ManyToOne
    @JoinColumn(name = "metric_id", nullable = false)
    @JsonBackReference
    private Metric metric;
}
