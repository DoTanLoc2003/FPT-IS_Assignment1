package com.assignment1.backend.dto.response;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private String message;
    private boolean success;
    private T data;
    private String error;

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        response.data = data;
        return response;
    }

    public static <T>ApiResponse<T> fail(String error) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.error = error;
        return response;
    }
}
