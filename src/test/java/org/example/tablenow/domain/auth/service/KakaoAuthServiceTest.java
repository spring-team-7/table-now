package org.example.tablenow.domain.auth.service;

import org.example.tablenow.domain.auth.dto.response.TokenResponse;
import org.example.tablenow.domain.auth.oAuth.config.KakaoOAuthProperties;
import org.example.tablenow.domain.auth.oAuth.config.OAuthProperties;
import org.example.tablenow.domain.auth.oAuth.config.OAuthProvider;
import org.example.tablenow.domain.auth.oAuth.kakao.KakaoUserInfo;
import org.example.tablenow.domain.auth.oAuth.kakao.KakaoUserInfoResponse;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.example.tablenow.global.util.OAuthResponseParser;
import org.example.tablenow.global.util.PhoneNumberNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.tablenow.testutil.OAuthTestUtil.createAccessTokenJson;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KakaoAuthServiceTest {

    @InjectMocks
    private KakaoAuthService kakaoAuthService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenService tokenService;
    @Mock
    private WebClient webClient;
    @Mock
    private OAuthProperties oAuthProperties;
    @Mock
    private KakaoOAuthProperties kakaoOAuthProperties;
    @Mock
    private OAuthResponseParser oAuthResponseParser;

    // WebClient mocking을 위한 내부 변수들
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;
    @Mock
    private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    private static final String AUTHORIZATION_CODE = "test_authorization_code";
    private static final String KAKAO_ACCESS_TOKEN = "test_access_token";

    private KakaoUserInfoResponse createKakaoUserInfoResponse(
            Long id, String email, String name, String phone, String nickname, String imageUrl) {

        KakaoUserInfoResponse response = new KakaoUserInfoResponse();
        KakaoUserInfoResponse.KakaoAccount account = new KakaoUserInfoResponse.KakaoAccount();
        KakaoUserInfoResponse.KakaoAccount.Profile profile = new KakaoUserInfoResponse.KakaoAccount.Profile();

        // private 필드에 값 주입을 위한 ReflectionTestUtils 사용
        ReflectionTestUtils.setField(response, "id", id);
        ReflectionTestUtils.setField(account, "email", email);
        ReflectionTestUtils.setField(account, "name", name);
        ReflectionTestUtils.setField(account, "phone_number", phone);
        ReflectionTestUtils.setField(profile, "nickname", nickname);
        ReflectionTestUtils.setField(profile, "profile_image_url", imageUrl);
        ReflectionTestUtils.setField(account, "profile", profile);
        ReflectionTestUtils.setField(response, "kakao_account", account);

        return response;
    }

    private User createUserFromKakao(KakaoUserInfo kakaoUserInfo) {
        return User.builder()
                .email(kakaoUserInfo.getEmail())
                .name(kakaoUserInfo.getName())
                .nickname(kakaoUserInfo.getNickname())
                .userRole(UserRole.ROLE_USER)
                .oauthId(kakaoUserInfo.getId())
                .oauthProvider(OAuthProvider.KAKAO)
                .imageUrl(kakaoUserInfo.getProfileImage())
                .phoneNumber(PhoneNumberNormalizer.normalize(kakaoUserInfo.getPhoneNumber()))
                .build();
    }

    // access_token 요청하는 WebClient mocking
    private void mockWebClientTokenRequest(Mono<String> response) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(BodyInserter.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(response);
    }

    // Kakao 사용자 정보 요청하는 WebClient mocking
    private void mockWebClientUserInfoRequest(Mono<KakaoUserInfoResponse> response) {
        when(webClient.get()).thenAnswer(invocation -> requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenAnswer(invocation -> requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenAnswer(invocation -> requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(KakaoUserInfoResponse.class)).thenReturn(response);
    }

    // Kakao 사용자 연결 해제 요청하는 WebClient mocking
    private void mockWebClientUnlinkRequest(Mono<Void> response) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(anyString(), anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(BodyInserter.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(response);
    }

    @Nested
    class 로그인 {

        @BeforeEach
        void setup() {
            // OAuth 설정값 모킹
            OAuthProperties.Registration.Kakao regKakao =
                    new OAuthProperties.Registration.Kakao("clientId", "clientSecret", "redirectUri", "authorization_code");

            OAuthProperties.Registration registration =
                    new OAuthProperties.Registration(regKakao, null); // Naver는 필요 없으므로 null

            OAuthProperties.Provider.Kakao providerKakao =
                    new OAuthProperties.Provider.Kakao(null, "token_uri", "user_info_uri", null);

            OAuthProperties.Provider provider =
                    new OAuthProperties.Provider(providerKakao, null); // Naver는 필요 없으므로 null

            when(oAuthProperties.getRegistration()).thenReturn(registration);
            when(oAuthProperties.getProvider()).thenReturn(provider);
        }

        @Test
        void 액세스토큰_JSON파싱_실패_예외처리() {
            // given
            String invalidAccessTokenJson = "invalid json";
            mockWebClientTokenRequest(Mono.just(invalidAccessTokenJson));
            when(oAuthResponseParser.extractAccessToken(invalidAccessTokenJson))
                    .thenThrow(new RuntimeException("JSON 파싱 실패"));

            // when & then
            assertThrows(HandledException.class, () -> {
                kakaoAuthService.login(AUTHORIZATION_CODE);
            });
        }

        @Test
        void 탈퇴한_유저가_로그인_시_예외처리() {
            // given
            String accessTokenJson = createAccessTokenJson(KAKAO_ACCESS_TOKEN);
            KakaoUserInfoResponse response = createKakaoUserInfoResponse(
                    99L, "deleted@test.com", "탈퇴자", "+82 10-9999-9999", "탈퇴유저", "image-url");
            User deletedUser = User.builder()
                    .email("deleted@test.com")
                    .build();
            deletedUser.deleteUser();

            mockWebClientTokenRequest(Mono.just(accessTokenJson));
            when(oAuthResponseParser.extractAccessToken(accessTokenJson))
                    .thenReturn(KAKAO_ACCESS_TOKEN);

            mockWebClientUserInfoRequest(Mono.just(response));

            when(userRepository.findByEmail("deleted@test.com")).thenReturn(Optional.of(deletedUser));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () -> {
                kakaoAuthService.login(AUTHORIZATION_CODE);
            });
            assertEquals(ErrorCode.ALREADY_DELETED_USER.getDefaultMessage(), exception.getMessage());
        }

        @Test
        void 카카오_토큰요청_중_WebClient_요청실패_예외처리() {
            // given
            WebClientRequestException webClientRequestException = new WebClientRequestException(
                    new IOException("연결 실패"),
                    HttpMethod.POST,
                    URI.create("https://kauth.kakao.com/oauth/token"),
                    HttpHeaders.EMPTY
            );
            mockWebClientTokenRequest(Mono.error(webClientRequestException));

            // when & then
            assertThatThrownBy(() -> kakaoAuthService.login(AUTHORIZATION_CODE))
                    .isInstanceOf(HandledException.class)
                    .hasMessage(ErrorCode.OAUTH_PROVIDER_UNREACHABLE.getDefaultMessage());
        }

        @Test
        void 카카오_유저정보요청_중_WebClient_요청실패_예외처리() {
            // given
            String accessTokenJson = createAccessTokenJson(KAKAO_ACCESS_TOKEN);

            // 1. access token 요청 mocking (정상 흐름)
            mockWebClientTokenRequest(Mono.just(accessTokenJson));
            when(oAuthResponseParser.extractAccessToken(accessTokenJson))
                    .thenReturn(KAKAO_ACCESS_TOKEN);

            // 2. 유저정보 요청 mocking (이 부분에서 예외 발생)
            WebClientRequestException webClientRequestException = new WebClientRequestException(
                    new IOException("연결 실패"),
                    HttpMethod.GET,
                    URI.create("https://kapi.kakao.com/v2/user/me"),
                    HttpHeaders.EMPTY
            );
            mockWebClientUserInfoRequest(Mono.error(webClientRequestException));

            // when & then
            assertThatThrownBy(() -> kakaoAuthService.login(AUTHORIZATION_CODE))
                    .isInstanceOf(HandledException.class)
                    .hasMessage(ErrorCode.OAUTH_PROVIDER_UNREACHABLE.getDefaultMessage());
        }

        @Test
        void 기존유저_성공() {
            // given
            String accessTokenJson = createAccessTokenJson(KAKAO_ACCESS_TOKEN); // 카카오 서버에서 받은 토큰 응답 가정
            KakaoUserInfoResponse response = createKakaoUserInfoResponse(
                    999L, "user@test.com", "유저", "+82 10-1234-5678", "닉네임", "image-url");
            User user = User.builder()
                    .email("user@test.com")
                    .build();

            mockWebClientTokenRequest(Mono.just(accessTokenJson));
            when(oAuthResponseParser.extractAccessToken(accessTokenJson))
                    .thenReturn(KAKAO_ACCESS_TOKEN);

            mockWebClientUserInfoRequest(Mono.just(response));

            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
            when(tokenService.createAccessToken(user)).thenReturn("access_token");
            when(tokenService.createRefreshToken(user)).thenReturn("refresh_token");

            // when
            TokenResponse tokenResponse = kakaoAuthService.login(AUTHORIZATION_CODE);

            // then
            assertEquals("access_token", tokenResponse.getAccessToken());
            assertEquals("refresh_token", tokenResponse.getRefreshToken());
        }

        @Test
        void 신규회원가입_성공() {
            // given
            String accessTokenJson = createAccessTokenJson(KAKAO_ACCESS_TOKEN);
            KakaoUserInfoResponse response = createKakaoUserInfoResponse(
                    -999L, "newuser@test.com", "새유저", "+82 10-0000-0000", "새유저닉네임", "image-url");

            KakaoUserInfo kakaoUserInfo = KakaoUserInfo.fromKakaoUserInfoResponse(response);
            User newUser = createUserFromKakao(kakaoUserInfo);

            mockWebClientTokenRequest(Mono.just(accessTokenJson));
            when(oAuthResponseParser.extractAccessToken(accessTokenJson))
                    .thenReturn(KAKAO_ACCESS_TOKEN);

            mockWebClientUserInfoRequest(Mono.just(response));

            when(userRepository.findByEmail("newuser@test.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(newUser);
            when(tokenService.createAccessToken(newUser)).thenReturn("access_token");
            when(tokenService.createRefreshToken(newUser)).thenReturn("refresh_token");

            // when
            TokenResponse tokenResponse = kakaoAuthService.login(AUTHORIZATION_CODE);

            // then
            assertEquals("access_token", tokenResponse.getAccessToken());
            assertEquals("refresh_token", tokenResponse.getRefreshToken());
        }
    }

    @Nested
    class 연결끊기Api_호출 {

        private String kakaoId = "kakao-user-id";

        @BeforeEach
        void setup() {
            // mock 속성
            when(kakaoOAuthProperties.getAdminKey()).thenReturn("admin-key");
            when(kakaoOAuthProperties.getUnlinkUri()).thenReturn("https://kapi.kakao.com/v1/user/unlink");
        }

        @Test
        void 카카오_연결해제_중_WebClient_요청실패_예외처리() {
            // given
            WebClientRequestException webClientRequestException = new WebClientRequestException(
                    new IOException("연결 실패"),
                    HttpMethod.POST,
                    URI.create("https://kapi.kakao.com/v1/user/unlink"),
                    HttpHeaders.EMPTY
            );
            mockWebClientUnlinkRequest(Mono.error(webClientRequestException));

            // when & then
            assertThatThrownBy(() -> kakaoAuthService.unlinkKakaoByAdminKey(kakaoId))
                    .isInstanceOf(HandledException.class)
                    .hasMessage(ErrorCode.OAUTH_PROVIDER_UNREACHABLE.getDefaultMessage());
        }

        @Test
        void unlinkKakaoByAdminKey_성공() {
            // given
            mockWebClientUnlinkRequest(Mono.empty());

            // when & then
            assertDoesNotThrow(() -> kakaoAuthService.unlinkKakaoByAdminKey(kakaoId));
        }
    }
}