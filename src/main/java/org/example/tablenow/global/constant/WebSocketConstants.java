package org.example.tablenow.global.constant;

public final class WebSocketConstants {

    // WebSocket 연결 시 사용할 엔드포인트 (SockJS에서 접속 경로로 사용됨)
    public static final String ENDPOINT_CHAT = "/ws/chat";

    // 클라이언트가 메시지를 보낼 때 사용하는 prefix
    // ex) stompClient.send("/app/chat/message", ...)
    public static final String APP_DEST_PREFIX = "/app";

    // 서버-클라이언트 간 메시지 전달에 사용하는 브로커 경로 prefix
    // SimpleBroker 버전 (registry.enableSimpleBroker)
    public static final String TOPIC_PREFIX_SIMPLE = "/topic";
    // RabbitMQ Relay 버전 (registry.enableStompBrokerRelay)
    public static final String TOPIC_PREFIX_RELAY = "/exchange";

    // 채팅방 메시지를 브로드캐스트할 때 사용하는 prefix
    // SimpleBroker 버전 (/topic/chat/{reservationId})
    public static final String TOPIC_CHAT_PREFIX_SIMPLE = "/topic/chat/";
    // RabbitMQ Relay 버전 (/exchange/amq.topic/chat/{reservationId})
    public static final String TOPIC_CHAT_PREFIX_RELAY = "/exchange/amq.topic/chat.";

    private WebSocketConstants() {
        // 인스턴스 생성 방지
    }
}
