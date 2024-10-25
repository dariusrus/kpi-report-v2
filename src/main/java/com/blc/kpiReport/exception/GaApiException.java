package com.blc.kpiReport.exception;

public class GaApiException extends Exception {
    public GaApiException(String message) {
        super(message);
    }

    public GaApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
