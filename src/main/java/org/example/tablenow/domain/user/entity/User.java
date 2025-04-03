package org.example.tablenow.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.example.tablenow.global.entity.TimeStamped;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "user")
public class User extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    private String imageUrl;
    private String oAuthProvider;
    private String oAuthId;
    private Boolean isAlarmEnabled;
    private LocalDateTime deletedAt;
}
