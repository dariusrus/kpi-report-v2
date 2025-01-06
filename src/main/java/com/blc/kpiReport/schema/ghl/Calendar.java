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
@Table(name = "calendar")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Calendar extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @JsonIgnore
    private Long id;

    @NotNull
    @Size(max = 200)
    @Column(name = "calendar_ghl_id", length = 200, nullable = false)
    private String calendarGhlId;

    @Size(max = 200)
    @Column(name = "calendar_name", length = 200)
    private String calendarName;

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<AppointmentOpportunity> appointmentOpportunities;

    @ManyToOne
    @JoinColumn(name = "go_high_level_report_id", nullable = false)
    @JsonBackReference
    private GoHighLevelReport goHighLevelReport;
}
