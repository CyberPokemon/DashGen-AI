package com.glitchers.backend.dto;

public class ApiResponseDTO {
    String message;

    public ApiResponseDTO() {
    }

    public ApiResponseDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
