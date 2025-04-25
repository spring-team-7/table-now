package org.example.tablenow.domain.store.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoreKeyGenerator {
    public static String generateStoreListKey(int page, int size, String sort, String direction, Long categoryId, String keyword) {
        String key = String.format(
                "page=%d:size=%d:sort=%s:direction=%s:categoryId=%s:keyword=%s",
                page,
                size,
                sort,
                direction,
                categoryId != null ? categoryId : "",
                keyword != null ? keyword : ""
        );
        log.info("[Radis 키 생성] store:search: {}", key);
        return key;
    }

    public static String generateStoreKeyByPattern(String header, String parameter, String keyword) {
        String key = String.format("*%s*%s=%s*", header, parameter, keyword);
        log.info("[Radis 패턴 생성] {}", key);
        return key;
    }
}
