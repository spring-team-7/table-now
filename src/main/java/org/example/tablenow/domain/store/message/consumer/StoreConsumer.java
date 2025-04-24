package org.example.tablenow.domain.store.message.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.store.entity.StoreDocument;
import org.example.tablenow.domain.store.message.dto.StoreEventDto;
import org.example.tablenow.domain.store.repository.StoreElasticRepository;
import org.example.tablenow.domain.store.service.StoreSearchService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static org.example.tablenow.global.rabbitmq.constant.RabbitConstant.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreConsumer {

    private final StoreElasticRepository storeElasticRepository;
    private final StoreSearchService storeSearchService;

    @RabbitListener(queues = STORE_CREATE_QUEUE)
    public void handleCreate(StoreEventDto event) {
        try {
            StoreDocument storeDocument = event.getStoreDocument();
            // ElasticSearch 인덱스 업데이트
            storeElasticRepository.updateStoreIndex(storeDocument);
            // Redis 캐시 키 탐색 및 선별 삭제
            storeSearchService.invalidateCacheOnNewStore(storeDocument);
        } catch (Exception e) {
            log.error("[StoreDocumentConsumer] MQ 처리 중 예외 발생", e);
        }
    }

    @RabbitListener(queues = STORE_UPDATE_QUEUE)
    public void handleUpdate(StoreEventDto event) {
        try {
            StoreDocument storeDocument = event.getStoreDocument();
            // ElasticSearch 인덱스 업데이트
            storeElasticRepository.updateStoreIndex(storeDocument);
            // Redis 캐시 삭제
            storeSearchService.invalidateStoreCacheByKey(storeDocument.getId());
        } catch (Exception e) {
            log.error("[StoreDocumentConsumer] MQ 처리 중 예외 발생", e);
        }
    }

    @RabbitListener(queues = STORE_DELETE_QUEUE)
    public void handleDelete(Long storeId) {
        try {
            // ElasticSearch 인덱스 삭제
            storeElasticRepository.deleteStoreIndex(storeId);
            // Redis 캐시 삭제
            storeSearchService.invalidateStoreCacheByKey(storeId);
        } catch (Exception e) {
            log.error("[StoreDocumentConsumer] MQ 처리 중 예외 발생", e);
        }
    }
}
