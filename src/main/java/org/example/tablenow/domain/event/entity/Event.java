package org.example.tablenow.domain.event.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    private LocalDateTime openAt;
    private LocalDateTime eventTime;
    private int limitPeople;
    @Enumerated(EnumType.STRING)
    private EventStatus status;
    private LocalDateTime createdAt;
}
