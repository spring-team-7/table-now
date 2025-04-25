package org.example.tablenow.global.config;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.global.constant.WebSocketConstants;
import org.example.tablenow.global.interceptor.JwtHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    /* 클라이언트가 WebSocket 연결을 시도할 엔드포인트 URL 등록 */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(WebSocketConstants.ENDPOINT_CHAT)
                .setAllowedOriginPatterns("*")
                .addInterceptors(jwtHandshakeInterceptor)
                .withSockJS();
    }

    /* 클라이언트가 메시지를 보낼 때의 prefix 설정 */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes(WebSocketConstants.APP_DEST_PREFIX); // 클라이언트가 보낼 때 prefix
        registry.enableSimpleBroker(WebSocketConstants.TOPIC_PREFIX);                   // 서버가 브로드캐스트할 때 prefix
    }
}
