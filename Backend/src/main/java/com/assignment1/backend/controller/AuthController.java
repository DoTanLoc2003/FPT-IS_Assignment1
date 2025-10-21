package com.assignment1.backend.controller;

import com.assignment1.backend.dto.request.LoginRequest;
import com.assignment1.backend.dto.request.LogoutRequest;
import com.assignment1.backend.dto.request.RefreshTokenRequest;
import com.assignment1.backend.dto.request.RegisterRequest;
import com.assignment1.backend.dto.response.ApiResponse;
import com.assignment1.backend.dto.response.AuthResponse;
import com.assignment1.backend.service.KeyCloakService;
import com.assignment1.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

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
          request.getLastName());

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(ApiResponse.success("User registered successfully with KeyCloak",
              "User " + request.getUsername() + " create successfully"));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.fail(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.fail("Resgister failed: " + e.getMessage()));
    }
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
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
      authResponse.setTokenType((String) tokenData.get("token_type"));

      AuthResponse.UserInfo user = new AuthResponse.UserInfo();
      user.setSub((String) userInfo.get("sub"));
      user.setUsername((String) userInfo.get("username"));
      user.setEmail((String) userInfo.get("email"));
      user.setFirstName((String) userInfo.get("firstName"));
      user.setLastName((String) userInfo.get("lastName"));
      authResponse.setUser(user);

      return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));

    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.fail(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.fail("Login failed" + e.getMessage()));
    }
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
    try {
      Map<String, Object> tokenData = keyCloakService.refreshToken(request.getRefreshToken());

      AuthResponse authResponse = new AuthResponse();
      authResponse.setAccessToken((String) tokenData.get("access_token"));
      authResponse.setRefreshToken((String) tokenData.get("refresh_token"));
      authResponse.setExpiresIn((Integer) tokenData.get("expires_in"));
      authResponse.setRefreshExpiresIn((Integer) tokenData.get("refresh_expires_in"));
      authResponse.setTokenType((String) tokenData.get("token_type"));

      return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authResponse));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.fail(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.fail("Refresh token failed: " + e.getMessage()));
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
    try {
      keyCloakService.logoutUser(request.getRefreshToken());
      return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.fail(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.fail("Logout failed: " + e.getMessage()));
    }
  }

  @GetMapping("/verify")
  public ResponseEntity<ApiResponse<Map<String, Object>>> verifyToken(
      @RequestHeader("Authorization") String authHeader) {
    try {
      if (!authHeader.startsWith("Bearer")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.fail("Invalid Authorization header"));
      }

      String token = authHeader.substring(7);
      Map<String, Object> payload = keyCloakService.parseJwtPayload(token);
      return ResponseEntity.ok(ApiResponse.success("Token is valid", payload));

    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.fail(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.fail("Token verification failed: " + e.getMessage()));
    }
  }

  @GetMapping("/profile")
  public ResponseEntity<ApiResponse<Map<String, Object>>> getUserProfile(
      @RequestHeader("Authorization") String authHeadder) {
    try {
      if (authHeadder == null || !authHeadder.startsWith("Bearer ")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.fail("Missing or invalid Authorization header"));
      }

      String token = authHeadder.substring(7);
      Map<String, Object> userInfo = keyCloakService.parseJwtPayload(token);

      Map<String, Object> profile = new HashMap<>();
      profile.put("username", userInfo.get("username"));
      profile.put("email", userInfo.get("email"));
      profile.put("firstName", userInfo.get("firstName"));
      profile.put("lastName", userInfo.get("lastName"));
      profile.put("sub", userInfo.get("sub"));

      return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully!!", profile));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.fail(e.getMessage()));
    }
  }
  
}
