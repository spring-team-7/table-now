package org.example.tablenow.domain.reservation.message.customer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static org.example.tablenow.global.constant.RabbitConstant.RESERVATION_REMINDER_SEND_DLQ;
import static org.example.tablenow.global.constant.RabbitConstant.RESERVATION_REMINDER_SEND_RETRY_EXCHANGE;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderDlqReprocessor {
    private final RabbitTemplate rabbitTemplate;
    private static final int MAX_RETRY_COUNT = 3;

    @RabbitListener(queues = RESERVATION_REMINDER_SEND_DLQ)
    public void reprocess(Message message) {
        MessageProperties props = message.getMessageProperties();
        Integer retryCount = (Integer) props.getHeaders().getOrDefault("x-retry-count", 0);

        if (retryCount >= MAX_RETRY_COUNT) {
            log.error("[ReminderDlqReprocessor] 재시도 횟수 초과 → reservationId={}, message={}",
                    props.getHeaders().get("reservationId"), new String(message.getBody()));
            return;
        }

        // retryCount 증가 및 헤더 업데이트
        props.setHeader("x-retry-count", retryCount + 1);

        // Retry Exchange로 재전송 (TTL 큐 -> 메인 큐 자동 복귀)
        rabbitTemplate.send(
                RESERVATION_REMINDER_SEND_RETRY_EXCHANGE,
                message
        );

        log.warn("[ReminderDlqReprocessor] DLQ 메시지 재전송 완료 → retryCount={}, message={}",
                retryCount + 1, new String(message.getBody()));
    }
}
