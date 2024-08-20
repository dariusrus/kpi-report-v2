package com.blc.kpiReport.util;

public class NumberUtil {

    public static Double roundToTwoDecimalPlaces(Double value) {
        if (value == null) { return null; }
        return Math.round(value * 100.0) / 100.0;
    }
}
