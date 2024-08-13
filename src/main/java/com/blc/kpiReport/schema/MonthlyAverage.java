package com.blc.kpiReport.schema;

import com.blc.kpiReport.models.ClientType;
import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "monthly_average")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class MonthlyAverage extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @JsonIgnore
    private Long id;

    @NotNull
    @Column(name = "month", nullable = false)
    private int month;

    @NotNull
    @Column(name = "year", nullable = false)
    private int year;

    @NotNull
    @Column(name = "average_uniqueSiteVisitors")
    private int averageUniqueSiteVisitors;

    @NotNull
    @Column(name = "average_total_leads", nullable = false)
    private int averageTotalLeads;

    @NotNull
    @Column(name = "average_opportunity_to_lead", nullable = false)
    private double averageOpportunityToLead;

    @NotNull
    @Column(name = "weighted_average_opportunity_to_lead", nullable = false)
    private double weightedAverageOpportunityToLead;

    @NotNull
    @Column(name = "client_type")
    @Enumerated(EnumType.STRING)
    private ClientType clientType;
}
