package org.example.tablenow.domain.event.message.consumer;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.event.service.EventOpenRetryService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;

import static org.example.tablenow.global.constant.RabbitConstant.EVENT_OPEN_DLQ;

@Component
@RequiredArgsConstructor
public class EventOpenDlqReprocessor {

    private final EventOpenRetryService eventOpenRetryService;

    @RabbitListener(queues = EVENT_OPEN_DLQ)
    public void reprocess(Message message) {
        eventOpenRetryService.process(message);
    }
}
