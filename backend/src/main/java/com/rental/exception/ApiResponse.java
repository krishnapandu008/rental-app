package com.rental.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
    private Object data; // optional payload

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
