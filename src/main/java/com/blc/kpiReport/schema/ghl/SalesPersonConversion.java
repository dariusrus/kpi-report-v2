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
@Table(name = "sales_person_conversion")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SalesPersonConversion extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @JsonIgnore
    private Long id;

    @NotNull
    @Size(max = 500)
    @Column(name = "sales_person_id", length = 500)
    private String salesPersonId;

    @NotNull
    @Size(max = 500)
    @Column(name = "sales_person_name", length = 500)
    private String salesPersonName;

    @NotNull
    @Column(name = "count")
    private int count;

    @ManyToOne
    @JoinColumn(name = "pipeline_stage_id", nullable = false)
    @JsonBackReference
    private PipelineStage pipelineStage;
}
