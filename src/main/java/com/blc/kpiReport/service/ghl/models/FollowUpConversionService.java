package com.blc.kpiReport.service.ghl.models;

import com.blc.kpiReport.repository.ghl.FollowUpConversionRepository;
import com.blc.kpiReport.repository.ghl.SalesPersonConversationRepository;
import com.blc.kpiReport.schema.ghl.FollowUpConversion;
import com.blc.kpiReport.schema.ghl.SalesPersonConversation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FollowUpConversionService {

    private final FollowUpConversionRepository repository;

    public FollowUpConversionService(FollowUpConversionRepository repository) {
        this.repository = repository;
    }

    public List<FollowUpConversion> saveAll(List<FollowUpConversion> followUpConversions) {
        return repository.saveAll(followUpConversions);
    }

    public void deleteByGoHighLevelReportId(Long goHighLevelReportId) {
        repository.deleteByGoHighLevelReport_Id(goHighLevelReportId);
    }

    public List<FollowUpConversion> findAllByGoHighLevelReportId(Long goHighLevelReportId) {
        return repository.findAllByGoHighLevelReport_Id(goHighLevelReportId);
    }
}
