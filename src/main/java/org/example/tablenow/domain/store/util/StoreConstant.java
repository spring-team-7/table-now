package org.example.tablenow.domain.store.util;

public class StoreConstant {
    // 인기 검색어 랭킹
    public static final String STORE_KEYWORD_RANK_KEY = "store:keyword";
    public static final String STORE_KEYWORD_USER_KEY = "store:keyword:user:";

    // 검색 Redis Key
    public static final String STORE_SEARCH_KEY = "store:search:";

    // 역인덱스 key
    public static final String STORE_CACHE_KEY = "store:cache-keys:";

    // ElasticSearch
    public static final String STORE_INDEX = "store";
    public static final String STORE_ANALYZER = "nori_analyzer";
}
