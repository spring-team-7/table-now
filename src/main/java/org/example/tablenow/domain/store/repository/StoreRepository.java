package org.example.tablenow.domain.store.repository;

import org.example.tablenow.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreQueryRepository {
    Optional<Store> findByIdAndDeletedAtIsNull(Long id);
}
