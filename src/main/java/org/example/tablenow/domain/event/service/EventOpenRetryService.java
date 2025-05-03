package org.example.tablenow.domain.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static org.example.tablenow.global.constant.RabbitConstant.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventOpenRetryService {

    private final RabbitTemplate rabbitTemplate;

    private static final int MAX_RETRY_COUNT = 3;

    public void process(Message message) {
        MessageProperties props = message.getMessageProperties();
        Integer retryCount = (Integer) props.getHeaders().getOrDefault(RETRY_HEADER, 0);

        if (retryCount >= MAX_RETRY_COUNT) {
            log.error("[EventOpentRetryService] 재시도 최대 횟수 초과 → messageId={}, retryCount={}",
                    message.getMessageProperties().getMessageId(), retryCount);
            return;
        }

        Message retryMessage = MessageBuilder
                .withBody(message.getBody())
                .copyProperties(props)
                .setHeader(RETRY_HEADER, retryCount + 1)
                .build();

        rabbitTemplate.send(
                EVENT_OPEN_RETRY_EXCHANGE,
                EVENT_OPEN_RETRY_ROUTING_KEY,
                retryMessage
        );

        log.info("[EventOpentRetryService] DLQ 메시지 재전송 완료 → retryCount={}, routingKey={}, message={}",
                retryCount + 1, EVENT_OPEN_RETRY_ROUTING_KEY, new String(retryMessage.getBody()));
    }
}