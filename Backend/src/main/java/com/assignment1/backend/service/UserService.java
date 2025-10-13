package com.assignment1.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.assignment1.backend.dto.request.RegisterRequest;
import java.util.Map;

@Service
public class UserService {

    // @Autowired
    // private KeyCloakService keyCloakService;

    public void register(RegisterRequest registerRequest) {

        try {
            RegisterRequest request = new RegisterRequest();
            request.setUsername(registerRequest.getUsername());
            request.setEmail(registerRequest.getEmail());
            request.setPassword(registerRequest.getPassword());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user " + e.getMessage());
        }
    }
        
}
