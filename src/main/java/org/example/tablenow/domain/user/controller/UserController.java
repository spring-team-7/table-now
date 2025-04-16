package org.example.tablenow.domain.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.user.dto.request.UpdatePasswordRequest;
import org.example.tablenow.domain.user.dto.request.UpdateProfileRequest;
import org.example.tablenow.domain.user.dto.request.UserDeleteRequest;
import org.example.tablenow.domain.user.dto.response.SimpleUserResponse;
import org.example.tablenow.domain.user.dto.response.UserProfileResponse;
import org.example.tablenow.domain.user.service.UserService;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.util.CookieUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    @DeleteMapping("/v1/users")
    public ResponseEntity<SimpleUserResponse> deleteUser(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody UserDeleteRequest request,
            HttpServletResponse response
    ) {
        CookieUtil.deleteRefreshTokenCookie(response);
        return ResponseEntity.ok(userService.deleteUser(authUser, request));
    }

    @PatchMapping("/v1/users/password")
    public ResponseEntity<SimpleUserResponse> updatePassword(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody UpdatePasswordRequest request,
            HttpServletResponse response
    ) {
        CookieUtil.deleteRefreshTokenCookie(response);
        return ResponseEntity.ok(userService.updatePassword(authUser, request));
    }

    @GetMapping("/v1/users")
    public ResponseEntity<UserProfileResponse> getUserProfile(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(userService.getUserProfile(authUser));
    }

    @PatchMapping("/v1/users")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(userService.updateUserProfile(authUser, request));
    }
}
