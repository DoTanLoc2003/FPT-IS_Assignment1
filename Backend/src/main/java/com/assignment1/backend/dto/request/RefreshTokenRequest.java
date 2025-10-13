package com.assignment1.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message="Refresh token is required")
    private String refreshToken;
}
