package com.blc.kpiReport.schema.ghl;

import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "contact_scheduled_appointment")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ContactScheduledAppointment extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @JsonIgnore
    private Long id;

    @Size(max = 200)
    @Column(name = "contact_name", length = 200, nullable = false)
    private String contactName;

    @Column(name = "scheduled_a_call")
    private boolean scheduledACall;

    @OneToOne
    @JoinColumn(name = "ghl_contact_id")
    private GhlContact ghlContact;

    @ManyToOne
    @JoinColumn(name = "ghl_user_id")
    private GhlUser ghlUser;
}
