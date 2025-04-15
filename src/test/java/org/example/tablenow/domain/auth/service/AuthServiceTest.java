package org.example.tablenow.domain.auth.service;

import org.example.tablenow.domain.auth.dto.request.SigninRequest;
import org.example.tablenow.domain.auth.dto.request.SignupRequest;
import org.example.tablenow.domain.auth.dto.response.SignupResponse;
import org.example.tablenow.domain.auth.dto.response.TokenResponse;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.domain.user.service.UserService;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Transactional
class AuthServiceTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    private SignupRequest signupRequest;
    private SigninRequest signinRequest;

    @BeforeEach
    void setUp() {
        signupRequest = SignupRequest.builder()
                .email("user@test.com")
                .password("password")
                .name("이름")
                .nickname("닉네임")
                .phoneNumber("01012345678")
                .userRole(UserRole.ROLE_USER)
                .build();

        signinRequest = SigninRequest.builder()
                .email(signupRequest.getEmail())
                .password(signupRequest.getPassword())
                .build();
    }

    @Nested
    class 회원가입 {

        @Test
        void 중복된_이메일로_가입_시_예외() {
            // given
            authService.signup(signupRequest);

            // when & then
            assertThatThrownBy(() -> authService.signup(signupRequest))
                    .isInstanceOf(HandledException.class)
                    .hasMessage(ErrorCode.DUPLICATE_EMAIL.getDefaultMessage());
        }

        @Test
        void 회원가입_성공() {
            // when
            SignupResponse signupResponse = authService.signup(signupRequest);
            User savedUser = userService.getUser(signupResponse.getId());

            // then
            assertAll(
                    () -> assertThat(savedUser)
                            .extracting("email", "name", "nickname", "phoneNumber", "userRole")
                            .containsExactly(signupRequest.getEmail(), signupRequest.getName(), signupRequest.getNickname(), signupRequest.getPhoneNumber(), signupRequest.getUserRole()),
                    () -> assertThat(passwordEncoder.matches(signupRequest.getPassword(), savedUser.getPassword())).isTrue()
            );
        }
    }

    @Nested
    class 로그인 {

        private SignupResponse signupResponse;

        @BeforeEach
        void setupSignup() {
            signupResponse = authService.signup(signupRequest);
        }

        @Test
        void 존재하지_않는_이메일로_로그인_시_예외() {
            // given
            SigninRequest invalidRequest = SigninRequest.builder()
                    .email("invalid@test.com")
                    .password("password")
                    .build();

            // when & then
            assertThatThrownBy(() -> authService.signin(invalidRequest))
                    .isInstanceOf(HandledException.class)
                    .hasMessage(ErrorCode.USER_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 탈퇴한_사용자가_로그인_시도_시_예외() {
            // given
            User savedUser = userService.getUser(signupResponse.getId());
            savedUser.deleteUser();

            // when & then
            assertThatThrownBy(() -> authService.signin(signinRequest))
                    .isInstanceOf(HandledException.class)
                    .hasMessage(ErrorCode.ALREADY_DELETED_USER.getDefaultMessage());
        }

        @Test
        void 비밀번호가_저장된_비밀번호와_일치하지_않을_시_예외() {
            // given
            SigninRequest wrongPasswordRequest = SigninRequest.builder()
                    .email("user@test.com")
                    .password("wrongPassword")
                    .build();

            // when & then
            assertThatThrownBy(() -> authService.signin(wrongPasswordRequest))
                    .isInstanceOf(HandledException.class)
                    .hasMessage(ErrorCode.INCORRECT_PASSWORD.getDefaultMessage());
        }

        @Test
        void 로그인_성공() {
            // when
            TokenResponse tokenResponse = authService.signin(signinRequest);

            // then
            assertAll(
                    () -> assertThat(tokenResponse.getAccessToken()).startsWith("Bearer"),
                    () -> assertThat(tokenResponse.getRefreshToken()).isNotNull()
            );
        }
    }

    @Nested
    class 토큰_재발급 {

        private String oldRefreshToken;
        private String oldAccessToken;

        @BeforeEach
        void setupTokens() {
            SignupResponse response = authService.signup(signupRequest);
            User savedUser = userService.getUser(response.getId());
            oldRefreshToken = tokenService.createRefreshToken(savedUser);
            oldAccessToken = tokenService.createAccessToken(savedUser);
        }

        @Test
        void 토큰_재발급_성공() {
            // when
            TokenResponse tokenResponse = authService.refreshToken(oldRefreshToken);
            String newAccessToken = tokenResponse.getAccessToken();
            String newRefreshToken = tokenResponse.getRefreshToken();

            // then
            assertAll(
                    () -> assertThat(newAccessToken).isNotNull(),
                    () -> assertThat(newRefreshToken).isNotNull(),
                    () -> assertThat(newAccessToken).isNotEqualTo(oldAccessToken),
                    () -> assertThat(newRefreshToken).isNotEqualTo(oldRefreshToken)
            );
        }
    }

    @Nested
    class 로그아웃 {

        private String refreshToken;
        private User savedUser;

        @BeforeEach
        void setupLogout() {
            SignupRequest request = SignupRequest.builder()
                    .email("logout@test.com")
                    .password("password")
                    .name("로그아웃")
                    .nickname("닉네임")
                    .phoneNumber("01000000000")
                    .userRole(UserRole.ROLE_USER)
                    .build();

            SignupResponse response = authService.signup(request);
            savedUser = userService.getUser(response.getId());
            refreshToken = tokenService.createRefreshToken(savedUser);
        }

        @Test
        void 로그아웃_시_리프레시토큰_Redis에서_삭제_성공() {
            // when
            authService.logout(refreshToken);

            // then
            assertThatThrownBy(() -> tokenService.validateRefreshToken(refreshToken))
                    .isInstanceOf(HandledException.class)
                    .hasMessage(ErrorCode.EXPIRED_REFRESH_TOKEN.getDefaultMessage());
        }
    }
}