package com.blc.kpiReport.models.response.ghl;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SalesPersonConversionResponse {
    private String salesPersonId;
    private String salesPersonName;
    private String photoUrl;
    private int count;
    private List<ContactResponse> convertedContacts;
    private double monetaryValue;
}

