package org.example.tablenow.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.auth.oAuth.config.OAuthProvider;
import org.example.tablenow.domain.auth.service.KakaoAuthService;
import org.example.tablenow.domain.auth.service.TokenService;
import org.example.tablenow.domain.image.service.ImageService;
import org.example.tablenow.domain.user.dto.request.UpdatePasswordRequest;
import org.example.tablenow.domain.user.dto.request.UpdateProfileRequest;
import org.example.tablenow.domain.user.dto.request.UserDeleteRequest;
import org.example.tablenow.domain.user.dto.response.SimpleUserResponse;
import org.example.tablenow.domain.user.dto.response.UserProfileResponse;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.global.constant.RegexConstants;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.example.tablenow.global.security.enums.BlacklistReason;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;
    private final KakaoAuthService kakaoAuthService;
    private final TokenService tokenService;

    @Transactional
    public SimpleUserResponse deleteUser(AuthUser authUser, UserDeleteRequest request, String refreshToken, String accessToken) {
        User user = getUser(authUser.getId());

        if (user.isOAuthUser()) {
            // 소셜 유저 연동 해제
            if (user.getOauthProvider() == OAuthProvider.KAKAO) {
                kakaoAuthService.unlinkKakaoByAdminKey(user.getOauthId());
            } else if (user.getOauthProvider() == OAuthProvider.NAVER) {
                log.info("[OAuth] 네이버는 OAuth AccessToken이 없으므로 연결 해제를 생략.");
            }
        } else {
            // 일반 유저 비밀번호 검증
            if (!StringUtils.hasText(request.getPassword())) {
                throw new HandledException(ErrorCode.PASSWORD_REQUIRED);
            }
            validatePassword(user, request.getPassword());
        }

        user.deleteUser();
        userRepository.save(user);

        if (StringUtils.hasText(user.getImageUrl())) {
            imageService.delete(user.getImageUrl());
        }

        tokenService.deleteRefreshToken(refreshToken);
        tokenService.addToBlacklist(accessToken, user.getId(), BlacklistReason.WITHDRAWAL);

        return SimpleUserResponse.builder()
                .id(user.getId())
                .message("회원 탈퇴에 성공했습니다.")
                .build();
    }

    @Transactional
    public SimpleUserResponse updatePassword(AuthUser authUser, UpdatePasswordRequest request, String refreshToken, String accessToken) {
        User user = getUser(authUser.getId());

        // OAuth 유저 차단
        if (user.isOAuthUser()) {
            throw new HandledException(ErrorCode.UNSUPPORTED_SOCIAL_USER_OPERATION);
        }

        // Dto 검증
        if (!StringUtils.hasText(request.getPassword()) || !StringUtils.hasText(request.getNewPassword())) {
            throw new HandledException(ErrorCode.PASSWORD_REQUIRED);
        }
        if (!Pattern.matches(RegexConstants.PASSWORD_REGEX, request.getNewPassword())) {
            throw new HandledException(ErrorCode.PASSWORD_FORMAT_INVALID);
        }

        validatePassword(user, request.getPassword());

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        tokenService.deleteRefreshToken(refreshToken);
        tokenService.addToBlacklist(accessToken, user.getId(), BlacklistReason.PASSWORD_CHANGE);

        return SimpleUserResponse.builder()
                .id(user.getId())
                .message("비밀번호가 성공적으로 수정되었습니다.")
                .build();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(AuthUser authUser) {
        User user = getUser(authUser.getId());
        return UserProfileResponse.fromUser(user);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(AuthUser authUser, UpdateProfileRequest request) {
        User user = getUser(authUser.getId());

        if (StringUtils.hasText(request.getNickname())) {
            user.updateNickname(request.getNickname());
        }

        if (StringUtils.hasText(request.getPhoneNumber())) {
            user.updatePhoneNumber(request.getPhoneNumber());
        }

        String userImageUrl = user.getImageUrl();
        String requestImageUrl = request.getImageUrl();
        if (StringUtils.hasText(requestImageUrl)) {
            if (!Objects.equals(userImageUrl, requestImageUrl) && StringUtils.hasText(userImageUrl)) {
                imageService.delete(userImageUrl);
            }
            user.updateImageUrl(requestImageUrl);
        }

        userRepository.save(user);

        return UserProfileResponse.fromUser(user);
    }

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));
    }

    public List<User> getUsersWithAlarmEnabled() {
        return userRepository.findAllByIsAlarmEnabledTrue();
    }

    private void validatePassword(User user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new HandledException(ErrorCode.INCORRECT_PASSWORD);
        }
    }
}
