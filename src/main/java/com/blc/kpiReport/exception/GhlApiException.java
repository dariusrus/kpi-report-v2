package com.blc.kpiReport.exception;

public class GhlApiException extends Exception {
    public GhlApiException(String message) {
        super(message);
    }

    public GhlApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
