package com.placement.placement_intelligence.controller;

import com.placement.placement_intelligence.dto.AuthRequest;
import com.placement.placement_intelligence.dto.AuthResponse;
import com.placement.placement_intelligence.dto.GoogleLoginRequest;
import com.placement.placement_intelligence.dto.SignupRequest;
import com.placement.placement_intelligence.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicAuthController {

    private final AuthService authService;

    public PublicAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/google-login")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.googleLogin(request));
    }
}
