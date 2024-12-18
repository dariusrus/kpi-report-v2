package com.blc.kpiReport.service.ghl.models;

import com.blc.kpiReport.schema.ghl.LeadSource;
import com.blc.kpiReport.repository.ghl.LeadSourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeadSourceService {

    private final LeadSourceRepository repository;

    public LeadSourceService(LeadSourceRepository repository) {
        this.repository = repository;
    }

    public List<LeadSource> saveAll(List<LeadSource> leadSources) {
        return repository.saveAll(leadSources);
    }

    public void deleteByGoHighLevelReportId(Long goHighLevelReportId) {
        repository.deleteByGoHighLevelReport_Id(goHighLevelReportId);
    }

    public List<LeadSource> findAllByGoHighLevelReportId(Long goHighLevelReportId) {
        return repository.findAllByGoHighLevelReport_Id(goHighLevelReportId);
    }
}
