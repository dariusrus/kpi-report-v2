package com.blc.kpiReport.models.response;

import com.blc.kpiReport.models.ClientType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GhlLocationSummaryResponse {

    private Long id;
    private String locationId;
    private String gaAccountId;
    private String gaPropertyId;
    private String gaCountryCode;
    private String name;
    private boolean ghlAccessTokenIsSet;
    private boolean ghlRefreshTokenIsSet;
    private boolean mcApiTokenIsSet;
    private ClientType clientType;
}