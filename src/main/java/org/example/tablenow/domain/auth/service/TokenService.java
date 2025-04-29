package org.example.tablenow.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.auth.dto.token.RefreshToken;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.global.constant.RedisKeyConstants;
import org.example.tablenow.global.constant.SecurityConstants;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.example.tablenow.global.security.enums.BlacklistReason;
import org.example.tablenow.global.util.JwtUtil;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final StringRedisTemplate redisTemplate;
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
        String redisKey = RedisKeyConstants.REFRESH_TOKEN_KEY_PREFIX + refreshToken;

        try {
            // userId를 값으로 Redis에 저장
            redisTemplate.opsForValue().set(
                    redisKey,
                    String.valueOf(user.getId()),
                    SecurityConstants.REFRESH_TOKEN_TTL_SECONDS,
                    TimeUnit.SECONDS
            );
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            log.warn("[Redis] RefreshToken Redis 저장 실패: {}", e.getMessage());
            throw new HandledException(ErrorCode.REDIS_CONNECTION_ERROR);
        }

        return refreshToken;
    }

    public RefreshToken validateRefreshToken(String refreshToken, Long userId) {
        String redisKey = RedisKeyConstants.REFRESH_TOKEN_KEY_PREFIX + refreshToken;

        try {
            String userIdValue = redisTemplate.opsForValue().get(redisKey);

            if (userIdValue == null) {
                throw new HandledException(ErrorCode.EXPIRED_REFRESH_TOKEN);
            }
            if (!userIdValue.equals(String.valueOf(userId))) {
                throw new HandledException(ErrorCode.INVALID_REFRESH_TOKEN_OWNER);
            }

            return RefreshToken.builder()
                    .token(refreshToken)
                    .userId(Long.valueOf(userIdValue))
                    .build();
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            log.error("[Redis] RefreshToken 검증 중 Redis 오류 발생", e);
            throw new HandledException(ErrorCode.REDIS_CONNECTION_ERROR);
        }
    }

    public void deleteRefreshToken(String refreshToken) {
        String redisKey = RedisKeyConstants.REFRESH_TOKEN_KEY_PREFIX + refreshToken;

        try {
            Boolean deleted = redisTemplate.delete(redisKey);
            if (!deleted) {
                log.warn("[Redis] 삭제 시도한 RefreshToken이 Redis에 존재하지 않아 삭제되지 않음. 토큰값: {}", refreshToken);
            }
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            log.warn("[Redis] RefreshToken 삭제 중 Redis 오류 발생: {}", e.getMessage());
        }
    }

    public void addToBlacklist(String accessToken, Long userId, BlacklistReason reason) {
        try {
            String jwt = jwtUtil.substringToken(accessToken);
            String redisKey = RedisKeyConstants.BLACKLIST_TOKEN_KEY_PREFIX + jwt;
            long ttl = jwtUtil.getRemainingTokenTime(jwt);
            String value = String.format("%s:userId=%d", reason.getValue(), userId);

            redisTemplate.opsForValue().set(redisKey, value, ttl, TimeUnit.SECONDS);
            log.info("[Redis] AccessToken 블랙리스트 등록 완료: {} (TTL {}초)", jwt, ttl);
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            log.warn("[Redis] AccessToken 블랙리스트 등록 실패: {}", e.getMessage());
        }
    }
}
