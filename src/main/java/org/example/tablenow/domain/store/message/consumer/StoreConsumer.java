package org.example.tablenow.domain.store.message.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.store.entity.StoreDocument;
import org.example.tablenow.domain.store.repository.StoreElasticRepository;
import org.example.tablenow.domain.store.service.StoreSearchService;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.example.tablenow.global.constant.RabbitConstant.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final StoreElasticRepository storeElasticRepository;
    private final StoreSearchService storeSearchService;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRY_COUNT = 3;

    @RabbitListener(queues = STORE_CREATE_QUEUE)
    public void handleCreate(Message message) {
        try {
            StoreDocument storeDocument = updateIndexAndGetStoreDocument(message);
            storeSearchService.evictSearchCacheForNewStore(storeDocument);
        } catch (Exception e) {
            log.error("[StoreDocumentConsumer] MQ 처리 중 예외 발생 (create)", e);
            throw new HandledException(ErrorCode.STORE_RABBIT_MQ_MESSAGE_FAILED);
        }
    }

    @RabbitListener(queues = STORE_UPDATE_QUEUE)
    public void handleUpdate(Message message) {
        try {
            StoreDocument storeDocument = updateIndexAndGetStoreDocument(message);
            storeSearchService.evictSearchCacheByStoreId(storeDocument.getId());
        } catch (Exception e) {
            log.error("[StoreDocumentConsumer] MQ 처리 중 예외 발생 (update)", e);
            throw new HandledException(ErrorCode.STORE_RABBIT_MQ_MESSAGE_FAILED);
        }
    }

    @RabbitListener(queues = STORE_DELETE_QUEUE)
    public void handleDelete(Message message) {
        try {
            Long storeId = objectMapper.readValue(message.getBody(), Long.class);
            storeElasticRepository.deleteStoreIndex(storeId);
            storeSearchService.evictSearchCacheByStoreId(storeId);
        } catch (Exception e) {
            log.error("[StoreDocumentConsumer] MQ 처리 중 예외 발생 (delete)", e);
            throw new HandledException(ErrorCode.STORE_RABBIT_MQ_MESSAGE_FAILED);
        }
    }

    // DLQ Consumers
    @RabbitListener(queues = STORE_CREATE_DLQ)
    public void handleCreateDlq(Message message) {
        sendRetryMessage(message, STORE_CREATE_QUEUE);
    }

    @RabbitListener(queues = STORE_UPDATE_DLQ)
    public void handleUpdateDlq(Message message) {
        sendRetryMessage(message, STORE_UPDATE_QUEUE);
    }

    @RabbitListener(queues = STORE_DELETE_DLQ)
    public void handleDeleteDlq(Message message) {
        sendRetryMessage(message, STORE_DELETE_QUEUE);
    }

    private void sendRetryMessage(Message message, String mainQueue) {
        MessageProperties props = message.getMessageProperties();
        Integer retryCount = (Integer) props.getHeaders().getOrDefault("x-retry-count", 0);

        if (retryCount >= MAX_RETRY_COUNT) {
            log.warn("DLQ 재시도 {}회 초과 → 관리자 확인 필요: {}", MAX_RETRY_COUNT, new String(message.getBody()));
            return;
        }

        Message retryMessage = getRetryMessage(message, mainQueue, props, retryCount);
        rabbitTemplate.convertAndSend(mainQueue, retryMessage);
    }

    private static Message getRetryMessage(Message message, String mainQueue, MessageProperties props, Integer retryCount) {
        Message retryMessage = MessageBuilder
                .withBody(message.getBody())
                .copyHeaders(props.getHeaders())
                .setHeader("x-retry-count", retryCount + 1)
                .build();

        log.warn("DLQ 메시지 재전송 ({}회): {}", retryCount + 1, mainQueue);
        return retryMessage;
    }

    private StoreDocument updateIndexAndGetStoreDocument(Message message) throws IOException {
        StoreDocument storeDocument = objectMapper.readValue(message.getBody(), StoreDocument.class);
        storeElasticRepository.updateStoreIndex(storeDocument);
        return storeDocument;
    }
}
