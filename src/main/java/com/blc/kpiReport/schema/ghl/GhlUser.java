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
@Table(name = "ghl_user")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class GhlUser extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @JsonIgnore
    private Long id;

    @NotNull
    @Size(max = 500)
    @Column(name = "user_id", length = 500)
    private String userId;

    @NotNull
    @Size(max = 500)
    @Column(name = "name", length = 500)
    private String name;

    @Size(max = 1000)
    @Column(name = "photo_url", length = 1000)
    private String photoUrl;

    @ManyToOne
    @JoinColumn(name = "ghl_location_id", nullable = false)
    @JsonBackReference
    private GhlLocation ghlLocation;
}
