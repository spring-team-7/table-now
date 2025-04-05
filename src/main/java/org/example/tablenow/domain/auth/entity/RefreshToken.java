package org.example.tablenow.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.auth.TokenState;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String refreshToken;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenState tokenState;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public RefreshToken(Long userId) {
        this.userId = userId;
        this.refreshToken = UUID.randomUUID().toString();
        this.tokenState = TokenState.VALID;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateTokenStatus(TokenState tokenStatus){
        this.tokenState = tokenStatus;
        this.updatedAt = LocalDateTime.now();
    }
}
