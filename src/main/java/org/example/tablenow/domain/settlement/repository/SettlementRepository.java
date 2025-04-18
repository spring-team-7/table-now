package org.example.tablenow.domain.settlement.repository;

import org.example.tablenow.domain.settlement.entity.Settlement;
import org.example.tablenow.domain.settlement.enums.SettlementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    boolean existsByPaymentId(Long paymentId);

    List<Settlement> findAllByStatus(SettlementStatus settlementStatus);

    @Query("""
    SELECT s FROM Settlement s
    JOIN FETCH s.payment p
    JOIN FETCH p.reservation r
    JOIN FETCH r.store st
    WHERE st.user.id = :ownerId
""")
    Page<Settlement> findByStoreOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);
}
