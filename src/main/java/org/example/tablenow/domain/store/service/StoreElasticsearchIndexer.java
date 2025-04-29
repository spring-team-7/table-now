package org.example.tablenow.domain.store.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.entity.StoreDocument;
import org.example.tablenow.domain.store.repository.StoreRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreElasticsearchIndexer {

    private final StoreRepository storeRepository;
    private final ElasticsearchOperations operations;
    public static int INDEX_BATCH_SIZE = 1000;

    public void reindexAllStores() {
        IndexOperations indexOperations = operations.indexOps(StoreDocument.class);
        if (indexOperations.exists()) {
            indexOperations.delete();
        }
        indexOperations.create();
        indexOperations.putMapping();

        List<Store> stores = storeRepository.findAllWithUserAndCategory();
        List<StoreDocument> storeDocuments = stores.stream()
                .map(StoreDocument::fromStore)
                .collect(Collectors.toList());

        for (int i = 0; i < storeDocuments.size(); i += INDEX_BATCH_SIZE) {
            List<StoreDocument> batch = storeDocuments.subList(i, Math.min(i + INDEX_BATCH_SIZE, storeDocuments.size()));
            try {
                operations.save(batch);
            } catch (Exception e) {
                log.error("[ElasticSearch] 인덱스 생성 예외 발생", e.getMessage());
                throw new HandledException(ErrorCode.STORE_ELASTICSEARCH_INDEX_FAILED);
            }
        }
    }
}
