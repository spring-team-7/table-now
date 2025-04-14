package org.example.tablenow.domain.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tablenow.domain.auth.dto.response.TokenResponse;
import org.example.tablenow.domain.auth.oAuth.config.OAuthProperties;
import org.example.tablenow.domain.auth.oAuth.config.OAuthProvider;
import org.example.tablenow.domain.auth.oAuth.naver.NaverUserInfo;
import org.example.tablenow.domain.auth.oAuth.naver.NaverUserInfoResponse;
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

import static org.example.tablenow.testutil.OAuthTestUtil.createAccessTokenJson;
import static org.example.tablenow.testutil.OAuthTestUtil.stubJsonParsing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NaverAuthServiceTest {

    @InjectMocks
    private NaverAuthService naverAuthService;

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
    private static final String NAVER_ACCESS_TOKEN = "test_access_token";

    @BeforeEach
    void setup() {
        // OAuth 설정값 모킹
        OAuthProperties.Registration.Naver regNaver = new OAuthProperties.Registration.Naver();
        regNaver.setClientId("clientId");
        regNaver.setClientSecret("clientSecret");
        regNaver.setRedirectUri("redirectUri");
        regNaver.setAuthorizationGrantType("authorization_code");

        OAuthProperties.Provider.Naver providerNaver = new OAuthProperties.Provider.Naver();
        providerNaver.setTokenUri("token_uri");
        providerNaver.setUserInfoUri("user_info_uri");

        OAuthProperties.Registration registration = new OAuthProperties.Registration();
        registration.setNaver(regNaver);
        when(oAuthProperties.getRegistration()).thenReturn(registration);

        OAuthProperties.Provider provider = new OAuthProperties.Provider();
        provider.setNaver(providerNaver);
        when(oAuthProperties.getProvider()).thenReturn(provider);
    }

    private NaverUserInfoResponse createNaverUserInfoResponse(
            String id, String name, String nickname, String email, String image, String phone) {

        NaverUserInfoResponse.Response r = new NaverUserInfoResponse.Response();
        ReflectionTestUtils.setField(r, "id", id);
        ReflectionTestUtils.setField(r, "name", name);
        ReflectionTestUtils.setField(r, "nickname", nickname);
        ReflectionTestUtils.setField(r, "email", email);
        ReflectionTestUtils.setField(r, "profile_image", image);
        ReflectionTestUtils.setField(r, "mobile", phone);

        NaverUserInfoResponse response = new NaverUserInfoResponse();
        ReflectionTestUtils.setField(response, "resultcode", "00");
        ReflectionTestUtils.setField(response, "message", "success");
        ReflectionTestUtils.setField(response, "response", r);

        return response;
    }

    private User createUserFromNaver(NaverUserInfo naverUserInfo) {
        return User.builder()
                .email(naverUserInfo.getEmail())
                .name(naverUserInfo.getName())
                .nickname(naverUserInfo.getNickname())
                .userRole(UserRole.ROLE_USER)
                .oauthId(naverUserInfo.getId())
                .oauthProvider(OAuthProvider.NAVER)
                .imageUrl(naverUserInfo.getProfileImage())
                .phoneNumber(PhoneNumberNormalizer.normalize(naverUserInfo.getPhoneNumber()))
                .build();
    }

    // 네이버 인가 코드로 access_token을 요청하는 WebClient mocking
    private void mockWebClientTokenRequest(String responseJson) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any()))
                .thenAnswer(invocation -> requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseJson));
    }

    // 발급받은 access_token으로 사용자 정보 요청하는 WebClient mocking
    private void mockWebClientUserInfoRequest(NaverUserInfoResponse response) {
        when(webClient.get()).thenAnswer(invocation -> requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString()))
                .thenAnswer(invocation -> requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString()))
                .thenAnswer(invocation -> requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(NaverUserInfoResponse.class))
                .thenReturn(Mono.just(response));
    }

    @Nested
    class 로그인 {

        @Test
        void 액세스토큰_JSON파싱_실패_예외_발생() throws Exception {
            // given
            String invalidAccessTokenJson = "invalid json";
            mockWebClientTokenRequest(invalidAccessTokenJson); // 응답은 왔지만 JSON 파싱이 불가능한 경우
            when(objectMapper.readTree(invalidAccessTokenJson))
                    .thenThrow(new RuntimeException("JSON 파싱 실패"));

            // when & then
            assertThrows(HandledException.class, () -> {
                naverAuthService.login(AUTHORIZATION_CODE);
            });
        }

        @Test
        void 기존유저_성공() throws Exception {
            // given
            String accessTokenJson = createAccessTokenJson(NAVER_ACCESS_TOKEN); // 네이버 서버에서 받은 토큰 응답 가정
            NaverUserInfoResponse response = createNaverUserInfoResponse(
                    "id123", "유저", "닉네임", "user@test.com", "image-url", "010-1234-5678");
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
            TokenResponse tokenResponse = naverAuthService.login(AUTHORIZATION_CODE);

            // then
            assertEquals("access_token", tokenResponse.getAccessToken());
            assertEquals("refresh_token", tokenResponse.getRefreshToken());
        }

        @Test
        void 신규회원가입_성공() throws Exception {
            // given
            String accessTokenJson = createAccessTokenJson(NAVER_ACCESS_TOKEN);
            NaverUserInfoResponse response = createNaverUserInfoResponse(
                    "id999", "새유저", "새유저닉네임", "newuser@test.com", "image-url", "010-0000-0000");

            NaverUserInfo naverUserInfo = NaverUserInfo.fromNaverUserInfoResponse(response.getResponse());
            User newUser = createUserFromNaver(naverUserInfo);

            mockWebClientTokenRequest(accessTokenJson);
            mockWebClientUserInfoRequest(response);
            stubJsonParsing(objectMapper, accessTokenJson);

            when(userRepository.findByEmail("newuser@test.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(newUser);
            when(tokenService.createAccessToken(newUser)).thenReturn("access_token");
            when(tokenService.createRefreshToken(newUser)).thenReturn("refresh_token");

            // when
            TokenResponse tokenResponse = naverAuthService.login(AUTHORIZATION_CODE);

            // then
            assertEquals("access_token", tokenResponse.getAccessToken());
            assertEquals("refresh_token", tokenResponse.getRefreshToken());
        }
    }
}