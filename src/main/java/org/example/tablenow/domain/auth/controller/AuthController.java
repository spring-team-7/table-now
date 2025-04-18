package org.example.tablenow.domain.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.auth.dto.request.SigninRequest;
import org.example.tablenow.domain.auth.dto.request.SignupRequest;
import org.example.tablenow.domain.auth.dto.response.AccessTokenResponse;
import org.example.tablenow.domain.auth.dto.response.SignupResponse;
import org.example.tablenow.domain.auth.dto.response.TokenResponse;
import org.example.tablenow.domain.auth.service.AuthService;
import org.example.tablenow.domain.auth.service.KakaoAuthService;
import org.example.tablenow.domain.auth.service.NaverAuthService;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.example.tablenow.global.util.CookieUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final KakaoAuthService kakaoAuthService;
    private final NaverAuthService naverAuthService;

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
        CookieUtil.addRefreshTokenToCookie(response, tokenResponse.getRefreshToken());
        return ResponseEntity.ok(AccessTokenResponse.fromTokenResponse(tokenResponse));
    }

    @PostMapping("/v1/auth/refresh")
    public ResponseEntity<AccessTokenResponse> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken,
            @AuthenticationPrincipal AuthUser authUser,
            HttpServletResponse response
    ) {
        if (refreshToken == null) {
            throw new HandledException(ErrorCode.REFRESH_TOKEN_MISSING);
        }

        TokenResponse tokenResponse = authService.refreshToken(refreshToken, accessToken, authUser.getId());
        CookieUtil.addRefreshTokenToCookie(response, tokenResponse.getRefreshToken());
        return ResponseEntity.ok(AccessTokenResponse.fromTokenResponse(tokenResponse));
    }

    @PostMapping("/v1/auth/logout")
    public ResponseEntity<String> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String accessToken,
            @AuthenticationPrincipal AuthUser authUser,
            HttpServletResponse response
    ) {
        if (refreshToken != null || accessToken != null) {
            authService.logout(refreshToken, accessToken, authUser.getId());
        }
        CookieUtil.deleteRefreshTokenCookie(response);
        return ResponseEntity.ok("로그아웃에 성공했습니다.");
    }

    @GetMapping("/v1/auth/kakao")
    public ResponseEntity<AccessTokenResponse> kakaoLogin(
            @RequestParam String code,
            HttpServletResponse response
    ) {
        TokenResponse tokenResponse = kakaoAuthService.login(code);

        CookieUtil.addRefreshTokenToCookie(response, tokenResponse.getRefreshToken());
        return ResponseEntity.ok(AccessTokenResponse.fromTokenResponse(tokenResponse));
    }

    @GetMapping("/v1/auth/naver")
    public ResponseEntity<AccessTokenResponse> naverLogin(
            @RequestParam String code,
            HttpServletResponse response
    ) {
        TokenResponse tokenResponse = naverAuthService.login(code);

        CookieUtil.addRefreshTokenToCookie(response, tokenResponse.getRefreshToken());
        return ResponseEntity.ok(AccessTokenResponse.fromTokenResponse(tokenResponse));
    }
}
