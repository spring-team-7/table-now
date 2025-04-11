package org.example.tablenow.domain.user.service;

import org.example.tablenow.domain.auth.service.TokenService;
import org.example.tablenow.domain.image.service.ImageService;
import org.example.tablenow.domain.user.dto.request.UpdatePasswordRequest;
import org.example.tablenow.domain.user.dto.request.UpdateProfileRequest;
import org.example.tablenow.domain.user.dto.request.UserDeleteRequest;
import org.example.tablenow.domain.user.dto.response.SimpleUserResponse;
import org.example.tablenow.domain.user.dto.response.UserProfileResponse;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenService tokenService;
    @Mock
    private ImageService imageService;

    @InjectMocks
    private UserService userService;

    Long userId = 1L;
    AuthUser authUser = new AuthUser(userId, "user@test.com", UserRole.ROLE_USER, "일반회원");
    User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(authUser.getId())
                .email(authUser.getEmail())
                .nickname(authUser.getNickname())
                .password("encoded-password")
                .phoneNumber("01012345678")
                .imageUrl("image-url")
                .build();
    }

    @Nested
    class 유저_ID_조회 {

        @Test
        void 존재하지_않는_ID면_예외_발생() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            HandledException ex = assertThrows(HandledException.class, () ->
                    userService.getUser(userId));
            assertEquals(ErrorCode.USER_NOT_FOUND.getDefaultMessage(), ex.getMessage());
        }

        @Test
        void 유저_반환_성공() {
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            assertEquals(user, userService.getUser(userId));
        }
    }

    @Nested
    class 유저_이메일_조회 {

        @Test
        void 존재하지_않는_이메일이면_예외_발생() {
            // given
            given(userRepository.findByEmail(authUser.getEmail())).willReturn(Optional.empty());

            // when & then
            HandledException ex = assertThrows(HandledException.class, () ->
                    userService.getUserByEmail(authUser.getEmail()));
            assertEquals(ErrorCode.USER_NOT_FOUND.getDefaultMessage(), ex.getMessage());
        }

        @Test
        void 유저_반환_성공() {
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
            assertEquals(user, userService.getUserByEmail(user.getEmail()));
        }
    }

    @Nested
    class 회원_탈퇴 {

        @Test
        void 비밀번호_일치하지_않으면_예외_발생() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

            UserDeleteRequest request = UserDeleteRequest.builder()
                    .password("wrong-password")
                    .build();

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    userService.deleteUser(authUser, request)
            );
            assertEquals(ErrorCode.INCORRECT_PASSWORD.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 탈퇴_성공_이미지있는_유저는_이미지_삭제() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

            UserDeleteRequest request = UserDeleteRequest.builder()
                    .password("encoded-password")
                    .build();

            // when
            SimpleUserResponse response = userService.deleteUser(authUser, request);

            // then
            assertEquals(userId, response.getId());
            verify(tokenService).deleteRefreshTokenByUserId(userId);
            verify(imageService).delete("image-url");
        }

        @Test
        void 탈퇴_성공_이미지없는_유저는_이미지_삭제_안함() {
            // given
            user.updateImageUrl(null);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

            UserDeleteRequest request = UserDeleteRequest.builder()
                    .password("encoded-password")
                    .build();

            // when
            userService.deleteUser(authUser, request);

            // then
            verify(imageService, never()).delete(anyString());
        }
    }

    @Nested
    class 비밀번호_변경 {

        @Test
        void 비밀번호_일치하지_않으면_예외_발생() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

            UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                    .password("wrong-password")
                    .newPassword("new-password")
                    .build();

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    userService.updatePassword(authUser, request)
            );
            assertEquals(ErrorCode.INCORRECT_PASSWORD.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 비밀번호_변경_성공() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

            UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                    .password("encoded-password")
                    .newPassword("new-password")
                    .build();

            // when
            SimpleUserResponse response = userService.updatePassword(authUser, request);

            // then
            assertEquals(userId, response.getId());
            verify(tokenService).deleteRefreshTokenByUserId(userId);
        }
    }

    @Nested
    class 프로필_조회 {

        @Test
        void 유저가_존재하지_않으면_예외_발생() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    userService.getUserProfile(authUser)
            );
            assertEquals(ErrorCode.USER_NOT_FOUND.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 프로필_조회_성공() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            UserProfileResponse response = userService.getUserProfile(authUser);

            // then
            assertAll(
                    () -> assertEquals(user.getId(), response.getId()),
                    () -> assertEquals(user.getEmail(), response.getEmail()),
                    () -> assertEquals(user.getNickname(), response.getNickname())
            );
        }
    }

    @Nested
    class 프로필_수정 {

        @Test
        void 기존이미지와_다르면_삭제후_업데이트_성공() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            String oldImageUrl = user.getImageUrl();
            String newImageUrl = "new-image.jpg";
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .nickname("새닉네임")
                    .phoneNumber("01099999999")
                    .imageUrl(newImageUrl)
                    .build();

            // when
            UserProfileResponse response = userService.updateUserProfile(authUser, request);

            // then
            assertAll(
                    () -> assertEquals("새닉네임", response.getNickname()),
                    () -> assertEquals("01099999999", response.getPhoneNumber()),
                    () -> assertEquals(newImageUrl, response.getImageUrl())
            );
            verify(imageService).delete(oldImageUrl);
        }

        @Test
        void 기존_이미지가_없다면_삭제되지_않고_업데이트_성공() {
            // given
            user.updateImageUrl(null); // 기존 이미지 없음
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            String newImageUrl = "new-image.jpg";
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .imageUrl(newImageUrl)
                    .build();

            // when
            UserProfileResponse response = userService.updateUserProfile(authUser, request);

            // then
            assertEquals(newImageUrl, response.getImageUrl());
            verify(imageService, never()).delete(anyString());
        }

        @Test
        void 이미지가_같으면_삭제하지_않고_업데이트_성공() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            String sameImageUrl = user.getImageUrl(); // 같은 URL
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .imageUrl(sameImageUrl)
                    .build();

            // when
            UserProfileResponse response = userService.updateUserProfile(authUser, request);

            // then
            assertEquals(sameImageUrl, response.getImageUrl());
            verify(imageService, never()).delete(anyString());
        }

        @Test
        void 이미지_요청이_없으면_이미지_삭제및_업데이트_없이_성공() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .imageUrl(null) // 이미지 변경 요청 없음
                    .build();

            // when
            UserProfileResponse response = userService.updateUserProfile(authUser, request);

            // then
            assertEquals("image-url", response.getImageUrl()); // 변경 없음
            verify(imageService, never()).delete(anyString());
        }
    }
}