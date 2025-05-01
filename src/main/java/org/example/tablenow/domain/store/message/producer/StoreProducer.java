package org.example.tablenow.domain.store.message.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.entity.StoreDocument;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static org.example.tablenow.global.constant.RabbitConstant.*;


@Slf4j
@Component
@RequiredArgsConstructor
@Async
public class StoreProducer {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void publishStoreCreate(Store store) {
        StoreDocument storeDocument = StoreDocument.fromStore(store);
        sendMessage(storeDocument, STORE_CREATE, store.getId());
    }

    public void publishStoreUpdate(Store store) {
        StoreDocument storeDocument = StoreDocument.fromStore(store);
        sendMessage(storeDocument, STORE_UPDATE, store.getId());
    }

    public void publishStoreDelete(Long storeId) {
        sendMessage(storeId, STORE_DELETE, storeId);
    }

    private void sendMessage(Object payload, String routingKey, Object logId) {
        try {
            byte[] body = objectMapper.writeValueAsBytes(payload);
            Message message = MessageBuilder
                    .withBody(body)
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .setHeader("x-retry-count", 0) // 초기 전송 시 retry-count 0
                    .build();

            rabbitTemplate.send(STORE_EXCHANGE, routingKey, message);
            log.info("[StoreDocumentProducer] RabbitMQ {} 메시지 발행 : storeId={}", routingKey, logId);
        } catch (Exception e) {
            log.error("[StoreDocumentProducer] 메시지 직렬화 실패: {}", e.getMessage(), e);
            throw new HandledException(ErrorCode.STORE_SERIALIZATION_FAILED);
        }
    }
}
