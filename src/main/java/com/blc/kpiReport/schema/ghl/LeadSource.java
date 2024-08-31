package com.blc.kpiReport.schema.ghl;

import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "lead_source")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class LeadSource extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @JsonIgnore
    private Long id;

    @NotNull
    @Size(max = 200)
    @Column(name = "source", length = 200, nullable = false)
    private String source;

    @NotNull
    @Size(max = 200)
    @Column(name = "lead_type", length = 200)
    private String leadType;

    @NotNull
    @Column(name = "total_leads", nullable = false)
    private int totalLeads;

    @NotNull
    @Column(name = "total_values", nullable = false)
    private double totalValues;

    @NotNull
    @Column(name = "open", nullable = false)
    private int open;

    @NotNull
    @Column(name = "won", nullable = false)
    private int won;

    @NotNull
    @Column(name = "lost", nullable = false)
    private int lost;

    @NotNull
    @Column(name = "abandoned", nullable = false)
    private int abandoned;

    @NotNull
    @Column(name = "win_percentage", nullable = false)
    private double winPercentage;

    @OneToMany(mappedBy = "leadSource", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<LeadContact> leadContacts;

    @ManyToOne
    @JoinColumn(name = "go_high_level_report_id", nullable = false)
    @JsonBackReference
    private GoHighLevelReport goHighLevelReport;
}
