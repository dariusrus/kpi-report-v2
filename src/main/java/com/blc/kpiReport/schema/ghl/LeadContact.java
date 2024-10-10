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
@Table(name = "lead_contact")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class LeadContact extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @JsonIgnore
    private Long id;

    @NotNull
    @Size(max = 200)
    @Column(name = "contact_name", length = 200, nullable = false)
    private String contactName;

    @Size(max = 500)
    @Column(name = "contact_source", length = 500)
    private String contactSource;

    @Size(max = 500)
    @Column(name = "created_by_source", length = 500)
    private String createdBySource;

    @Size(max = 500)
    @Column(name = "attribution_source", length = 500)
    private String attributionSource;

    @Size(max = 200)
    @Column(name = "date_added", length = 200, nullable = false)
    private String dateAdded;

    @Size(max = 500)
    @Column(name = "owner_name", length = 500)
    private String ownerName;

    @Size(max = 1000)
    @Column(name = "owner_photo_url", length = 1000)
    private String ownerPhotoUrl;

    @Size(max = 200)
    @Column(name = "status", length = 200)
    private String status;

    @ManyToOne
    @JoinColumn(name = "lead_source_id", nullable = false)
    @JsonBackReference
    private LeadSource leadSource;
}
