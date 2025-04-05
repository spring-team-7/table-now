package org.example.tablenow.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.auth.dto.request.SigninRequest;
import org.example.tablenow.domain.auth.dto.request.SignupRequest;
import org.example.tablenow.domain.auth.dto.response.TokenResponse;
import org.example.tablenow.domain.auth.entity.RefreshToken;
import org.example.tablenow.domain.user.dto.response.UserResponse;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new HandledException(ErrorCode.CONFLICT, "이미 가입된 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User newUser = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .nickname(request.getNickname())
                .phoneNumber(request.getPhoneNumber())
                .userRole(request.getUserRole())
                .build();

        User savedUser = userRepository.save(newUser);

        return UserResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .nickname(savedUser.getNickname())
                .phoneNumber(savedUser.getPhoneNumber())
                .userRole(savedUser.getUserRole().name())
                .build();
    }

    @Transactional
    public TokenResponse signin(SigninRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new HandledException(ErrorCode.NOT_FOUND, "해당 유저를 찾을 수 없습니다."));

        if (user.getDeletedAt() != null) {
            throw new HandledException(ErrorCode.AUTHORIZATION, "이미 탈퇴한 사용자입니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new HandledException(ErrorCode.AUTHORIZATION, "비밀번호가 일치하지 않습니다.");
        }

        return generateTokenResponse(user);
    }

    @Transactional
    public TokenResponse refreshToken(String token) {
        RefreshToken refreshToken = tokenService.validateRefreshToken(token);

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new HandledException(ErrorCode.NOT_FOUND, "해당 유저를 찾을 수 없습니다."));

        return generateTokenResponse(user);
    }

    private TokenResponse generateTokenResponse(User user) {
        // Access & Refresh Token 생성
        String accessToken = tokenService.createAccessToken(user);
        String refreshToken = tokenService.createRefreshToken(user);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
