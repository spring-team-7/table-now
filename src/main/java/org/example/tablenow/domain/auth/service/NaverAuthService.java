package org.example.tablenow.domain.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.auth.dto.response.TokenResponse;
import org.example.tablenow.domain.auth.oAuth.config.OAuthProperties;
import org.example.tablenow.domain.auth.oAuth.config.OAuthProvider;
import org.example.tablenow.domain.auth.oAuth.naver.NaverUserInfo;
import org.example.tablenow.domain.auth.oAuth.naver.NaverUserInfoResponse;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.global.constant.OAuthConstants;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.example.tablenow.global.constant.SecurityConstants;
import org.example.tablenow.global.util.PhoneNumberNormalizer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Service
public class NaverAuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final OAuthProperties oAuthProperties;

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
        if (user.getDeletedAt() != null) {
            // 탈퇴한 유저는 재가입 불가
            throw new HandledException(ErrorCode.ALREADY_DELETED_USER);
        }

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
        formData.add(OAuthConstants.CLIENT_ID, oAuthProperties.getRegistration().getNaver().getClientId());
        formData.add(OAuthConstants.CLIENT_SECRET, oAuthProperties.getRegistration().getNaver().getClientSecret());
        formData.add(OAuthConstants.REDIRECT_URI, oAuthProperties.getRegistration().getNaver().getRedirectUri());
        formData.add(OAuthConstants.GRANT_TYPE, oAuthProperties.getRegistration().getNaver().getAuthorizationGrantType());
        formData.add(OAuthConstants.CODE, code);

        return webClient.post()
                .uri(oAuthProperties.getProvider().getNaver().getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractAccessToken)
                .block();
    }

    private NaverUserInfoResponse getNaverUserInfo(String accessToken) {
        return webClient.get()
                .uri(oAuthProperties.getProvider().getNaver().getUserInfoUri())
                .header(HttpHeaders.AUTHORIZATION, SecurityConstants.BEARER_PREFIX + accessToken)
                .retrieve()
                .bodyToMono(NaverUserInfoResponse.class) // 응답 Body 객체 매핑: JSON -> NaverUserInfoResponse 클래스 인스턴스로 역직렬화
                .block();
    }

    private String extractAccessToken(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get(OAuthConstants.ACCESS_TOKEN).asText();
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
