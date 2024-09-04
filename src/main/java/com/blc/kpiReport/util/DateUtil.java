package com.blc.kpiReport.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static FormattedDateRange getFormattedDateRange(int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);

        return new FormattedDateRange(startDateStr, endDateStr);
    }

    public static String formatMonthAndYear(int month, int year) {
        LocalDate date = LocalDate.of(year, month, 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM, yyyy");
        return date.format(formatter);
    }

    public static String formatDayMonthYear(int day, int month, int year) {
        LocalDate date = LocalDate.of(year, month, day);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        return date.format(formatter);
    }

    public static String convertDate(String date) {
        var inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        var outputFormatter = DateTimeFormatter.ofPattern("MMMM, yyyy");
        var localDate = LocalDate.parse(date, inputFormatter);
        return localDate.format(outputFormatter);
    }

    public static String formatDate(String date) {
        return date.substring(5, 7) + "-" + date.substring(8) + "-" + date.substring(0, 4);
    }

    public static String subtractOneYear(String date) {
        LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
        LocalDate updatedDate = localDate.minusYears(2);
        return updatedDate.format(DATE_FORMATTER);
    }

    public record FormattedDateRange(String startDate, String endDate) {}
}