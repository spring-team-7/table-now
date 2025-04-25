package org.example.tablenow.domain.notification.message.vacancy.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.notification.message.vacancy.dto.VacancyEventDto;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

import static org.example.tablenow.global.constant.RabbitConstant.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class VacancyDlqReprocessor {

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = VACANCY_DLQ)
    public void reprocess(Message message) {
        try {
            Object object = rabbitTemplate.getMessageConverter().fromMessage(message);

            // Map → DTO 변환 (수동 퍼블리시 할 경우)
            VacancyEventDto event = (object instanceof Map map)
                ? VacancyEventDto.builder()
                .storeId(((Number) map.get("storeId")).longValue())
                .waitDate(LocalDate.parse((String) map.get("waitDate")))
                .build()
                : (VacancyEventDto) object;

            // retry count 확인
            Integer retryCount = (Integer) message.getMessageProperties().getHeaders().getOrDefault("x-retry-count", 0);
            if (retryCount >= 3) {
                log.warn("[DLQ] 재처리 횟수 초과 → storeId={}, retryCount={}", event.getStoreId(), retryCount);
                return;
            }

            // retryCount +1 설정 후 재전송
            MessageProperties props = new MessageProperties();
            props.setHeader("x-retry-count", retryCount + 1);
            Message retryMessage = rabbitTemplate.getMessageConverter().toMessage(event, props);

            rabbitTemplate.send(VACANCY_EXCHANGE, VACANCY_ROUTING_KEY, retryMessage);
            log.info("[DLQ] 재처리 시도 완료 → storeId={}, retryCount={}", event.getStoreId(), retryCount + 1);

        } catch (Exception e) {
            log.error("[DLQ] 재처리 중 예외 발생", e);
        }
    }
}