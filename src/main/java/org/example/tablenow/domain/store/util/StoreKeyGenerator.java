package org.example.tablenow.domain.store.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoreKeyGenerator {
    public static String generateStoreListKey(int page, int size, String sort, String direction, Long categoryId, String search) {
        String key = String.format(
                "page=%d:size=%d:sort=%s:direction=%s:categoryId=%s:search=%s",
                page,
                size,
                sort,
                direction,
                categoryId != null ? categoryId : "",
                search != null ? search : ""
        );
        log.info("key stores: {}", key);
        return key;
    }
}
