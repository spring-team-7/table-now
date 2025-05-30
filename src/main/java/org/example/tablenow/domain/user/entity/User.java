package org.example.tablenow.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.auth.oAuth.config.OAuthProvider;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.entity.TimeStamped;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    private String password;

    @Column(nullable = false)
    private String name;

    private String nickname;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private OAuthProvider oauthProvider;

    private String oauthId;

    private Boolean isAlarmEnabled;

    private LocalDateTime deletedAt;

    @Builder
    public User(Long id, String email, String password, String name, String nickname, String phoneNumber, UserRole userRole,
                String oauthId, OAuthProvider oauthProvider, String imageUrl) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.userRole = userRole;
        this.oauthId = oauthId;
        this.oauthProvider = oauthProvider;
        this.imageUrl = imageUrl;
    }

    public static User fromAuthUser(AuthUser authUser) {
        return User.builder()
                .id(authUser.getId())
                .email(authUser.getEmail())
                .nickname(authUser.getNickname())
                .userRole(UserRole.of(authUser.getAuthorities().iterator().next().getAuthority()))
                .build();
    }

    public void updateAlarmSetting(boolean isAlarmEnabled){
        this.isAlarmEnabled = isAlarmEnabled;
    }

    public void deleteUser() {
        this.deletedAt = LocalDateTime.now();
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
    }

    public void updatePhoneNumber(String newPhoneNumber) {
        this.phoneNumber = newPhoneNumber;
    }

    public void updateImageUrl(String newImageUrl) {
        this.imageUrl = newImageUrl;
    }

    public boolean isOAuthUser() {
        return this.oauthProvider != null;
    }
}
