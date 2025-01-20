package com.blc.kpiReport.models.pojo.ghl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@RequiredArgsConstructor
public class GhlOauthToken {

    @JsonProperty("access_token")
    String accessToken;

    @JsonProperty("token_type")
    String tokenType;

    @JsonProperty("expires_in")
    Integer expiresIn;

    @JsonProperty("refresh_token")
    String refreshToken;

    String scope;

    String locationId;

    String companyId;
}

