package com.blc.kpiReport.schema;

import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "go_high_level_report")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class GoHighLevelReport extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @JsonIgnore
    private Long id;

    @OneToMany(mappedBy = "goHighLevelReport", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<LeadSource> leadSources;

    @OneToMany(mappedBy = "goHighLevelReport", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "goHighLevelReport", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<ContactWon> contactsWon;

    @OneToMany(mappedBy = "goHighLevelReport", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<PipelineStage> pipelineStages;

    @OneToOne
    @JoinColumn(name = "kpi_report_id", nullable = false)
    @JsonBackReference
    private KpiReport kpiReport;
}
