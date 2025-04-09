package org.example.tablenow.domain.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.auth.dto.response.TokenResponse;
import org.example.tablenow.domain.auth.oAuth.NaverUserInfo;
import org.example.tablenow.domain.auth.oAuth.OAuthProperties;
import org.example.tablenow.domain.auth.oAuth.OAuthProvider;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.example.tablenow.global.util.PhoneNumberNormalizer;
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
public class NaverAuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final OAuthProperties OAuthProperties;

    @Transactional
    public TokenResponse login(String code) {
        String naverAccessToken = getNaverAccessToken(code);
        Map<String, Object> userInfo = getNaverUserInfo(naverAccessToken);
        NaverUserInfo naverUserInfo = NaverUserInfo.from(userInfo);

        User user = userRepository.findByEmail(naverUserInfo.getEmail())
                .orElseGet(() -> registerNewUser(naverUserInfo));

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

    private Map<String, Object> getNaverUserInfo(String accessToken) {
        Map<String, Object> responseMap = webClient.get()
                .uri(OAuthProperties.getProvider().getNaver().getUserInfoUri())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        return (Map<String, Object>) responseMap.get("response");
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
