package org.example.tablenow.domain.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.auth.dto.response.TokenResponse;
import org.example.tablenow.domain.auth.oAuth.NaverUserInfo;
import org.example.tablenow.domain.auth.oAuth.NaverUserInfoResponse;
import org.example.tablenow.domain.auth.oAuth.OAuthProperties;
import org.example.tablenow.domain.auth.oAuth.OAuthProvider;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.example.tablenow.global.util.PhoneNumberNormalizer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Service
public class NaverAuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final OAuthProperties OAuthProperties;

    @Transactional
    public TokenResponse login(String code) {
        // 1. 인가 코드로 액세스 토큰 요청
        String naverAccessToken = getNaverAccessToken(code);

        // 2. 액세스 토큰으로 사용자 정보 요청
        NaverUserInfoResponse userInfo = getNaverUserInfo(naverAccessToken);
        NaverUserInfo naverUserInfo = NaverUserInfo.fromNaverUserInfoResponse(userInfo.getResponse());

        // 3. 사용자 정보로 기존 유저 조회 또는 신규 회원 가입
        User user = userRepository.findByEmail(naverUserInfo.getEmail())
                .orElseGet(() -> registerNewUser(naverUserInfo));

        // 4. JWT 토큰 생성 및 반환
        String accessToken = tokenService.createAccessToken(user);
        String refreshToken = tokenService.createRefreshToken(user);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private String getNaverAccessToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", OAuthProperties.getRegistration().getNaver().getClientId());
        formData.add("client_secret", OAuthProperties.getRegistration().getNaver().getClientSecret());
        formData.add("redirect_uri", OAuthProperties.getRegistration().getNaver().getRedirectUri());
        formData.add("grant_type", OAuthProperties.getRegistration().getNaver().getAuthorizationGrantType());
        formData.add("code", code);

        return webClient.post()
                .uri(OAuthProperties.getProvider().getNaver().getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractAccessToken)
                .block();
    }

    private NaverUserInfoResponse getNaverUserInfo(String accessToken) {
        return webClient.get()
                .uri(OAuthProperties.getProvider().getNaver().getUserInfoUri())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(NaverUserInfoResponse.class) // 응답 Body 객체 매핑: JSON -> NaverUserInfoResponse 클래스 인스턴스로 역직렬화
                .block();
    }

    private String extractAccessToken(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            throw new HandledException(ErrorCode.FAILED_TO_PARSE_OAUTH_TOKEN);
        }
    }

    private User registerNewUser(NaverUserInfo naverUserInfo) {
        String normalizedPhoneNumber = PhoneNumberNormalizer.normalize(naverUserInfo.getPhoneNumber());

        User user = User.builder()
                .email(naverUserInfo.getEmail())
                .name(naverUserInfo.getName())
                .nickname(naverUserInfo.getNickname())
                .userRole(UserRole.ROLE_USER)
                .oauthId(naverUserInfo.getId())
                .oauthProvider(OAuthProvider.NAVER)
                .imageUrl(naverUserInfo.getProfileImage())
                .phoneNumber(normalizedPhoneNumber)
                .build();
        return userRepository.save(user);
    }
}
