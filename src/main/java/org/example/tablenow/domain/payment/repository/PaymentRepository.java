package org.example.tablenow.domain.payment.repository;

import com.querydsl.core.Fetchable;
import org.example.tablenow.domain.payment.entity.Payment;
import org.example.tablenow.domain.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByReservationId(Long reservationId);

    @Query("""
    SELECT p FROM Payment p
    LEFT JOIN FETCH Settlement s ON s.payment = p
    WHERE p.status = 'DONE'
    AND s.id IS NULL
""")
    List<Payment> findUnsettledDonePayments();
}
