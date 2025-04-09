package org.example.tablenow.global.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String registrationId = token.getAuthorizedClientRegistrationId();

        String redirectUrl;
        if ("kakao".equals(registrationId)) {
            redirectUrl = "/api/v1/auth/kakao";
        } else if ("naver".equals(registrationId)) {
            redirectUrl = "/api/v1/auth/naver";
        } else {
            redirectUrl = "/";
        }

        response.sendRedirect(redirectUrl);
    }
}
