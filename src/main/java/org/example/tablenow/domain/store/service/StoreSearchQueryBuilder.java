package org.example.tablenow.domain.store.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.store.entity.StoreDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreSearchQueryBuilder {

    private final ElasticsearchOperations elasticsearchOperations;
    public static final String CATEGORY_ID = "categoryId";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String ADDRESS = "address";

    public Page<StoreDocument> searchByKeywordAndCategoryId(String keyword, Long categoryId, Pageable pageable) {
        Criteria criteria = new Criteria();

        // 선택 조건: 키워드 검색
        if (StringUtils.hasText(keyword)) {
            Criteria keywordCriteria = new Criteria(NAME).matches(keyword)
                    .or(new Criteria(DESCRIPTION).matches(keyword))
                    .or(new Criteria(ADDRESS).matches(keyword));
            criteria = criteria.and(keywordCriteria);
        }

        // 필수 조건: categoryId 일치
        if (categoryId != null) {
            criteria = criteria.and(CATEGORY_ID).is(categoryId);
        }

        CriteriaQuery criteriaQuery = new CriteriaQuery(criteria, pageable);
        SearchHits<StoreDocument> searchHits = elasticsearchOperations.search(criteriaQuery, StoreDocument.class);

        List<StoreDocument> content = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }
}
