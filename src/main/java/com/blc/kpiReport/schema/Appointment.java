package com.blc.kpiReport.schema;

import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "appointment")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Appointment extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @JsonIgnore
    private Long id;

    @NotNull
    @Size(max = 200)
    @Column(name = "status", length = 200)
    private String status;

    @NotNull
    @Column(name = "count")
    private int count;

    @NotNull
    @Column(name = "percentage")
    private double percentage;

    @ManyToOne
    @JoinColumn(name = "go_high_level_report_id", nullable = false)
    @JsonBackReference
    private GoHighLevelReport goHighLevelReport;
}
