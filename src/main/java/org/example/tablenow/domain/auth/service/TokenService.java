package org.example.tablenow.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.auth.entity.RefreshToken;
import org.example.tablenow.domain.auth.repository.RefreshTokenRepository;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.example.tablenow.global.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    public String createAccessToken(User user) {
        return jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole(), user.getNickname());
    }

    @Transactional
    public String createRefreshToken(User user) {
        // 기존 토큰이 있다면 갱신, 없으면 생성
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId())
                .map(token -> {
                    token.updateToken();
                    return token;
                })
                .orElseGet(() -> RefreshToken.builder()
                        .userId(user.getId())
                        .build());
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        return savedToken.getToken();
    }

    @Transactional
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new HandledException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (refreshToken.isExpired()) {
            // 토큰이 만료되었을 경우 삭제
            refreshTokenRepository.delete(refreshToken);
            throw new HandledException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        return refreshToken;
    }

    @Transactional
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    @Transactional
    public void deleteRefreshTokenByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
