package com.blc.kpiReport.models.pojo.openai;

import com.blc.kpiReport.schema.ghl.ContactWon;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class MonthlyPrompt {
    private String subAgency;
    private String ghlLocationId;
    private String country;
    private String monthAndYear;
    private String clientType;
    private GoogleAnalytics googleAnalytics;
    private WebsiteAnalytics websiteAnalytics;
    private GoHighLevel goHighLevel;
    private List<ContactWon> contactsWon;
}
