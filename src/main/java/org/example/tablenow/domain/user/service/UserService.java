package org.example.tablenow.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.auth.service.TokenService;
import org.example.tablenow.domain.user.dto.request.UpdatePasswordRequest;
import org.example.tablenow.domain.user.dto.request.UserDeleteRequest;
import org.example.tablenow.domain.user.dto.response.SimpleUserResponse;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional
    public SimpleUserResponse deleteUser(AuthUser authUser, UserDeleteRequest request) {
        User user = findUserById(authUser.getId());
        validatePassword(user, request.getPassword());

        user.deleteUser();
        tokenService.deleteRefreshTokenByUserId(user.getId());

        return SimpleUserResponse.builder()
                .id(user.getId())
                .message("회원 탈퇴에 성공했습니다.")
                .build();
    }

    @Transactional
    public SimpleUserResponse updatePassword(AuthUser authUser, UpdatePasswordRequest request) {
        User user = findUserById(authUser.getId());
        validatePassword(user, request.getPassword());

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        tokenService.deleteRefreshTokenByUserId(user.getId());

        return SimpleUserResponse.builder()
                .id(user.getId())
                .message("비밀번호가 성공적으로 수정되었습니다.")
                .build();
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));
    }

    private void validatePassword(User user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new HandledException(ErrorCode.INCORRECT_PASSWORD);
        }
    }
}
