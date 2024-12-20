package com.blc.kpiReport.schema.ghl;

import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "contact_won")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ContactWon extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @JsonIgnore
    private Long id;

    @NotNull
    @Size(max = 500)
    @Column(name = "contact_name", length = 500)
    private String contactName;

    @NotNull
    @Size(max = 500)
    @Column(name = "contact_email", length = 500)
    private String contactEmail;

    @NotNull
    @Size(max = 200)
    @Column(name = "source", length = 200)
    private String source;

    @NotNull
    @Size(max = 200)
    @Column(name = "attribution_source", length = 200)
    private String attributionSource;

    @OneToOne
    @JoinColumn(name = "ghl_contact_id")
    private GhlContact ghlContact;

    @ManyToOne
    @JoinColumn(name = "go_high_level_report_id", nullable = false)
    @JsonBackReference
    private GoHighLevelReport goHighLevelReport;
}
