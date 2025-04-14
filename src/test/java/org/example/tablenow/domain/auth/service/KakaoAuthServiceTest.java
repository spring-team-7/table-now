package org.example.tablenow.domain.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tablenow.domain.auth.dto.response.TokenResponse;
import org.example.tablenow.domain.auth.oAuth.config.OAuthProperties;
import org.example.tablenow.domain.auth.oAuth.config.OAuthProvider;
import org.example.tablenow.domain.auth.oAuth.kakao.KakaoUserInfo;
import org.example.tablenow.domain.auth.oAuth.kakao.KakaoUserInfoResponse;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.global.exception.HandledException;
import org.example.tablenow.global.util.PhoneNumberNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.example.tablenow.testutil.OAuthTestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private ObjectMapper objectMapper;
    @Mock
    private OAuthProperties oAuthProperties;

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

    @BeforeEach
    void setup() {
        // OAuth 설정값 모킹
        OAuthProperties.Registration.Kakao regKakao = new OAuthProperties.Registration.Kakao();
        regKakao.setClientId("clientId");
        regKakao.setClientSecret("clientSecret");
        regKakao.setRedirectUri("redirectUri");
        regKakao.setAuthorizationGrantType("authorization_code");

        OAuthProperties.Provider.Kakao providerKakao = new OAuthProperties.Provider.Kakao();
        providerKakao.setTokenUri("token_uri");
        providerKakao.setUserInfoUri("user_info_uri");

        OAuthProperties.Registration registration = new OAuthProperties.Registration();
        registration.setKakao(regKakao);
        when(oAuthProperties.getRegistration()).thenReturn(registration);

        OAuthProperties.Provider provider = new OAuthProperties.Provider();
        provider.setKakao(providerKakao);
        when(oAuthProperties.getProvider()).thenReturn(provider);
    }

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

    private void mockWebClientTokenRequest(String responseJson) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any()))
                .thenAnswer(invocation -> requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseJson));
    }

    // 발급받은 access_token으로 Kakao 사용자 정보 요청하는 WebClient mocking
    private void mockWebClientUserInfoRequest(KakaoUserInfoResponse response) {
        when(webClient.get()).thenAnswer(invocation -> requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString()))
                .thenAnswer(invocation -> requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString()))
                .thenAnswer(invocation -> requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(KakaoUserInfoResponse.class))
                .thenReturn(Mono.just(response));
    }

    @Nested
    class 로그인 {

        @Test
        void 액세스토큰_JSON파싱_실패_예외_발생() throws Exception {
            // given
            String invalidAccessTokenJson = "invalid json";
            mockWebClientTokenRequest(invalidAccessTokenJson);
            when(objectMapper.readTree(invalidAccessTokenJson))
                    .thenThrow(new RuntimeException("JSON 파싱 실패"));

            // when & then
            assertThrows(HandledException.class, () -> {
                kakaoAuthService.login(AUTHORIZATION_CODE);
            });
        }

        @Test
        void 기존유저_성공() throws Exception {
            // given
            String accessTokenJson = createAccessTokenJson(KAKAO_ACCESS_TOKEN); // 카카오 서버에서 받은 토큰 응답 가정
            KakaoUserInfoResponse response = createKakaoUserInfoResponse(
                    999L, "user@test.com", "유저", "+82 10-1234-5678", "닉네임", "image-url");
            User user = User.builder()
                    .email("user@test.com")
                    .build();

            mockWebClientTokenRequest(accessTokenJson);
            mockWebClientUserInfoRequest(response);
            stubJsonParsing(objectMapper, accessTokenJson);

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
        void 신규회원가입_성공() throws Exception {
            // given
            String accessTokenJson = createAccessTokenJson(KAKAO_ACCESS_TOKEN);
            KakaoUserInfoResponse response = createKakaoUserInfoResponse(
                    -999L, "newuser@test.com", "새유저", "+82 10-0000-0000", "새유저닉네임", "image-url");

            KakaoUserInfo kakaoUserInfo = KakaoUserInfo.fromKakaoUserInfoResponse(response);
            User newUser = createUserFromKakao(kakaoUserInfo);

            mockWebClientTokenRequest(accessTokenJson);
            mockWebClientUserInfoRequest(response);
            stubJsonParsing(objectMapper, accessTokenJson);

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
}