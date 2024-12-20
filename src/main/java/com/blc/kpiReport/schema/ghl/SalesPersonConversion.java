package com.blc.kpiReport.schema.ghl;

import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.List;

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
    @Column(name = "count")
    private int count;

    @NotNull
    @Column(name = "monetary_value")
    private Double monetaryValue;

    @ManyToOne
    @JoinColumn(name = "ghl_user_id")
    private GhlUser ghlUser;

    @OneToMany(mappedBy = "salesPersonConversion", fetch = FetchType.EAGER)
    private List<GhlContact> convertedGhlContacts;

    @ManyToOne
    @JoinColumn(name = "pipeline_stage_id", nullable = false)
    @JsonBackReference
    private PipelineStage pipelineStage;
}
