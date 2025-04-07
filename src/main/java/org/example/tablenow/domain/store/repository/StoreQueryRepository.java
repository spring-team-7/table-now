package org.example.tablenow.domain.store.repository;

import org.example.tablenow.domain.store.dto.response.StoreSearchResponseDto;
import org.example.tablenow.domain.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StoreQueryRepository {
    Long countActiveStoresByUser(Long id);

    List<Store> findAllByUserId(Long id);

    Page<StoreSearchResponseDto> searchStores(Pageable pageable, Long categoryId, String keyword);
}
