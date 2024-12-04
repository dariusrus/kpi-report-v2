package com.blc.kpiReport.schema.ghl;

import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

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
    private int totalSms;

    @Column(name = "total_emails")
    private int totalEmails;

    @Column(name = "total_calls")
    private int totalCalls;

    @Column(name = "total_live_chat_messages")
    private int totalLiveChatMessages;

    @Column(name = "total_followups")
    private int totalFollowups;

    @Column(name = "total_conversions")
    private int totalConversions;

    @Column(name = "follow_up_per_conversion")
    private double followUpPerConversion;

    @ManyToOne
    @JoinColumn(name = "ghl_user_id")
    private GhlUser ghlUser;

    @ManyToOne
    @JoinColumn(name = "go_high_level_report_id", nullable = false)
    @JsonBackReference
    private GoHighLevelReport goHighLevelReport;
}
