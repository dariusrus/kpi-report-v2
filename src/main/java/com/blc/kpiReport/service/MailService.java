package com.blc.kpiReport.service;

import com.blc.kpiReport.config.SpringMailProperties;
import com.blc.kpiReport.models.response.GenerateKpiReportBatchResponse;
import com.blc.kpiReport.models.response.GenerateKpiReportResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final SpringMailProperties mailProperties;

    private JavaMailSender createJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailProperties.getHost());
        mailSender.setPort(mailProperties.getPort());

        mailSender.setUsername(mailProperties.getAccount().getUsername());
        mailSender.setPassword(mailProperties.getAccount().getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        log.debug("JavaMailSender configured with host: {}, port: {}", mailProperties.getHost(), mailProperties.getPort());

        return mailSender;
    }

    public void sendEmail(String to, String subject, String body) throws MessagingException {
        log.info("Sending email to: {}", to);

        String[] recipients = to.split("\\s*,\\s*");

        JavaMailSender mailSender = createJavaMailSender();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(mailProperties.getAccount().getUsername());
        helper.setTo(recipients);
        helper.setSubject(subject);
        helper.setText(body, true);

        mailSender.send(message);
        log.info("Email sent successfully to: {}", to);
    }

    public void sendReportNotification(GenerateKpiReportBatchResponse reportData, boolean isCronJob) throws MessagingException {
        log.info("Preparing to send report notification...");
        String htmlBody = generateHtmlBody(reportData);
        String subjectPrefix = isCronJob ? "[DEV] Scheduled Job: " : "[DEV] Manually Triggered: ";
        String subject = subjectPrefix + "Batch KPI Report Generation Status - " + reportData.getMonthAndYear();

        sendEmail(mailProperties.getReportNotification().getRecipients(), subject, htmlBody);
        log.info("Report notification sent successfully.");
    }

    public void sendDailyReportNotification(String day, GenerateKpiReportBatchResponse reportData, boolean isCronJob) throws MessagingException {
        log.info("Preparing to send daily report notification...");
        String htmlBody = generateDailyHtmlBody(day, reportData);
        String subjectPrefix = isCronJob ? "[DEV] Scheduled Job: " : "[DEV] Manually Triggered: ";
        String subject = subjectPrefix + "Daily MS Clarity Generation Status - " + day;

        sendEmail(mailProperties.getReportNotification().getRecipients(), subject, htmlBody);
        log.info("Report notification sent successfully.");
    }

    private String generateDailyHtmlBody(String day, GenerateKpiReportBatchResponse reportData) {
        StringBuilder htmlBuilder = new StringBuilder();

        htmlBuilder.append("<html><body>");
        htmlBuilder.append("<h1>Daily Microsoft Clarity Generation Status</h1>");
        htmlBuilder.append("<p><strong>Date:</strong> ").append(day).append("</p>");
        htmlBuilder.append("<p><strong>Status:</strong> ").append(reportData.getStatus()).append("</p>");
        htmlBuilder.append("<p><strong>Percentage Done:</strong> ").append(String.format("%.2f", reportData.getPercentageDone())).append("%</p>");
        htmlBuilder.append("<p><strong>Success Ratio:</strong> ").append(reportData.getSuccessRatio()).append("</p>");
        htmlBuilder.append("<p><strong>Total Time Elapsed:</strong> ").append(reportData.getTotalTimeElapsed()).append("</p>");

        htmlBuilder.append("<h2>KPI Reports</h2>");
        htmlBuilder.append("<table border='1' cellspacing='0' cellpadding='5'>");
        htmlBuilder.append("<tr><th>ID</th><th>Sub Agency</th><th>GHL Location ID</th><th>Status</th><th>Time Elapsed</th></tr>");

        for (GenerateKpiReportResponse report : reportData.getKpiReports()) {
            htmlBuilder.append("<tr>");
            htmlBuilder.append("<td>").append(report.getId()).append("</td>");
            htmlBuilder.append("<td>").append(report.getSubAgency()).append("</td>");
            htmlBuilder.append("<td>").append(report.getGhlLocationId()).append("</td>");
            htmlBuilder.append("<td>").append(report.getStatus()).append("</td>");
            htmlBuilder.append("<td>").append(report.getTimeElapsed()).append("</td>");
            htmlBuilder.append("</tr>");
        }

        htmlBuilder.append("</table>");

        if (!reportData.getFailedReports().isEmpty()) {
            htmlBuilder.append("<h2>Failed Reports</h2>");
            htmlBuilder.append("<ul>");
            for (String failedReport : reportData.getFailedReports()) {
                htmlBuilder.append("<li>").append(failedReport).append("</li>");
            }
            htmlBuilder.append("</ul>");
        }

        htmlBuilder.append("</body></html>");

        log.debug("HTML body generated successfully.");

        return htmlBuilder.toString();
    }

    private String generateHtmlBody(GenerateKpiReportBatchResponse reportData) {
        StringBuilder htmlBuilder = new StringBuilder();

        htmlBuilder.append("<html><body>");
        htmlBuilder.append("<h1>KPI Report Status</h1>");
        htmlBuilder.append("<p><strong>Month and Year:</strong> ").append(reportData.getMonthAndYear()).append("</p>");
        htmlBuilder.append("<p><strong>Status:</strong> ").append(reportData.getStatus()).append("</p>");
        htmlBuilder.append("<p><strong>Percentage Done:</strong> ").append(String.format("%.2f", reportData.getPercentageDone())).append("%</p>");
        htmlBuilder.append("<p><strong>Success Ratio:</strong> ").append(reportData.getSuccessRatio()).append("</p>");
        htmlBuilder.append("<p><strong>Total Time Elapsed:</strong> ").append(reportData.getTotalTimeElapsed()).append("</p>");

        htmlBuilder.append("<h2>KPI Reports</h2>");
        htmlBuilder.append("<table border='1' cellspacing='0' cellpadding='5'>");
        htmlBuilder.append("<tr><th>ID</th><th>Sub Agency</th><th>GHL Location ID</th><th>Status</th><th>Time Elapsed</th></tr>");

        for (GenerateKpiReportResponse report : reportData.getKpiReports()) {
            htmlBuilder.append("<tr>");
            htmlBuilder.append("<td>").append(report.getId()).append("</td>");
            htmlBuilder.append("<td>").append(report.getSubAgency()).append("</td>");
            htmlBuilder.append("<td>").append(report.getGhlLocationId()).append("</td>");
            htmlBuilder.append("<td>").append(report.getStatus()).append("</td>");
            htmlBuilder.append("<td>").append(report.getTimeElapsed()).append("</td>");
            htmlBuilder.append("</tr>");
        }

        htmlBuilder.append("</table>");

        if (!reportData.getFailedReports().isEmpty()) {
            htmlBuilder.append("<h2>Failed Reports</h2>");
            htmlBuilder.append("<ul>");
            for (String failedReport : reportData.getFailedReports()) {
                htmlBuilder.append("<li>").append(failedReport).append("</li>");
            }
            htmlBuilder.append("</ul>");
        }

        htmlBuilder.append("</body></html>");

        log.debug("HTML body generated successfully.");

        return htmlBuilder.toString();
    }
}