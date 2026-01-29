package com.faith.wallet_service.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class ErrorDetails {

    private HttpStatus httpStatus;
    private Integer statusCode;
    private LocalDateTime time;
    private String message;

    public static ErrorDetails builder() { return new ErrorDetails(); }

    public ErrorDetails httpStatus(HttpStatus v) { this.httpStatus = v; return this; }
    public ErrorDetails statusCode(int v) { this.statusCode = v; return this; }
    public ErrorDetails time(LocalDateTime v) { this.time = v; return this; }
    public ErrorDetails message(String v) { this.message = v; return this; }
    public ErrorDetails build() { return this; }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public void setHttpStatus(HttpStatus httpStatus) { this.httpStatus = httpStatus; }
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
