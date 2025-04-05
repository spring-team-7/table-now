package org.example.tablenow.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.auth.entity.RefreshToken;
import org.example.tablenow.domain.auth.repository.RefreshTokenRepository;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.global.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    public String createAccessToken(User user) {
        return jwtUtil.createToken(user.getId(), user.getEmail(), user.getRole(), user.getNickname());
    }

    public String createRefreshToken(User user) {
        // 기존 토큰이 있다면 삭제
        refreshTokenRepository.findByUserId(user.getId())
                .ifPresent(refreshTokenRepository::delete);

        RefreshToken newToken = refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(user.getId())
                        .build());
        return newToken.getRefreshToken();
    }
}
