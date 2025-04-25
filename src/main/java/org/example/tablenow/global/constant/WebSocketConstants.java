package org.example.tablenow.global.constant;

public final class WebSocketConstants {

    // WebSocket 연결 시 사용할 엔드포인트 (SockJS에서 접속 경로로 사용됨)
    public static final String ENDPOINT_CHAT = "/ws/chat";

    // 클라이언트가 메시지를 보낼 때 사용하는 prefix
    // ex) stompClient.send("/app/chat/message", ...)
    public static final String APP_DEST_PREFIX = "/app";

    // 서버가 메시지를 브로드캐스트할 때 사용하는 prefix
    // ex) messagingTemplate.convertAndSend("/topic/...")
    public static final String TOPIC_PREFIX = "/topic";

    // 채팅방 메시지를 브로드캐스트할 때 사용하는 prefix
    // ex) /topic/chat/{reservationId}
    public static final String TOPIC_CHAT_PREFIX = "/topic/chat/";

    private WebSocketConstants() {
        // 인스턴스 생성 방지
    }
}
