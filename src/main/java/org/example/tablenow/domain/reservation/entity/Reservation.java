package org.example.tablenow.domain.reservation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.example.tablenow.global.entity.TimeStamped;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Entity
@Table(name = "reservation")
public class Reservation extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    private LocalTime time;
    private int numOfPeople;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
    private LocalDateTime deletedAt;
}
