package com.blc.kpiReport.models.response.ghl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class ContactResponse {
    private String contactId;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
}