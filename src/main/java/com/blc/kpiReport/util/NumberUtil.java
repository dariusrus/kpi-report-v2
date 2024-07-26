package com.blc.kpiReport.util;

public class NumberUtil {

    public static double roundToTwoDecimalPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
