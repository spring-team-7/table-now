package org.example.tablenow.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "chat_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false)
    private Long reservationId;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private Long reservationUserId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public ChatMessage(Long reservationId, User sender, Long ownerId, Long reservationUserId, String content, String imageUrl, boolean isRead, LocalDateTime createdAt) {
        this.reservationId = reservationId;
        this.sender = sender;
        this.ownerId = ownerId;
        this.reservationUserId = reservationUserId;
        this.content = content;
        this.imageUrl = imageUrl;
        this.isRead = isRead;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public void changeToRead() {
        this.isRead = true;
    }
}
