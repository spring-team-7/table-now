package org.example.tablenow.domain.auth.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.auth.dto.response.TokenResponse;
import org.example.tablenow.domain.auth.oAuth.KakaoUserInfo;
import org.example.tablenow.domain.auth.oAuth.OAuthProperties;
import org.example.tablenow.domain.auth.oAuth.OAuthProvider;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class SocialAuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final OAuthProperties OAuthProperties;

    @Transactional
    public TokenResponse kakaoLogin(String code) {
        // 1. 인가 코드로 액세스 토큰 요청
        String accessToken = getKakaoAccessToken(code);

        // 2. 액세스 토큰으로 사용자 정보 요청
        Map<String, Object> userInfo = getKakaoUserInfo(accessToken);
        KakaoUserInfo kakaoUserInfo = KakaoUserInfo.from(userInfo);

        // 3. 유저 정보 조회 또는 신규 회원 가입
        User user = userRepository.findByEmail(kakaoUserInfo.getEmail())
                .orElseGet(() -> registerNewUser(kakaoUserInfo));

        // 4. JWT 토큰 생성
        return generateTokenResponse(user);
    }

    private String getKakaoAccessToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", OAuthProperties.getRegistration().getKakao().getClientId());
        formData.add("client_secret", OAuthProperties.getRegistration().getKakao().getClientSecret());
        formData.add("redirect_uri", OAuthProperties.getRegistration().getKakao().getRedirectUri());
        formData.add("grant_type", OAuthProperties.getRegistration().getKakao().getAuthorizationGrantType());
        formData.add("code", code);

        return webClient.post()
                .uri(OAuthProperties.getProvider().getKakao().getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractAccessToken)
                .block();
    }

    private String extractAccessToken(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse access token", e);
        }
    }

    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        return webClient.get()
                .uri(OAuthProperties.getProvider().getKakao().getUserInfoUri())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

    private User registerNewUser(KakaoUserInfo kakaoUserInfo) {
        User newUser = User.builder()
                .email(kakaoUserInfo.getEmail())
                .name(kakaoUserInfo.getName())
                .nickname(kakaoUserInfo.getNickname())
                .userRole(UserRole.ROLE_USER)
                .oauthId(kakaoUserInfo.getId())
                .oauthProvider(OAuthProvider.KAKAO)
                .imageUrl(kakaoUserInfo.getProfileImage())
                .phoneNumber(kakaoUserInfo.getPhoneNumber())
                .build();
        return userRepository.save(newUser);
    }

    private TokenResponse generateTokenResponse(User user) {
        // Access & Refresh Token 생성
        String accessToken = tokenService.createAccessToken(user);
        String refreshToken = tokenService.createRefreshToken(user);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
