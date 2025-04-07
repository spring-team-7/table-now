package org.example.tablenow.global.dto;

import lombok.Getter;
import org.example.tablenow.domain.user.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
public class AuthUser {

    private final Long id;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String nickname;

    public AuthUser(Long id, String email, UserRole userRole, String nickname) {
        this.id = id;
        this.email = email;
        this.authorities = List.of(new SimpleGrantedAuthority(userRole.name()));
        this.nickname = nickname;
    }
}

