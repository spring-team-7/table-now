package org.example.tablenow.domain.notification.message.vacancy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.notification.message.vacancy.dto.VacancyEventDto;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

import static org.example.tablenow.global.constant.RabbitConstant.VACANCY_EXCHANGE;
import static org.example.tablenow.global.constant.RabbitConstant.VACANCY_ROUTING_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class VacancyRetryService {

    private final RabbitTemplate rabbitTemplate;

    private static final String RETRY_HEADER = "x-retry-count";
    private static final int MAX_RETRY_COUNT = 3;

    public void process(Message message) {
        try {
            VacancyEventDto event = parseMessage(message);
            int retryCount = extractRetryCount(message);

            if (retryCount >= MAX_RETRY_COUNT) {
                log.warn("[DLQ] 재처리 횟수 초과 → storeId={}, retryCount={}", event.getStoreId(), retryCount);
                return;
            }

            resendMessage(event, retryCount + 1);

        } catch (Exception e) {
            log.error("[DLQ] 재처리 중 예외 발생", e);
        }
    }

    // 메시지를 DTO로 변환
    private VacancyEventDto parseMessage(Message message) {
        Object object = rabbitTemplate.getMessageConverter().fromMessage(message);
        // 수동 퍼블리시일 경우
        if (object instanceof Map map) {
            return VacancyEventDto.builder()
                .storeId(((Number) map.get("storeId")).longValue())
                .waitDate(LocalDate.parse((String) map.get("waitDate")))
                .build();
        }

        return (VacancyEventDto) object;
    }

    // 메시지 헤더에서 재시도 횟수 추출
    private int extractRetryCount(Message message) {
        return (Integer) message.getMessageProperties()
            .getHeaders()
            .getOrDefault(RETRY_HEADER, 0);
    }

    // 메시지에 재시도 횟수 증가 후 다시 전송
    private void resendMessage(VacancyEventDto event, int nextRetryCount) {
        MessageProperties props = new MessageProperties();
        props.setHeader(RETRY_HEADER, nextRetryCount);

        Message retryMessage = rabbitTemplate.getMessageConverter().toMessage(event, props);
        rabbitTemplate.send(VACANCY_EXCHANGE, VACANCY_ROUTING_KEY, retryMessage);

        log.info("[DLQ] 재처리 시도 완료 → storeId={}, retryCount={}", event.getStoreId(), nextRetryCount);
    }
}