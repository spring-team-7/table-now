package org.example.tablenow.domain.rating.repository;

import org.example.tablenow.domain.rating.entity.Rating;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RatingRepository extends CrudRepository<Rating, Long> {

    @Query("""
            SELECT COUNT(r) > 0
            FROM Rating r
            WHERE r.user.id = :userId
              AND r.store.id = :storeId
            """)
    boolean existsByUserAndStore(Long userId, Long storeId);

    Optional<Rating> findByUser_IdAndStore_Id(Long userId, Long storeId);
}
