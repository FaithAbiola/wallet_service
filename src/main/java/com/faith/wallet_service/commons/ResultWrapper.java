package com.faith.wallet_service.commons;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * API response wrapper: status (SUCCESS/FAILED), message, data.
 */
@JsonPropertyOrder({"status", "message", "data"})
public class ResultWrapper<T> {

    private ResultStatus status;
    private String message;
    private T data;

    public ResultWrapper() {
    }

    public ResultStatus getStatus() { return status; }
    public void setStatus(ResultStatus status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public enum ResultStatus { SUCCESS, FAILED, ERROR }
}
