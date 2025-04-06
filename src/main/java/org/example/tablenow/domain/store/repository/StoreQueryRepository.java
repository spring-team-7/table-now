package org.example.tablenow.domain.store.repository;

import org.example.tablenow.domain.user.entity.User;

public interface StoreQueryRepository {
    Long countActiveStoresByUser(Long id);
}
