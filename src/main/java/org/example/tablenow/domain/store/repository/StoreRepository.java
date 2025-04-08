package org.example.tablenow.domain.store.repository;

import org.example.tablenow.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
}
