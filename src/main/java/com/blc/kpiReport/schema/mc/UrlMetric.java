package com.blc.kpiReport.schema.mc;

import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "url_metric")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UrlMetric extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(nullable = false, length = 5000)
    @Size(max = 5000)
    private String url;

    @OneToMany(mappedBy = "urlMetric", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeviceMetric> devices;

    @ManyToOne
    @JoinColumn(name = "monthly_clarity_report_id", nullable = false)
    @JsonBackReference
    private MonthlyClarityReport monthlyClarityReport;
}