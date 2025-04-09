package org.example.tablenow.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.auth.service.TokenService;
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
        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new HandledException(ErrorCode.INCORRECT_PASSWORD);
        }

        user.deleteUser();
        tokenService.deleteRefreshTokenByUserId(user.getId());

        return SimpleUserResponse.builder()
                .id(user.getId())
                .message("회원 탈퇴에 성공했습니다.")
                .build();
    }
}
