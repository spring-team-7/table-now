package org.example.tablenow.global.rabbitmq.event.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.global.rabbitmq.constant.RabbitConstant;
import org.example.tablenow.global.rabbitmq.event.dto.EventOpenMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventOpenProducer {
    private final RabbitTemplate rabbitTemplate;

    public void send(EventOpenMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitConstant.EVENT_OPEN_EXCHANGE,   // fanout exchange
                "",                                   // fanout이므로 routingKey는 빈 문자열
                message
        );

        log.info("[EventOpenProducer] 이벤트 오픈 메시지 발행 완료 → eventId={}, storeId={}, openAt={}",
                message.getEventId(),
                message.getStoreId(),
                message.getOpenAt());
    }
}
