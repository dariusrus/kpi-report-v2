package com.blc.kpiReport.schema.ghl;

import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "follow_up_conversion")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class FollowUpConversion extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @JsonIgnore
    private Long id;

    @Column(name = "total_sms")
    private Integer totalSms;

    @Column(name = "total_emails")
    private Integer totalEmails;

    @Column(name = "total_calls")
    private Integer totalCalls;

    @Column(name = "total_live_chat_messages")
    private Integer totalLiveChatMessages;

    @Column(name = "total_follow_ups")
    private Integer totalFollowUps;

    @Column(name = "total_conversions")
    private Integer totalConversions;

    @Column(name = "total_follow_up_per_conversion")
    private Double totalFollowUpPerConversion;

    @ManyToOne
    @JoinColumn(name = "ghl_user_id")
    private GhlUser ghlUser;

    @ManyToOne
    @JoinColumn(name = "go_high_level_report_id", nullable = false)
    @JsonBackReference
    private GoHighLevelReport goHighLevelReport;
}
