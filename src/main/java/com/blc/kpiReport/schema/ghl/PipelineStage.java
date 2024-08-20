package com.blc.kpiReport.schema.ghl;

import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "pipeline_stage")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PipelineStage extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @JsonIgnore
    private Long id;

    @NotNull
    @Size(max = 500)
    @Column(name = "pipeline_name", length = 500)
    private String pipelineName;

    @NotNull
    @Size(max = 500)
    @Column(name = "stage_name", length = 500)
    private String stageName;

    @NotNull
    @Column(name = "count")
    private int count;

    @NotNull
    @Column(name = "position")
    private int position;

    @NotNull
    @Column(name = "percentage")
    private double percentage;

    @NotNull
    @Column(name = "monetary_value")
    private double monetaryValue;

    @ManyToOne
    @JoinColumn(name = "go_high_level_report_id", nullable = false)
    @JsonBackReference
    private GoHighLevelReport goHighLevelReport;
}
