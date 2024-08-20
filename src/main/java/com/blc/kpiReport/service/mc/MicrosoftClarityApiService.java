package com.blc.kpiReport.service.mc;

import com.blc.kpiReport.exception.MicrosoftClarityApiException;
import com.blc.kpiReport.schema.mc.DailyMetric;
import com.blc.kpiReport.schema.GhlLocation;
import com.blc.kpiReport.schema.KpiReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MicrosoftClarityApiService {
    private final MicrosoftClarityFetchService fetchService;
    private final MicrosoftClarityProcessorService processorService;
    private final MicrosoftClarityWriterService writerService;

    public DailyMetric fetchMetricsForPreviousDay(GhlLocation ghlLocation, KpiReport kpiReport, int day) throws IOException, MicrosoftClarityApiException {
        DailyMetric dailyMetric;
        if (ObjectUtils.isNotEmpty(ghlLocation)) {
            // Reader
            var previousDayMetrics = fetchService.fetchMetricsForPreviousDay(ghlLocation.getMcApiToken());

            // Processor
            var processedMetrics = processorService.processMetrics(previousDayMetrics);

            // Writer
            dailyMetric = writerService.getOrCreateDailyMetric(kpiReport, day);
            processedMetrics.forEach(metric -> metric.setDailyMetric(dailyMetric));
            writerService.saveMetrics(processedMetrics);
            log.info("Daily Metrics saved {}", dailyMetric);
        } else {
            dailyMetric = null;
        }
        return dailyMetric;
    }

}
