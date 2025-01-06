package com.blc.kpiReport.schema.ghl;

import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "appointment_opportunity")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AppointmentOpportunity extends DateAudit implements Serializable {

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
    @Column(name = "appointment_date")
    private Instant appointmentDate;

    @NotNull
    @Column(name = "last_stage_change_at")
    private Instant lastStageChangeAt;

    @Size(max = 500)
    @Column(name = "pipeline_name", length = 500)
    private String pipelineName;

    @Size(max = 500)
    @Column(name = "stage_name", length = 500)
    private String stageName;

    @OneToOne
    @JoinColumn(name = "ghl_contact_id")
    private GhlContact ghlContact;

    @OneToOne
    @JoinColumn(name = "ghl_user_id")
    private GhlUser ghlUser;

    @ManyToOne
    @JoinColumn(name = "calendar_id", nullable = false)
    @JsonBackReference
    private Calendar calendar;
}
