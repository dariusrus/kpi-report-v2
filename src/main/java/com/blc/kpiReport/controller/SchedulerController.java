package com.blc.kpiReport.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/scheduler")
@Tag(name = "Scheduler API", description = "Endpoints for retrieving information about scheduled tasks.")
public class SchedulerController {

    private final ApplicationContext applicationContext;

    @Operation(
        summary = "Get information about scheduled cron jobs",
        description = "Returns a list of methods that are scheduled to run, along with their schedule expressions.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Cron job information retrieved successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CronJobInfo.class))
            )
        }
    )
    @GetMapping(path = "/cron-jobs", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CronJobInfo> getCronJobs() {
        List<CronJobInfo> cronJobInfos = new ArrayList<>();

        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Method[] methods = bean.getClass().getDeclaredMethods();

            for (Method method : methods) {
                if (method.isAnnotationPresent(Scheduled.class)) {
                    Scheduled scheduled = method.getAnnotation(Scheduled.class);
                    String cron = scheduled.cron();
                    cronJobInfos.add(new CronJobInfo(bean.getClass().getName(), method.getName(), cron));
                }
            }
        }
        return cronJobInfos;
    }

    @Schema(description = "Information about a cron job")
    @Getter
    @Setter
    public static class CronJobInfo {
        @Schema(description = "The name of the class where the method is defined")
        private String className;

        @Schema(description = "The name of the method that is scheduled")
        private String methodName;

        @Schema(description = "The cron expression defining when the method is scheduled to run")
        private String cronExpression;

        public CronJobInfo(String className, String methodName, String cronExpression) {
            this.className = className;
            this.methodName = methodName;
            this.cronExpression = cronExpression;
        }
    }
}
