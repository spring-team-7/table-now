package org.example.tablenow.domain.chat.message.consumer;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.chat.message.service.ChatRetryService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static org.example.tablenow.global.constant.RabbitConstant.CHAT_DLQ;

@Component
@RequiredArgsConstructor
public class ChatDlqReprocessor {

    private final ChatRetryService chatRetryService;

    @RabbitListener(queues = CHAT_DLQ)
    public void reprocess(Message message) {
        chatRetryService.process(message);
    }
}
