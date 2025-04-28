package org.example.tablenow.domain.notification.message.vacancy.consumer;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.notification.message.vacancy.service.VacancyRetryService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static org.example.tablenow.global.constant.RabbitConstant.VACANCY_DLQ;

@Component
@RequiredArgsConstructor
public class VacancyDlqReprocessor {

    private final VacancyRetryService vacancyRetryService;

    @RabbitListener(queues = VACANCY_DLQ)
    public void reprocess(Message message) {
        vacancyRetryService.process(message);
    }
}