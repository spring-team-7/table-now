package org.example.tablenow.domain.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String paymentNumber;
    private String method;
    private BigDecimal price;
    private LocalDateTime createdAt;
}
