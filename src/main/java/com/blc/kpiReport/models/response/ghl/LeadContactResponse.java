package com.blc.kpiReport.models.response.ghl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class LeadContactResponse {
    private String contactName;
    private String contactSource;
    private String createdBySource;
    private String dateAdded;
    private String ownerName;
    private String ownerPhotoUrl;
    private String status;
}