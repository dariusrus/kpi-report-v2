package com.blc.kpiReport.models.response;

import com.blc.kpiReport.models.ClientType;
import lombok.*;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GhlLocationFullResponse {
    private Long id;
    private String locationId;
    private String gaAccountId;
    private String gaPropertyId;
    private String gaCountryCode;
    private String name;
    private String ghlAccessToken;
    private String ghlRefreshToken;
    private String ghlTokenScope;
    private Instant ghlTokenDate;
    private String mcApiToken;
    private ClientType clientType;
}