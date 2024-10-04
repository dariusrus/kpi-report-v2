package com.blc.kpiReport.models.response.ghl;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SalesPersonConversionResponse {
    private String salesPersonName;
    private int count;
}

