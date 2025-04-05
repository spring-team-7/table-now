package org.example.tablenow.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.auth.dto.request.SignupRequest;
import org.example.tablenow.domain.user.dto.response.UserResponse;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.example.tablenow.global.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public UserResponse signup(SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new HandledException(ErrorCode.CONFLICT, "이미 가입된 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User newUser = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .nickname(request.getNickname())
                .phoneNumber(request.getPhoneNumber())
                .userRole(request.getRole())
                .build();

        User savedUser = userRepository.save(newUser);

        return UserResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .nickname(savedUser.getNickname())
                .phoneNumber(savedUser.getPhoneNumber())
                .role(savedUser.getRole().name())
                .build();
    }
}
