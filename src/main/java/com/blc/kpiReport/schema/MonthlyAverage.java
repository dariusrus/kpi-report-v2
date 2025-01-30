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
    private Integer month;

    @NotNull
    @Column(name = "year", nullable = false)
    private Integer year;

    @NotNull
    @Column(name = "average_uniqueSiteVisitors")
    private Integer averageUniqueSiteVisitors;

    @NotNull
    @Column(name = "average_total_leads", nullable = false)
    private Integer averageTotalLeads;

    @NotNull
    @Column(name = "average_opportunity_to_lead", nullable = false)
    private Double averageOpportunityToLead;

    @NotNull
    @Column(name = "weighted_average_opportunity_to_lead", nullable = false)
    private Double weightedAverageOpportunityToLead;

    @Column(name = "average_follow_ups")
    private Integer averageFollowUps;

    @Column(name = "average_conversions")
    private Integer averageConversions;

    @Column(name = "average_follow_up_per_conversion")
    private Double averageFollowUpPerConversion;

    @Column(name = "average_total_follow_ups")
    private Integer averageTotalFollowUps;

    @Column(name = "average_total_conversions")
    private Integer averageTotalConversions;

    @Column(name = "average_total_follow_up_per_conversion")
    private Double averageTotalFollowUpPerConversion;

    @Column(name = "average_live_chat_messages")
    private Integer averageLiveChatMessage;

    @Column(name = "average_sms")
    private Integer averageSms;

    @Column(name = "average_emails")
    private Integer averageEmails;

    @Column(name = "average_calls")
    private Integer averageCalls;

    @NotNull
    @Column(name = "client_type")
    @Enumerated(EnumType.STRING)
    private ClientType clientType;
}
