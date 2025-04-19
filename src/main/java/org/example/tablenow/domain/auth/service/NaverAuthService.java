package org.example.tablenow.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.auth.dto.response.TokenResponse;
import org.example.tablenow.domain.auth.oAuth.config.OAuthProperties;
import org.example.tablenow.domain.auth.oAuth.config.OAuthProvider;
import org.example.tablenow.domain.auth.oAuth.naver.NaverUserInfo;
import org.example.tablenow.domain.auth.oAuth.naver.NaverUserInfoResponse;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.global.constant.OAuthConstants;
import org.example.tablenow.global.constant.SecurityConstants;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.example.tablenow.global.util.OAuthResponseParser;
import org.example.tablenow.global.util.PhoneNumberNormalizer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class NaverAuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final WebClient webClient;
    private final OAuthProperties oAuthProperties;
    private final OAuthResponseParser oAuthResponseParser;

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

        try {
            return webClient.post()
                    .uri(oAuthProperties.getProvider().getNaver().getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, res -> {
                        log.warn("[OAuth] 네이버 인가코드 오류 (4xx): {}", res.statusCode());
                        return Mono.error(new HandledException(ErrorCode.OAUTH_TOKEN_REQUEST_FAILED));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, res -> {
                        log.error("[OAuth] 네이버 인증 서버 오류 (5xx): {}", res.statusCode());
                        return Mono.error(new HandledException(ErrorCode.OAUTH_PROVIDER_SERVER_ERROR));
                    })
                    .bodyToMono(String.class)
                    .map(oAuthResponseParser::extractAccessToken)
                    .block();
        } catch (WebClientRequestException e) {
            log.error("[OAuth] 네이버 WebClient 요청 실패", e);
            throw new HandledException(ErrorCode.OAUTH_PROVIDER_UNREACHABLE);
        } catch (Exception e) {
            log.error("[OAuth] 네이버 토큰 요청 중 알 수 없는 오류", e);
            throw new HandledException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private NaverUserInfoResponse getNaverUserInfo(String accessToken) {
        try {
            return webClient.get()
                    .uri(oAuthProperties.getProvider().getNaver().getUserInfoUri())
                    .header(HttpHeaders.AUTHORIZATION, SecurityConstants.BEARER_PREFIX + accessToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, res -> {
                        log.warn("[OAuth] 네이버 사용자 정보 요청 실패 (4xx): {}", res.statusCode());
                        return Mono.error(new HandledException(ErrorCode.OAUTH_USER_INFO_REQUEST_FAILED));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, res -> {
                        log.error("[OAuth] 네이버 사용자 정보 요청 서버 오류 (5xx): {}", res.statusCode());
                        return Mono.error(new HandledException(ErrorCode.OAUTH_PROVIDER_SERVER_ERROR));
                    })
                    .bodyToMono(NaverUserInfoResponse.class) // 응답 Body 객체 매핑: JSON -> NaverUserInfoResponse 클래스 인스턴스로 역직렬화
                    .block();
        } catch (WebClientRequestException e) {
            log.error("[OAuth] 네이버 사용자 정보 요청 WebClient 실패", e);
            throw new HandledException(ErrorCode.OAUTH_PROVIDER_UNREACHABLE);
        } catch (Exception e) {
            log.error("[OAuth] 네이버 사용자 정보 요청 중 알 수 없는 오류", e);
            throw new HandledException(ErrorCode.INTERNAL_SERVER_ERROR);
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
