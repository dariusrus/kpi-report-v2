package com.blc.kpiReport.schema;

import com.blc.kpiReport.models.ClientType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "ghl_location")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class GhlLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Size(max = 200)
    @Column(name = "location_id", length = 200, nullable = false, unique = true)
    private String locationId;

    @Size(max = 200)
    @Column(name = "ga_account_id", length = 200)
    private String gaAccountId;

    @Size(max = 200)
    @Column(name = "ga_property_id", length = 200)
    private String gaPropertyId;

    @Size(max = 50)
    @Column(name = "ga_country_code", length = 50, columnDefinition = "varchar(50) default 'US'")
    private String gaCountryCode;

    @Size(max = 200)
    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Size(max = 5000)
    @Column(name = "ghl_access_token", length = 5000)
    private String ghlAccessToken;

    @Size(max = 5000)
    @Column(name = "ghl_refresh_token", length = 5000)
    private String ghlRefreshToken;

    @Size(max = 5000)
    @Column(name = "ghl_token_scope", length = 5000)
    private String ghlTokenScope;

    @Column(name = "ghl_token_date")
    private Instant ghlTokenDate;

    @Size(max = 5000)
    @Column(name = "mc_api_token", length = 5000)
    private String mcApiToken;

    @Column(name = "client_type")
    @Enumerated(EnumType.STRING)
    private ClientType clientType;
}
