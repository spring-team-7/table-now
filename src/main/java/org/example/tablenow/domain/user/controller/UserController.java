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
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.example.tablenow.global.util.CookieUtil;
import org.springframework.http.HttpHeaders;
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
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody UserDeleteRequest request,
            HttpServletResponse response
    ) {
        checkRefreshTokenExists(refreshToken);
        SimpleUserResponse deletedUserResponse =
                userService.deleteUser(authUser, request, refreshToken, accessToken);
        CookieUtil.deleteRefreshTokenCookie(response);
        return ResponseEntity.ok(deletedUserResponse);
    }

    @PatchMapping("/v1/users/password")
    public ResponseEntity<SimpleUserResponse> updatePassword(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody UpdatePasswordRequest request,
            HttpServletResponse response
    ) {
        checkRefreshTokenExists(refreshToken);
        SimpleUserResponse updatedPasswordResponse =
                userService.updatePassword(authUser, request, refreshToken, accessToken);
        CookieUtil.deleteRefreshTokenCookie(response);
        return ResponseEntity.ok(updatedPasswordResponse);
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

    private void checkRefreshTokenExists(String refreshToken) {
        if (refreshToken == null) {
            throw new HandledException(ErrorCode.REFRESH_TOKEN_MISSING);
        }
    }
}
