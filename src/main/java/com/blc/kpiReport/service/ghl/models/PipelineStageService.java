package com.blc.kpiReport.service.ghl.models;

import com.blc.kpiReport.schema.PipelineStage;
import com.blc.kpiReport.repository.PipelineStageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PipelineStageService {

    private final PipelineStageRepository repository;

    public PipelineStageService(PipelineStageRepository repository) {
        this.repository = repository;
    }

    public List<PipelineStage> saveAll(List<PipelineStage> pipelineStages) {
        return repository.saveAll(pipelineStages);
    }

    public void deleteByGoHighLevelReportId(Long goHighLevelReportId) {
        repository.deleteByGoHighLevelReport_Id(goHighLevelReportId);
    }
}
