package com.blc.kpiReport.models.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ContactsWonResponse {
    private String contactName;
    private String contactEmail;
}

