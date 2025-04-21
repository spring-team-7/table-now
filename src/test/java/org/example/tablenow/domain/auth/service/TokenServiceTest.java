package org.example.tablenow.domain.auth.service;

import org.example.tablenow.domain.auth.dto.token.RefreshToken;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.constant.SecurityConstants;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.example.tablenow.global.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ValueOperations<String, String> ops;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setupOps() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(ops);
    }

    @Nested
    class 액세스_토큰_생성 {
        User user = User.builder()
                .id(USER_ID)
                .email("access@test.com")
                .userRole(UserRole.ROLE_USER)
                .nickname("닉네임")
                .build();

        @Test
        void 사용자정보로_액세스_토큰_생성_성공() {
            // given
            String fakeToken = "Bearer fake.jwt.token";

            given(jwtUtil.createToken(
                    eq(user.getId()),
                    eq(user.getEmail()),
                    eq(user.getUserRole()),
                    eq(user.getNickname())
            )).willReturn(fakeToken);

            // when
            String accessToken = tokenService.createAccessToken(user);

            // then
            assertThat(accessToken).isEqualTo(fakeToken);
            verify(jwtUtil).createToken(
                    eq(user.getId()),
                    eq(user.getEmail()),
                    eq(user.getUserRole()),
                    eq(user.getNickname())
            );
        }
    }

    @Nested
    class 리프레시_토큰_생성 {
        User user = User.builder()
                .id(USER_ID)
                .build();

        @Test
        void Redis_장애_발생시_RefreshToken_저장_실패_예외처리() {
            // given
            willThrow(new RedisConnectionFailureException("Redis 연결 중 오류 발생"))
                    .given(ops)
                    .set(startsWith(SecurityConstants.REFRESH_TOKEN_KEY_PREFIX), eq(USER_ID.toString()), anyLong(), eq(TimeUnit.SECONDS));

            // when & then
            assertThatThrownBy(() -> tokenService.createRefreshToken(user))
                    .isInstanceOf(HandledException.class)
                    .hasMessage(ErrorCode.REDIS_CONNECTION_ERROR.getDefaultMessage());
        }

        @Test
        void 사용자ID로_리프레시_토큰을_생성하고_Redis에_저장_성공() {
            // when
            String refreshToken = tokenService.createRefreshToken(user);

            // then
            verify(redisTemplate.opsForValue())
                    .set(startsWith(SecurityConstants.REFRESH_TOKEN_KEY_PREFIX), eq(USER_ID.toString()), anyLong(), eq(TimeUnit.SECONDS));
            assertThat(refreshToken).isNotNull();
        }
    }


    @Nested
    class 리프레시_토큰_검증 {

        @Test
        void Redis에_존재하지_않는_토큰으로_검증_시_예외처리() {
            // given
            String token = "invalid";
            given(ops.get(SecurityConstants.REFRESH_TOKEN_KEY_PREFIX + token)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> tokenService.validateRefreshToken(token, USER_ID))
                    .isInstanceOf(HandledException.class)
                    .hasMessage(ErrorCode.EXPIRED_REFRESH_TOKEN.getDefaultMessage());
        }

        @Test
        void Redis에_저장된_userId와_다른_사용자가_요청시_예외처리() {
            // given
            String token = UUID.randomUUID().toString();
            String redisKey = SecurityConstants.REFRESH_TOKEN_KEY_PREFIX + token;
            given(ops.get(redisKey)).willReturn("999");

            // when & then
            assertThatThrownBy(() -> tokenService.validateRefreshToken(token, USER_ID))
                    .isInstanceOf(HandledException.class)
                    .hasMessage(ErrorCode.INVALID_REFRESH_TOKEN_OWNER.getDefaultMessage());
        }

        @Test
        void Redis_장애_발생시_RefreshToken_검증_실패_예외처리() {
            // given
            String token = UUID.randomUUID().toString();
            given(ops.get(anyString()))
                    .willThrow(new RedisSystemException("Redis 연결 중 오류 발생", new RuntimeException()));

            // when & then
            assertThatThrownBy(() -> tokenService.validateRefreshToken(token, USER_ID))
                    .isInstanceOf(HandledException.class)
                    .hasMessage(ErrorCode.REDIS_CONNECTION_ERROR.getDefaultMessage());
        }

        @Test
        void Redis에_저장된_토큰으로_검증_시_RefreshToken_반환_성공() {
            // given
            String token = UUID.randomUUID().toString();
            String redisKey = SecurityConstants.REFRESH_TOKEN_KEY_PREFIX + token;
            given(ops.get(redisKey)).willReturn(USER_ID.toString());

            // when
            RefreshToken result = tokenService.validateRefreshToken(token, USER_ID);

            // then
            assertAll(
                    () -> assertThat(result.getUserId()).isEqualTo(USER_ID),
                    () -> assertThat(result.getToken()).isEqualTo(token)
            );
        }
    }

    @Nested
    class 리프레시_토큰_삭제 {

        @Test
        void Redis에서_리프레시_토큰_삭제_성공() {
            // given
            String token = UUID.randomUUID().toString();
            String redisKey = SecurityConstants.REFRESH_TOKEN_KEY_PREFIX + token;

            // when
            tokenService.deleteRefreshToken(token);

            // then
            verify(redisTemplate).delete(redisKey);
        }
    }
}