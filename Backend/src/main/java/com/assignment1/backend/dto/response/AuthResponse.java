package com.assignment1.backend.dto.response;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;
    private Integer refreshExpiresIn;
    private String tokenType;
    private UserInfo user;

    @Data
    public static class UserInfo {
        private String sub;
        private String usernma;
        private String email;
        private String firstName;
        private String lastName;
    }
}
