package com.assignment1.backend.controller;

import com.assignment1.backend.dto.request.LoginRequest;
import com.assignment1.backend.dto.request.LogoutRequest;
import com.assignment1.backend.dto.request.RefreshTokenRequest;
import com.assignment1.backend.dto.request.RegisterRequest;
import com.assignment1.backend.dto.response.ApiResponse;
import com.assignment1.backend.dto.response.AuthResponse;
import com.assignment1.backend.entity.UserEntity;
import com.assignment1.backend.service.KeyCloakService;
import com.assignment1.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")

public class AuthController {

  @Autowired
  private UserService userService;

  @Autowired
  private KeyCloakService keyCloakService;

  @PostMapping("/register")
   public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
     try {
       userService.registerUser(
         request.getUsername(),
         request.getEmail(),
         request.getPassword(),
         request.getFirstName(),
         request.getLastName()
        );

      return ResponseEntity.status(HttpStatus.CREATED)
                             .body(ApiResponse.success("User registered successfully with KeyCloak",
                                                      "User " + request.getUsername() + " create successfully"));
     } catch (RuntimeException e) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                               .body(ApiResponse.error(e.getMessage()));
     } catch (Exception e) {
          return RequestEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                              .body(ApiResponse.error("Resgister failed: " + e.getMessage()));
     }
   }
@PostMapping("/login")
public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequest request) {
  try {
    Map<String, Object> tokenData = keyCloakService.authenticateUser(
        request.getUsername(),
        request.getPassword()
    );

    String accessToken = (String) tokenData.get("access_token");
    Map<String, Object> userInfo = keyCloakService.parseJwtPayload(accessToken);

    AuthResponse authResponse = new AuthResponse();
    authResponse.setAccessToken(accessToken);
    authResponse.setRefreshToken((String) tokenData.get("refresh_token"));
    authResponse.setExpiresIn((Integer) tokenData.get("expires_in"));
    authResponse.setRefreshExpiresIn((Integer) tokenData.get("refresh_expires_in"));
    authResponse.setTokenType((String) tokenData.get("tokenType"));

    authResponse.UserInfo user = new AuthResponse.UserInfo();
    user.setSub((String) userInfo.get("sub"));
    user.setUsername((String) userInfo.get("username"));
    user.setEmail((String) userInfo.get("email"));
    user.setFirstName((String) userInfo.get("firstName"));
    user.setLastName((String) userInfo.get("lastName"));
    authResponse.setUser(user);

    return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    
  } catch (RuntimeRxception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                         .body(ApiResponse.error(e.getMessage()));
  } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                         .bodfy(ApiResponse.error("Login failed" + e.getMessage()));
  }
}
  
}


// Few methods in AuthController, still not done yet!!!
  

