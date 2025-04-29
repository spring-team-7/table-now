package org.example.tablenow.domain.store.service;

import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.entity.StoreDocument;
import org.example.tablenow.domain.store.repository.StoreRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class StoreElasticsearchIndexerTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private ElasticsearchOperations operations;

    @Mock
    private IndexOperations indexOperations;

    @InjectMocks
    private StoreElasticsearchIndexer storeElasticsearchIndexer;

    @Test
    void Elasticsearch_인덱스_일괄_삭제_중_예외_발생() {
        // given
        Store store = mock(Store.class);
        StoreDocument storeDocument = mock(StoreDocument.class);

        given(storeRepository.findAllWithUserAndCategory()).willReturn(List.of(store));
        given(operations.indexOps(StoreDocument.class)).willReturn(indexOperations);
        given(indexOperations.exists()).willReturn(true);
        given(operations.save(anyList())).willThrow(new HandledException(ErrorCode.STORE_ELASTICSEARCH_INDEX_FAILED)); // 인덱스 저장 중 예외 발생

        try (MockedStatic<StoreDocument> storeDocStaticMock = mockStatic(StoreDocument.class)) {
            storeDocStaticMock.when(() -> StoreDocument.fromStore(store))
                    .thenReturn(storeDocument);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () -> {
                storeElasticsearchIndexer.reindexAllStores();
            });
            assertEquals(exception.getMessage(), ErrorCode.STORE_ELASTICSEARCH_INDEX_FAILED.getDefaultMessage());
        }
    }

    @Test
    void Elasticsearch_인덱스_일괄_삭제_및_생성() {
        // given
        Store store = mock(Store.class);
        StoreDocument storeDocument = mock(StoreDocument.class);

        given(storeRepository.findAllWithUserAndCategory()).willReturn(List.of(store));
        given(operations.indexOps(StoreDocument.class)).willReturn(indexOperations);
        given(indexOperations.exists()).willReturn(true);

        try (MockedStatic<StoreDocument> storeDocStaticMock = mockStatic(StoreDocument.class)) {
            storeDocStaticMock.when(() -> StoreDocument.fromStore(store))
                    .thenReturn(storeDocument);

            // when
            storeElasticsearchIndexer.reindexAllStores();

            // then
            verify(indexOperations).delete();
            verify(indexOperations).create();
            verify(indexOperations).putMapping();
            verify(operations).save(List.of(storeDocument));
        }
    }
}
