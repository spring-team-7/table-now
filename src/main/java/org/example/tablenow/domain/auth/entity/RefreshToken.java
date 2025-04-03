package org.example.tablenow.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.auth.TokenState;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String token;

    @Enumerated(EnumType.STRING)
    private TokenState tokenState;

    @Builder
    public RefreshToken(Long userId) {
        this.userId = userId;
        this.token = UUID.randomUUID().toString();
        this.tokenState = TokenState.VALID;
    }

    public void updateTokenStatus(TokenState tokenStatus){
        this.tokenState = tokenStatus;
    }
}
