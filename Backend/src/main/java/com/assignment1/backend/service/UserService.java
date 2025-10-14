package com.assignment1.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.assignment1.backend.dto.request.RegisterRequest;
import java.util.Map;

@Service
public class UserService {

     @Autowired
    private KeyCloakService keycloakService;

    public void registerUser(String username, String email, String firstName, String lastName,
            String password) {
        try {

            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setUsername(username);
            registerRequest.setEmail(email);
            registerRequest.setFirstName(firstName);
            registerRequest.setLastName(lastName);
            registerRequest.setPassword(password);

            keycloakService.createKeycloakUser(registerRequest);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }

    public boolean validatePassword(String username, String rawPassword) {
        
        try {
            keycloakService.authenticateUser(username, rawPassword);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> getUserFromToken(String token) {
        return keycloakService.parseJwtPayload(token);
    }
        
}
