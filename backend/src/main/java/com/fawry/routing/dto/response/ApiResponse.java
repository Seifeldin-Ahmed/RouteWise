package com.fawry.routing.dto.response;

public record ApiResponse(String status, String message) {

    public static ApiResponse error(String message) {
        return new ApiResponse("error", message);
    }
}
