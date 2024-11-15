package com.blc.kpiReport.schema.ghl;

import com.blc.kpiReport.schema.GhlLocation;
import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "ghl_contact")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class GhlContact extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @JsonIgnore
    private Long id;

    @NotNull
    @Size(max = 500)
    @Column(name = "ghl_id", length = 500)
    private String ghlId;

    @NotNull
    @Size(max = 500)
    @Column(name = "name", length = 500)
    private String name;

    @NotNull
    @Size(max = 500)
    @Column(name = "email", length = 500)
    private String email;

    @NotNull
    @Size(max = 500)
    @Column(name = "phone", length = 500)
    private String phone;

    @ManyToOne
    @JoinColumn(name = "ghl_location_id")
    @JsonBackReference
    private GhlLocation ghlLocation;

    @OneToOne(mappedBy = "ghlContact")
    @JsonBackReference
    private LeadContact leadContact;

    @OneToOne(mappedBy = "ghlContact")
    @JsonBackReference
    private ContactWon contactWon;

    @OneToOne(mappedBy = "ghlContact")
    @JsonBackReference
    private SalesPersonConversation salesPersonConversation;

    @ManyToOne
    @JoinColumn(name = "sales_person_conversion_id")
    @JsonBackReference
    private SalesPersonConversion salesPersonConversion;
}
