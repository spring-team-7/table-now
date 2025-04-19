package org.example.tablenow.domain.auth.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.auth.dto.response.TokenResponse;
import org.example.tablenow.domain.auth.oAuth.config.KakaoOAuthProperties;
import org.example.tablenow.domain.auth.oAuth.config.OAuthProperties;
import org.example.tablenow.domain.auth.oAuth.config.OAuthProvider;
import org.example.tablenow.domain.auth.oAuth.kakao.KakaoUserInfo;
import org.example.tablenow.domain.auth.oAuth.kakao.KakaoUserInfoResponse;
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
public class KakaoAuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final WebClient webClient;
    private final OAuthProperties oAuthProperties;
    private final KakaoOAuthProperties kakaoOAuthProperties;

    @Transactional
    public TokenResponse login(String code) {
        // 1. 인가 코드로 액세스 토큰 요청
        String kakaoAccessToken = getKakaoAccessToken(code);

        // 2. 액세스 토큰으로 사용자 정보 요청
        KakaoUserInfoResponse response = getKakaoUserInfo(kakaoAccessToken);
        KakaoUserInfo kakaoUserInfo = KakaoUserInfo.fromKakaoUserInfoResponse(response);

        // 3. 사용자 정보로 기존 유저 조회 또는 신규 회원 가입
        User user = userRepository.findByEmail(kakaoUserInfo.getEmail())
                .orElseGet(() -> registerNewUser(kakaoUserInfo));
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

    public void unlinkKakaoByAdminKey(String kakaoUserId) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(OAuthConstants.TARGET_ID_TYPE, OAuthConstants.USER_ID);
        formData.add(OAuthConstants.TARGET_ID, kakaoUserId);

        try {
            webClient.post()
                    .uri(kakaoOAuthProperties.getUnlinkUri())
                    .header(HttpHeaders.AUTHORIZATION, OAuthConstants.KAKAO_ADMIN_AUTH_PREFIX + kakaoOAuthProperties.getAdminKey())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, res -> {
                        log.warn("[OAuth] 카카오 계정 연결 해제 실패: {}", res.statusCode());
                        return Mono.error(new HandledException(ErrorCode.OAUTH_USER_UNLINK_REQUEST_FAILED));
                    })
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientRequestException e) {
            log.error("[OAuth] 카카오 계정 해제 WebClient 요청 실패", e);
            throw new HandledException(ErrorCode.OAUTH_PROVIDER_UNREACHABLE);
        } catch (Exception e) {
            log.error("[OAuth] 카카오 계정 해제 중 알 수 없는 오류", e);
            throw new HandledException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String getKakaoAccessToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(OAuthConstants.CLIENT_ID, oAuthProperties.getRegistration().getKakao().getClientId());
        formData.add(OAuthConstants.CLIENT_SECRET, oAuthProperties.getRegistration().getKakao().getClientSecret());
        formData.add(OAuthConstants.REDIRECT_URI, oAuthProperties.getRegistration().getKakao().getRedirectUri());
        formData.add(OAuthConstants.GRANT_TYPE, oAuthProperties.getRegistration().getKakao().getAuthorizationGrantType());
        formData.add(OAuthConstants.CODE, code);

        try {
            return webClient.post()
                    .uri(oAuthProperties.getProvider().getKakao().getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, res -> {
                        log.warn("[OAuth] 카카오 인가코드 오류 (4xx): {}", res.statusCode());
                        return Mono.error(new HandledException(ErrorCode.OAUTH_TOKEN_REQUEST_FAILED));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, res -> {
                        log.error("[OAuth] 카카오 인증 서버 오류 (5xx): {}", res.statusCode());
                        return Mono.error(new HandledException(ErrorCode.OAUTH_PROVIDER_SERVER_ERROR));
                    })
                    .bodyToMono(String.class)
                    .map(OAuthResponseParser::extractAccessToken)
                    .block();
        } catch (WebClientRequestException e) {
            log.error("[OAuth] 카카오 WebClient 요청 실패", e);
            throw new HandledException(ErrorCode.OAUTH_PROVIDER_UNREACHABLE);
        } catch (Exception e) {
            log.error("[OAuth] 카카오 토큰 요청 중 알 수 없는 오류", e);
            throw new HandledException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private KakaoUserInfoResponse getKakaoUserInfo(String accessToken) {
        try {
            return webClient.get()
                    .uri(oAuthProperties.getProvider().getKakao().getUserInfoUri())
                    .header(HttpHeaders.AUTHORIZATION, SecurityConstants.BEARER_PREFIX + accessToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, res -> {
                        log.warn("[OAuth] 카카오 사용자 정보 요청 실패 (4xx): {}", res.statusCode());
                        return Mono.error(new HandledException(ErrorCode.OAUTH_USER_INFO_REQUEST_FAILED));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, res -> {
                        log.error("[OAuth] 카카오 사용자 정보 요청 서버 오류 (5xx): {}", res.statusCode());
                        return Mono.error(new HandledException(ErrorCode.OAUTH_PROVIDER_SERVER_ERROR));
                    })
                    .bodyToMono(KakaoUserInfoResponse.class) // 응답 Body 객체 매핑: JSON -> KakaoUserInfoResponse 클래스 인스턴스로 역직렬화
                    .block();
        } catch (WebClientRequestException e) {
            log.error("[OAuth] 카카오 사용자 정보 요청 WebClient 실패", e);
            throw new HandledException(ErrorCode.OAUTH_PROVIDER_UNREACHABLE);
        } catch (Exception e) {
            log.error("[OAuth] 카카오 사용자 정보 요청 중 알 수 없는 오류", e);
            throw new HandledException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private User registerNewUser(KakaoUserInfo kakaoUserInfo) {
        String normalizedPhoneNumber = PhoneNumberNormalizer.normalize(kakaoUserInfo.getPhoneNumber());

        User newUser = User.builder()
                .email(kakaoUserInfo.getEmail())
                .name(kakaoUserInfo.getName())
                .nickname(kakaoUserInfo.getNickname())
                .userRole(UserRole.ROLE_USER)
                .oauthId(kakaoUserInfo.getId())
                .oauthProvider(OAuthProvider.KAKAO)
                .imageUrl(kakaoUserInfo.getProfileImage())
                .phoneNumber(normalizedPhoneNumber)
                .build();
        return userRepository.save(newUser);
    }
}
