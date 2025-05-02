package org.example.tablenow.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.chat.config.RabbitMQProperties;
import org.example.tablenow.global.constant.WebSocketConstants;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.example.tablenow.global.interceptor.JwtHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final RabbitMQProperties rabbitMQProperties;

    @Value("${spring.rabbitmq.host}")
    private String rabbitMqHost;

    @Value("${spring.rabbitmq.port}")
    private int rabbitMqPort;


    @Value("${chat.broker}")
    private String brokerType;

    /* 클라이언트가 WebSocket 연결을 시도할 엔드포인트 URL 등록 */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(WebSocketConstants.ENDPOINT_CHAT)
                .setAllowedOriginPatterns("*")
                .addInterceptors(jwtHandshakeInterceptor);
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
                    .setRelayHost(rabbitMqHost)  // RabbitMQ 서버 주소
                    .setRelayPort(rabbitMqPort)    // RabbitMQ STOMP 포트 (TCP)
                    .setSystemLogin(rabbitMQProperties.getUsername())
                    .setSystemPasscode(rabbitMQProperties.getPassword())
                    .setClientLogin(rabbitMQProperties.getUsername())
                    .setClientPasscode(rabbitMQProperties.getPassword());
        } else {
            throw new HandledException(ErrorCode.UNSUPPORTED_CHAT_BROKER_TYPE);
        }
    }

    /* 클라이언트 → 서버로 들어오는 메시지를 가로챈다 */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                // 여기서 모든 수신 메시지를 잡을 수 있음
                log.info("[WebSocket Inbound] 수신한 메시지: {}", message.getHeaders());
                return message;
            }
        });
    }
}
