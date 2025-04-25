package org.example.tablenow.domain.notification.message.vacancy.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.notification.message.vacancy.dto.VacancyEventDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static org.example.tablenow.global.rabbitmq.constant.RabbitConstant.VACANCY_EXCHANGE;
import static org.example.tablenow.global.rabbitmq.constant.RabbitConstant.VACANCY_ROUTING_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
// 예약 취소 발생 시, 해당 가게 ID와 날짜 정보를 담은 메시지를 RabbitMQ에 전송
public class VacancyProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendVacancyEvent(Long storeId, LocalDate waitDate) {
        VacancyEventDto event = VacancyEventDto.from(storeId, waitDate);

        rabbitTemplate.convertAndSend(VACANCY_EXCHANGE, VACANCY_ROUTING_KEY, event);
        log.info("[VacancyProducer] MQ 메시지 발행 → storeId={}, waitDate={}", storeId, waitDate);

    }
}