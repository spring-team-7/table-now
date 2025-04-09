package org.example.tablenow.domain.user.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.user.dto.request.UpdatePasswordRequest;
import org.example.tablenow.domain.user.dto.request.UserDeleteRequest;
import org.example.tablenow.domain.user.dto.response.SimpleUserResponse;
import org.example.tablenow.domain.user.service.UserService;
import org.example.tablenow.global.dto.AuthUser;
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
            @Valid @RequestBody UserDeleteRequest request,
            HttpServletResponse response
    ) {
        // 쿠키에서 리프레시 토큰 제거
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok(userService.deleteUser(authUser, request));
    }

    @PatchMapping("v1/users/password")
    public ResponseEntity<SimpleUserResponse> updatePassword(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        return ResponseEntity.ok(userService.updatePassword(authUser, request));
    }

}
