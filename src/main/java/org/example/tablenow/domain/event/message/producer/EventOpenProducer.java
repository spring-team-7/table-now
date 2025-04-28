package org.example.tablenow.domain.event.message.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.global.constant.RabbitConstant;
import org.example.tablenow.domain.event.message.dto.EventOpenMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Async
public class EventOpenProducer {
    private final RabbitTemplate rabbitTemplate;

    public void send(EventOpenMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitConstant.EVENT_OPEN_EXCHANGE,
                message
        );

        log.info("[EventOpenProducer] 이벤트 오픈 메시지 발행 완료 → eventId={}, storeId={}, openAt={}",
                message.getEventId(),
                message.getStoreId(),
                message.getOpenAt());
    }
}
