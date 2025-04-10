package org.example.tablenow.domain.auth.service;

import org.example.tablenow.domain.auth.dto.request.SigninRequest;
import org.example.tablenow.domain.auth.dto.request.SignupRequest;
import org.example.tablenow.domain.auth.dto.response.TokenResponse;
import org.example.tablenow.domain.auth.repository.RefreshTokenRepository;
import org.example.tablenow.domain.auth.dto.response.SignupResponse;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AuthServiceTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Nested
    class 회원가입 {
        SignupRequest request = SignupRequest.builder()
                .email("signup@test.com")
                .password("password123")
                .name("이름")
                .nickname("닉네임")
                .phoneNumber("01012345678")
                .userRole(UserRole.ROLE_USER)
                .build();

        // Exception Start

        @Test
        void 중복된_이메일로_가입_시_예외() {
            // given
            authService.signup(request);

            // when & then
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(HandledException.class)
                    .hasMessage(ErrorCode.DUPLICATE_EMAIL.getDefaultMessage());
        }

        // Exception End

        @Test
        void 성공() {
            // given
            SignupResponse signupResponse = authService.signup(request);

            // when
            User findUser = userRepository.findById(signupResponse.getId())
                    .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));

            // then
            assertThat(findUser)
                    .extracting("email", "name", "nickname", "phoneNumber", "userRole")
                    .containsExactly(request.getEmail(), request.getName(), request.getNickname(), request.getPhoneNumber(), request.getUserRole());
            assertThat(passwordEncoder.matches(request.getPassword(), findUser.getPassword())).isTrue();
        }
    }

    @Nested
    class 로그인 {
        SigninRequest request = SigninRequest.builder()
                .email("signin@test.com")
                .password("password123")
                .build();

        SignupRequest signupRequest = SignupRequest.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .name("이름")
                .nickname("닉네임")
                .phoneNumber("01012345678")
                .userRole(UserRole.ROLE_USER)
                .build();

        // Exception Start

        @Test
        void 해당_이메일의_사용자가_존재하지_않을_시_예외() {
            // when & then
            assertThatThrownBy(() -> authService.signin(request))
                    .isInstanceOf(HandledException.class)
                    .hasMessage(ErrorCode.USER_NOT_FOUND.getDefaultMessage());
        }

//        @Test
//        void 탈퇴한_사용자가_로그인_시도_시_예외() {
//            // given
//            UserResponse userResponse = authService.signup(signupRequest);
//            User findUser = userRepository.findById(userResponse.getId())
//                    .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));
//            // TODO: 회원(findUser) 탈퇴 API 실행
//
//            // when & then
//            assertThatThrownBy(() -> authService.signin(request))
//                    .isInstanceOf(HandledException.class)
//                    .hasMessage(ErrorCode.ALREADY_DELETED_USER.getDefaultMessage());
//        }

        @Test
        void 비밀번호가_저장된_비밀번호와_일치하지_않을_시_예외() {
            // given
            authService.signup(signupRequest);
            SigninRequest wrongPasswordRequest = SigninRequest.builder()
                    .email("signin@test.com")
                    .password("wrongPassword")
                    .build();

            // when & then
            assertThatThrownBy(() -> authService.signin(wrongPasswordRequest))
                    .isInstanceOf(HandledException.class)
                    .hasMessage(ErrorCode.INCORRECT_PASSWORD.getDefaultMessage());
        }

        // Exception End

        @Test
        void 성공() {
            // given
            authService.signup(signupRequest);

            // when
            TokenResponse tokenResponse = authService.signin(request);

            // then
            assertThat(tokenResponse.getAccessToken())
                    .isNotNull()
                    .contains("Bearer");
            assertThat(tokenResponse.getRefreshToken())
                    .isNotNull();
        }
    }

    @Nested
    class 토큰_재발급 {
        User user = User.builder()
                .email("refresh@test.com")
                .name("이름")
                .nickname("닉네임")
                .phoneNumber("01012345678")
                .userRole(UserRole.ROLE_USER)
                .build();
        User savedUser = userRepository.save(user);
        String oldRefreshToken = tokenService.createRefreshToken(savedUser);
        String oldAccessToken = tokenService.createAccessToken(savedUser);

        // Exception Start

        @Test
        void 리프레시_토큰에_저장된_유저가_존재하지_않을_시_예외() {
            // given
            userRepository.deleteById(savedUser.getId());

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(oldRefreshToken))
                    .isInstanceOf(HandledException.class)
                    .hasMessage(ErrorCode.USER_NOT_FOUND.getDefaultMessage());
        }

        // Exception End

        @Test
        void 성공() {
            // when
            TokenResponse tokenResponse = authService.refreshToken(oldRefreshToken);
            String newAccessToken = tokenResponse.getAccessToken();
            String newRefreshToken = tokenResponse.getRefreshToken();

            // then
            assertThat(newAccessToken).isNotNull();
            assertThat(newRefreshToken).isNotNull();
            assertThat(newAccessToken).isNotEqualTo(oldAccessToken);
            assertThat(newRefreshToken).isNotEqualTo(oldRefreshToken);
        }
    }

    @Nested
    class 로그아웃 {
        User user = User.builder()
                .id(-1L)
                .build();
        String refreshToken = tokenService.createRefreshToken(user);

        @Test
        void 성공() {
            // when
            authService.logout(refreshToken);

            // then
            assertThat(refreshTokenRepository.findByToken(refreshToken)).isEmpty();
        }
    }
}