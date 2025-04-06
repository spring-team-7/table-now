package org.example.tablenow.domain.store.repository;

import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.user.entity.User;

import java.util.List;

public interface StoreQueryRepository {
    Long countActiveStoresByUser(Long id);

    List<Store> findAllByUserId(Long id);
}
