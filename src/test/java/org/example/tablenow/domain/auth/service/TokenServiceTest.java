package org.example.tablenow.domain.auth.service;

import io.jsonwebtoken.Claims;
import org.example.tablenow.domain.auth.entity.RefreshToken;
import org.example.tablenow.domain.auth.repository.RefreshTokenRepository;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.example.tablenow.global.util.JwtUtil;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Nested
    class 액세스_토큰_생성 {
        User user = User.builder()
                .id(-1L)
                .email("accress@test.com")
                .userRole(UserRole.ROLE_USER)
                .nickname("닉네임")
                .build();

        @Test
        void 성공() {
            // when
            String accessToken = tokenService.createAccessToken(user);

            // then
            assertThat(accessToken).isNotNull();
            assertThat(accessToken).startsWith("Bearer ");

            String tokenWithoutPrefix = jwtUtil.substringToken(accessToken);
            Claims claims = jwtUtil.extractClaims(tokenWithoutPrefix);

            assertThat(claims.getSubject()).isEqualTo(String.valueOf(user.getId()));
            assertThat(claims.get("email")).isEqualTo(user.getEmail());
            assertThat(claims.get("userRole")).isEqualTo(user.getUserRole().name());
            assertThat(claims.get("nickname")).isEqualTo(user.getNickname());
        }
    }

    @Nested
    class 리프레시_토큰_생성 {
        User user = User.builder()
                .id(-1L)
                .build();

        @Test
        void 성공() {
            // when
            String refreshToken = tokenService.createRefreshToken(user);

            // then
            assertThat(refreshToken).isNotNull();
            assertThat(refreshTokenRepository.findByUserId(user.getId())).isPresent();
        }

        @Test
        void 기존_리프레시_토큰_존재_시_갱신() {
            // given
            String oldToken = tokenService.createRefreshToken(user);

            // when
            String newToken = tokenService.createRefreshToken(user);

            // then
            assertThat(newToken).isNotNull();
            assertThat(newToken).isNotEqualTo(oldToken);
            assertThat(refreshTokenRepository.findByUserId(user.getId())).isPresent();
        }
    }


    @Nested
    class 리프레시_토큰_검증 {
//        @Test
//        void 존재하지_않는_리프레시_토큰_예외() {
//            // when & then
//            assertThatThrownBy(() -> tokenService.validateRefreshToken("invalidToken"))
//                    .isInstanceOf(HandledException.class)
//                    .hasMessage(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getDefaultMessage());
//        }

        @Test
        void 만료된_리프레시_토큰_예외() {
            // given
            RefreshToken expiredToken = RefreshToken.builder()
                    .userId(-1L)
                    .build();
            expiredToken.expireToken();
            refreshTokenRepository.save(expiredToken);

            // when & then
            assertThatThrownBy(() -> tokenService.validateRefreshToken(expiredToken.getToken()))
                    .isInstanceOf(HandledException.class)
                    .hasMessage(ErrorCode.EXPIRED_REFRESH_TOKEN.getDefaultMessage());
            assertThat(refreshTokenRepository.findByToken(expiredToken.getToken())).isEmpty();
        }
    }

    @Nested
    class 토큰_삭제 {
        User user = User.builder()
                .id(-1L)
                .build();

        @Test
        void 리프레시토큰_삭제_성공() {
            // given
            String refreshToken = tokenService.createRefreshToken(user);

            // when
            tokenService.deleteRefreshToken(refreshToken);

            // then
            assertThat(refreshTokenRepository.findByToken(refreshToken)).isEmpty();
        }
    }
}