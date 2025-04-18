package org.example.tablenow.domain.settlement.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.payment.entity.Payment;
import org.example.tablenow.domain.settlement.enums.SettlementStatus;
import org.example.tablenow.global.entity.TimeStamped;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "settlement")
public class Settlement extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private SettlementStatus status;

    @Builder
    public Settlement(Payment payment, int amount, SettlementStatus status) {
        this.payment = payment;
        this.amount = amount;
        this.status = status;
    }

    public void done() {
        this.status = SettlementStatus.DONE;
    }

    public void cancel() {
        this.status = SettlementStatus.CANCELED;
    }
}
