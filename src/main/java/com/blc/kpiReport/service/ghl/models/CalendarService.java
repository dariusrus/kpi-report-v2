package com.blc.kpiReport.service.ghl.models;

import com.blc.kpiReport.repository.ghl.CalendarRepository;
import com.blc.kpiReport.schema.ghl.Calendar;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalendarService {

    private final CalendarRepository repository;

    public CalendarService(CalendarRepository repository) {
        this.repository = repository;
    }

    public List<Calendar> saveAll(List<Calendar> calendars) {
        return repository.saveAll(calendars);
    }

    public void deleteByGoHighLevelReportId(Long goHighLevelReportId) {
        repository.deleteByGoHighLevelReport_Id(goHighLevelReportId);
    }
}
