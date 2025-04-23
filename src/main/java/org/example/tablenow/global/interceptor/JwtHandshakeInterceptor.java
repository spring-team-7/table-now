package org.example.tablenow.global.interceptor;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.global.constant.SecurityConstants;
import org.example.tablenow.global.util.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    /* WebSocket 연결 시 handshake 단계에서 JWT 인증을 수행 - WebSocket 연결 허용 여부를 결정 */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String accessToken = extractToken(httpRequest);

            try {
                if (StringUtils.hasText(accessToken)) {
                    String jwt = jwtUtil.substringToken(accessToken);
                    Claims claims = jwtUtil.extractClaims(jwt);

                    Long userId = Long.valueOf(claims.getSubject());
                    attributes.put("userId", userId); // WebSocket 세션에 사용자 ID 저장

                    return true;
                }
            } catch (Exception e) {
                log.warn("[WebSocket] JWT 인증 실패: {}", e.getMessage());
                return false;
            }
        }

        return false; // 인증 실패 시 연결 거절
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }

    private String extractToken(HttpServletRequest httpRequest) {
        String accessToken = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        return (StringUtils.hasText(accessToken) && accessToken.startsWith(SecurityConstants.BEARER_PREFIX))
                ? accessToken : null;
    }
}
