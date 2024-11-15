package com.blc.kpiReport.models.response.ghl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class GhlUserResponse {
    private String userId;
    private String name;
    private String photoUrl;
}