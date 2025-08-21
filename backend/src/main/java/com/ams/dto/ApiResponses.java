package com.ams.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponses<T> {

    private boolean success;
    private T data;
    private String error;
    private String message;

    // Default constructor
    public ApiResponses() {
    }

    // Constructor for success with data
    public ApiResponses(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    // Constructor for success with data and message
    public ApiResponses(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    // Constructor for error
    public ApiResponses(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    // Static factory methods
    public static <T> ApiResponses<T> success(T data) {
        return new ApiResponses<>(true, data);
    }

    public static <T> ApiResponses<T> success(T data, String message) {
        return new ApiResponses<>(true, data, message);
    }

    public static <T> ApiResponses<T> successMessage(String message) {
        ApiResponses<T> response = new ApiResponses<>();
        response.setSuccess(true);
        response.setMessage(message);
        return response;
    }

    public static <T> ApiResponses<T> error(String error) {
        return new ApiResponses<>(false, error);
    }

    public static <T> ApiResponses<T> error(String error, String message) {
        ApiResponses<T> response = new ApiResponses<>(false, error);
        response.setMessage(message);
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ApiResponses{" +
                "success=" + success +
                ", data=" + data +
                ", error='" + error + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}