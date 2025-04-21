package org.example.tablenow.domain.store.repository;

import org.example.tablenow.domain.store.entity.Store;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreQueryRepository {
    @EntityGraph(attributePaths = {"category"}, type = EntityGraph.EntityGraphType.FETCH)
    Optional<Store> findByIdAndDeletedAtIsNull(Long id);

    @EntityGraph(attributePaths = {"user", "category"})
    @Query("SELECT s FROM Store s")
    List<Store> findAllWithUserAndCategory();
}
