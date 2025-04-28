package org.example.tablenow.global.config;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.global.constant.WebSocketConstants;
import org.example.tablenow.global.interceptor.JwtHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${chat.broker}")
    private String brokerType;
    @Value("${spring.rabbitmq.username}")
    private String rabbitmqUsername;
    @Value("${spring.rabbitmq.password}")
    private String rabbitmqPassword;

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

        if ("simple".equalsIgnoreCase(brokerType)) {
            // [SimpleBroker] 서버 메모리 브로커 사용
            registry.enableSimpleBroker(WebSocketConstants.TOPIC_PREFIX_SIMPLE);
        } else if ("rabbit".equalsIgnoreCase(brokerType)) {
            // [RabbitMQ Relay] MQ로 relay
            registry.enableStompBrokerRelay(WebSocketConstants.TOPIC_PREFIX_RELAY)
                    .setRelayHost("localhost")  // RabbitMQ 서버 주소
                    .setRelayPort(61613)        // RabbitMQ STOMP 포트 (TCP)
                    .setSystemLogin(rabbitmqUsername)
                    .setSystemPasscode(rabbitmqPassword)
                    .setClientLogin(rabbitmqUsername)
                    .setClientPasscode(rabbitmqPassword);
        } else {
            throw new IllegalArgumentException("지원하지 않는 brokerType입니다: " + brokerType);
        }
    }
}
