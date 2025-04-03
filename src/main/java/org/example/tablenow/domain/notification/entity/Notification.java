package org.example.tablenow.domain.notification.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
