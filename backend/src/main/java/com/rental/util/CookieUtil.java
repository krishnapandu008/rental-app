package com.rental.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public void setTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);  // only over HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 1 day (matches JWT expiry)
        response.addCookie(cookie);
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
        response.addCookie(cookie);
    }

    public void clearTokenCookies(HttpServletResponse response) {
        Cookie token = new Cookie("token", null);
        token.setHttpOnly(true);
        token.setSecure(true);
        token.setPath("/");
        token.setMaxAge(0);
        response.addCookie(token);

        Cookie refresh = new Cookie("refreshToken", null);
        refresh.setHttpOnly(true);
        refresh.setSecure(true);
        refresh.setPath("/");
        refresh.setMaxAge(0);
        response.addCookie(refresh);
    }
}