package org.example.tablenow.domain.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.auth.dto.request.SigninRequest;
import org.example.tablenow.domain.auth.dto.request.SignupRequest;
import org.example.tablenow.domain.auth.dto.response.AccessTokenResponse;
import org.example.tablenow.domain.auth.dto.response.TokenResponse;
import org.example.tablenow.domain.auth.service.AuthService;
import org.example.tablenow.domain.auth.service.KakaoAuthService;
import org.example.tablenow.domain.auth.service.NaverAuthService;
import org.example.tablenow.domain.auth.dto.response.SignupResponse;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final KakaoAuthService kakaoAuthService;
    private final NaverAuthService naverAuthService;
    private static final int REFRESH_TOKEN_TIME = 7 * 24 * 60 * 60; // 7일

    @PostMapping("/v1/auth/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/v1/auth/signin")
    public ResponseEntity<AccessTokenResponse> signin(
            @Valid @RequestBody SigninRequest request,
            HttpServletResponse response
    ) {
        TokenResponse tokenResponse = authService.signin(request);
        addRefreshTokenToCookie(response, tokenResponse.getRefreshToken());
        return ResponseEntity.ok(AccessTokenResponse.fromTokenResponse(tokenResponse));
    }

    @PostMapping("/v1/auth/refresh")
    public ResponseEntity<AccessTokenResponse> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null) {
            throw new HandledException(ErrorCode.REFRESH_TOKEN_MISSING);
        }

        TokenResponse tokenResponse = authService.refreshToken(refreshToken);
        addRefreshTokenToCookie(response, tokenResponse.getRefreshToken());
        return ResponseEntity.ok(AccessTokenResponse.fromTokenResponse(tokenResponse));
    }

    @PostMapping("/v1/auth/logout")
    public ResponseEntity<String> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        // 리프레시 토큰이 존재할 경우 DB에서 삭제
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        // 쿠키에서 리프레시 토큰 제거
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok("로그아웃에 성공했습니다.");
    }

    @GetMapping("/v1/auth/kakao")
    public ResponseEntity<AccessTokenResponse> kakaoLogin(
            @RequestParam String code,
            HttpServletResponse response
    ) {
        TokenResponse tokenResponse = kakaoAuthService.login(code);

        addRefreshTokenToCookie(response, tokenResponse.getRefreshToken());
        return ResponseEntity.ok(AccessTokenResponse.fromTokenResponse(tokenResponse));
    }

    @GetMapping("/v1/auth/naver")
    public ResponseEntity<AccessTokenResponse> naverLogin(
            @RequestParam String code,
            HttpServletResponse response
    ) {
        TokenResponse tokenResponse = naverAuthService.login(code);

        addRefreshTokenToCookie(response, tokenResponse.getRefreshToken());
        return ResponseEntity.ok(AccessTokenResponse.fromTokenResponse(tokenResponse));
    }

    private void addRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(REFRESH_TOKEN_TIME);
        response.addCookie(cookie);
    }
}
