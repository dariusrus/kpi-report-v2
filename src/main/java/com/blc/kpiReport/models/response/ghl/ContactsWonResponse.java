package com.blc.kpiReport.models.response.ghl;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ContactsWonResponse {
    private String contactName;
    private String contactEmail;
    private String contactSource;
    private String attributionSource;
}

