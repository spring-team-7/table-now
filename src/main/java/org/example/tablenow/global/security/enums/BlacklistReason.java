package org.example.tablenow.global.security.enums;

import lombok.Getter;

@Getter
public enum BlacklistReason {
    LOGOUT("logout"),
    REFRESH("refresh"),
    PASSWORD_CHANGE("password-change"),
    WITHDRAWAL("withdrawal");

    private final String value;

    BlacklistReason(String value) {
        this.value = value;
    }
}
