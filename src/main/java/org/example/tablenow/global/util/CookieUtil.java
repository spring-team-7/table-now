package org.example.tablenow.global.util;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

public class CookieUtil {

    private CookieUtil() {}

    public static void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
