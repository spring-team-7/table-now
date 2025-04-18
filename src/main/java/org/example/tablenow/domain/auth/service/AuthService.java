package org.example.tablenow.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.auth.dto.request.SigninRequest;
import org.example.tablenow.domain.auth.dto.request.SignupRequest;
import org.example.tablenow.domain.auth.dto.response.SignupResponse;
import org.example.tablenow.domain.auth.dto.response.TokenResponse;
import org.example.tablenow.domain.auth.dto.token.RefreshToken;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.domain.user.service.UserService;
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
    private final UserService userService;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new HandledException(ErrorCode.DUPLICATE_EMAIL);
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

        return SignupResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .nickname(savedUser.getNickname())
                .phoneNumber(savedUser.getPhoneNumber())
                .userRole(savedUser.getUserRole().name())
                .build();
    }

    public TokenResponse signin(SigninRequest request) {
        User user = userService.getUserByEmail(request.getEmail());

        if (user.getDeletedAt() != null) {
            throw new HandledException(ErrorCode.ALREADY_DELETED_USER);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new HandledException(ErrorCode.INCORRECT_PASSWORD);
        }

        return generateTokenResponse(user);
    }

    public TokenResponse refreshToken(String token) {
        RefreshToken refreshToken = tokenService.validateRefreshToken(token);
        tokenService.deleteRefreshToken(token);
        User user = userService.getUser(refreshToken.getUserId());

        return generateTokenResponse(user);
    }

    public void logout(String refreshToken, String accessToken, Long userId) {
        if (refreshToken != null) {
            tokenService.deleteRefreshToken(refreshToken);
        }
        if (accessToken != null) {
            tokenService.addToBlacklist(accessToken, userId);
        }
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
