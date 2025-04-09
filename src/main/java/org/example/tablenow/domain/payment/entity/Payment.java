package org.example.tablenow.domain.payment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.example.tablenow.domain.payment.enums.PaymentStatus;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.global.entity.TimeStamped;

@Getter
@Entity
@Table(name = "payment")
public class Payment extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String paymentKey;

    @NotBlank
    private String method;

    @NotNull
    @Min(0)
    private int price;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    public Payment() {}

    public Payment(String paymentKey, String method, Integer price, PaymentStatus status, User user, Reservation reservation) {
        this.paymentKey = paymentKey;
        this.method = method;
        this.price = price;
        this.status = status;
        this.user = user;
        this.reservation = reservation;
    }

    public void changePaymentMethod(String mothod) {
        this.method = mothod;
        super.update();
    }

    public void changePaymentStatus(PaymentStatus status) {
        this.status = status;
        super.update();
    }
}
