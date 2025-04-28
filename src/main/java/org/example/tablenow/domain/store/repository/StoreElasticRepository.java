package org.example.tablenow.domain.store.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.store.entity.StoreDocument;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.tablenow.domain.store.util.StoreConstant.STORE_INDEX;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StoreElasticRepository {

    private final ElasticsearchClient elasticsearchClient;

    /**
     * 가게 검색 Elastic 인덱스 조회
     */
    public Page<StoreDocument> searchByKeywordAndCategoryId(String keyword, Long categoryId, Pageable pageable) {
        try {
            // 동적 bool query 구성
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

            whereDeletedAtIsNull(boolQueryBuilder);

            whereKeywordContains(keyword, boolQueryBuilder);

            whereCategoryIdEq(categoryId, boolQueryBuilder);

            SearchResponse<StoreDocument> response = getStoreDocumentSearchResponse(pageable, boolQueryBuilder);

            List<StoreDocument> results = response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

            long totalHits = response.hits().total().value();

            return new PageImpl<>(results, pageable, totalHits);

        } catch (IOException e) {
            throw new HandledException(ErrorCode.STORE_ELASTICSEARCH_QUERY_FAILED);
        }
    }

    /**
     * 인덱스 갱신 또는 생성 (insert, update)
     */
    public void updateStoreIndex(StoreDocument storeDocument) {
        try {
            IndexRequest indexRequest = IndexRequest.of(i -> i
                    .index(STORE_INDEX)
                    .id(String.valueOf(storeDocument.getId()))
                    .document(storeDocument)
            );
            IndexResponse indexResponse = elasticsearchClient.index(indexRequest);
            log.info("[ElasticSearch] Index status: {}]", indexResponse.result());
        } catch (Exception e) {
            throw new HandledException(ErrorCode.STORE_ELASTICSEARCH_QUERY_FAILED);
        }
    }

    /**
     * 인덱스에서 문서 삭제 (delete)
     */
    public void deleteStoreIndex(Long storeId) {
        try {
            DeleteRequest request = DeleteRequest.of(i -> i
                    .index(STORE_INDEX)
                    .id(String.valueOf(storeId))
            );
            DeleteResponse deleteResponse = elasticsearchClient.delete(request);
            log.info("[ElasticSearch] Index status: {}]", deleteResponse.result());
        } catch (Exception e) {
            throw new HandledException(ErrorCode.STORE_ELASTICSEARCH_QUERY_FAILED);
        }
    }

    private void whereDeletedAtIsNull(BoolQuery.Builder boolQueryBuilder) {
        boolQueryBuilder.mustNot(m -> m
                .exists(e -> e
                        .field("deletedAt")
                )
        );
    }

    private void whereKeywordContains(String keyword, BoolQuery.Builder boolQueryBuilder) {
        if (keyword != null && !keyword.isBlank()) {
            boolQueryBuilder.must(m -> m
                    .multiMatch(q -> q
                            .query(keyword)
                            .fields("name^2.5",
                                    "name.keyword^3",
                                    "name.ngram^1.5",
                                    "name.edge_ngram^1.5",
                                    "description^2.5",
                                    "description.keyword^3",
                                    "address^2.5", "address.keyword^3")
                    )
            );
        }
    }

    private void whereCategoryIdEq(Long categoryId, BoolQuery.Builder boolQueryBuilder) {
        if (categoryId != null) {
            boolQueryBuilder.must(m -> m
                    .term(t -> t
                            .field("categoryId")
                            .value(categoryId)
                    )
            );
        }
    }

    private List<SortOptions> convertSort(Pageable pageable) {
        List<SortOptions> sortOptions = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            sortOptions.add(SortOptions.of(s -> s
                    .field(f -> f
                            .field(order.getProperty())
                            .order(order.isAscending() ? SortOrder.Asc : SortOrder.Desc)
                    )
            ));
        }
        return sortOptions;
    }

    private SearchResponse<StoreDocument> getStoreDocumentSearchResponse(Pageable pageable, BoolQuery.Builder boolQueryBuilder) throws IOException {
        SearchResponse<StoreDocument> response = elasticsearchClient.search(s -> s
                        .index(STORE_INDEX) // 인덱스명
                        .query(q -> q.bool(boolQueryBuilder.build()))
                        .from((int) pageable.getOffset())
                        .size(pageable.getPageSize())
                        .sort(convertSort(pageable)),
                StoreDocument.class
        );
        return response;
    }
}
