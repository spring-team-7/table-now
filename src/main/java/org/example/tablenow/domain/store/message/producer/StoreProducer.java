package org.example.tablenow.domain.store.message.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.message.dto.StoreEventDto;
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

    public void publishStoreCreate(Store store) {
        StoreEventDto event = StoreEventDto.fromStore(store);
        rabbitTemplate.convertAndSend(STORE_EXCHANGE, STORE_CREATE, event);
        log.info("[StoreDocumentProducer] RabbitMQ {} 메시지 발행 : storeId={}", STORE_CREATE, store.getId());
    }

    public void publishStoreUpdate(Store store) {
        StoreEventDto event = StoreEventDto.fromStore(store);
        rabbitTemplate.convertAndSend(STORE_EXCHANGE, STORE_UPDATE, event);
        log.info("[StoreDocumentProducer] RabbitMQ {} 메시지 발행 : storeId={}", STORE_UPDATE, store.getId());
    }

    public void publishStoreDelete(Long storeId) {
        rabbitTemplate.convertAndSend(STORE_EXCHANGE, STORE_DELETE, storeId);
        log.info("[StoreDocumentProducer] RabbitMQ {} 메시지 발행 : storeId={}", STORE_DELETE, storeId);
    }
}
