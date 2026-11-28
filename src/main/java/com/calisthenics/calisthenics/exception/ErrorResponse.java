package com.calisthenics.calisthenics.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String message;
    private int status;
    private LocalDateTime timestamp;
    private Map<String, String> errors;

    public ErrorResponse(String message, int status, LocalDateTime timestamp) {
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
    }

    public ErrorResponse(String message, int status, LocalDateTime timestamp, Map<String, String> errors) {
        this(message, status, timestamp);
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
