package org.example.tablenow.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.auth.dto.token.RefreshToken;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.example.tablenow.global.constant.SecurityConstants;
import org.example.tablenow.global.util.JwtUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    public String createAccessToken(User user) {
        return jwtUtil.createToken(
                user.getId(),
                user.getEmail(),
                user.getUserRole(),
                user.getNickname()
        );
    }

    public String createRefreshToken(User user) {
        String refreshToken = UUID.randomUUID().toString();
        String redisKey = SecurityConstants.REFRESH_TOKEN_KEY_PREFIX + refreshToken;

        // userId를 값으로 Redis에 저장
        redisTemplate.opsForValue().set(
                redisKey,
                String.valueOf(user.getId()),
                SecurityConstants.REFRESH_TOKEN_TTL_SECONDS,
                TimeUnit.SECONDS
        );

        return refreshToken;
    }

    public RefreshToken validateRefreshToken(String refreshToken) {
        String redisKey = SecurityConstants.REFRESH_TOKEN_KEY_PREFIX + refreshToken;
        String userIdValue = redisTemplate.opsForValue().get(redisKey);

        if (userIdValue == null) {
            throw new HandledException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        return RefreshToken.builder()
                .token(refreshToken)
                .userId(Long.valueOf(userIdValue))
                .build();
    }

    public void deleteRefreshToken(String refreshToken) {
        String redisKey = SecurityConstants.REFRESH_TOKEN_KEY_PREFIX + refreshToken;
        Boolean deleted = redisTemplate.delete(redisKey);

        if (!deleted) {
            log.warn("삭제 시도한 RefreshToken이 Redis에 존재하지 않아 삭제되지 않음. 토큰값: {}", refreshToken);
        }
    }

    public void addToBlacklist(String accessToken, Long userId) {
        String jwt = jwtUtil.substringToken(accessToken);
        String redisKey = SecurityConstants.BLACKLIST_TOKEN_KEY_PREFIX + jwt;
        long ttl = jwtUtil.getRemainingTokenTime(jwt);
        String value = String.format("logout:userId=%d", userId);

        redisTemplate.opsForValue().set(redisKey, value, ttl, TimeUnit.SECONDS);
        log.info("AccessToken 블랙리스트 등록 완료: {} (TTL {}초)", jwt, ttl);
    }
}
