package org.example.tablenow.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.global.entity.TimeStamped;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends TimeStamped {

    private static final long EXPIRATION_DAYS = 7; // 만료 시간 7일

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Builder
    public RefreshToken(Long userId) {
        this.userId = userId;
        this.token = UUID.randomUUID().toString();
        this.expiredAt = LocalDateTime.now().plusDays(EXPIRATION_DAYS);
    }

    public void updateToken() {
        this.token = UUID.randomUUID().toString();
        this.expiredAt = LocalDateTime.now().plusDays(EXPIRATION_DAYS); // 만료 시간 갱신
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }
}
