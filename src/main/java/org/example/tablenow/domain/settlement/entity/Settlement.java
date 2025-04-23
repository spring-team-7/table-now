package org.example.tablenow.domain.settlement.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.payment.entity.Payment;
import org.example.tablenow.domain.settlement.enums.SettlementStatus;
import org.example.tablenow.global.entity.TimeStamped;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;

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
    @Enumerated(EnumType.STRING)
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
        if (this.getStatus() == SettlementStatus.CANCELED) {
            throw new HandledException(ErrorCode.ALREADY_CANCELED);
        }

        if (this.getStatus() != SettlementStatus.DONE) {
            throw new HandledException(ErrorCode.INVALID_SETTLEMENT_STATUS);
        }

        this.status = SettlementStatus.CANCELED;
    }

    public static Settlement fromPayment(Payment payment) {
        return Settlement.builder()
                .payment(payment)
                .amount(payment.getPrice())
                .status(SettlementStatus.READY)
                .build();
    }
}
