package org.example.tablenow.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.auth.dto.request.SignupRequest;
import org.example.tablenow.domain.auth.service.AuthService;
import org.example.tablenow.domain.user.dto.response.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }
}
