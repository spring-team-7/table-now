package org.example.tablenow.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.auth.service.TokenService;
import org.example.tablenow.domain.image.service.ImageService;
import org.example.tablenow.domain.user.dto.request.UpdatePasswordRequest;
import org.example.tablenow.domain.user.dto.request.UpdateProfileRequest;
import org.example.tablenow.domain.user.dto.request.UserDeleteRequest;
import org.example.tablenow.domain.user.dto.response.SimpleUserResponse;
import org.example.tablenow.domain.user.dto.response.UserProfileResponse;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final ImageService imageService;

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public SimpleUserResponse deleteUser(AuthUser authUser, UserDeleteRequest request) {
        User user = validatePasswordAndGetUser(authUser, request.getPassword());

        user.deleteUser();
        if (StringUtils.hasText(user.getImageUrl())) {
            imageService.delete(user.getImageUrl());
        }

        return SimpleUserResponse.builder()
                .id(user.getId())
                .message("회원 탈퇴에 성공했습니다.")
                .build();
    }

    @Transactional
    public SimpleUserResponse updatePassword(AuthUser authUser, UpdatePasswordRequest request) {
        User user = validatePasswordAndGetUser(authUser, request.getPassword());

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));

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

        return UserProfileResponse.fromUser(user);
    }

    private void validatePassword(User user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new HandledException(ErrorCode.INCORRECT_PASSWORD);
        }
    }

    private User validatePasswordAndGetUser(AuthUser authUser, String rawPassword) {
        User user = getUser(authUser.getId());
        validatePassword(user, rawPassword);
        return user;
    }
}
