package com.rental.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    @Value("${cookie.secure:false}")   // default false for local dev, set true in production
    private boolean secure;

    public void setTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 1 day (matches JWT expiry)
        response.addCookie(cookie);
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
        response.addCookie(cookie);
    }

    public void clearTokenCookies(HttpServletResponse response) {
        Cookie token = new Cookie("token", null);
        token.setHttpOnly(true);
        token.setSecure(secure);
        token.setPath("/");
        token.setMaxAge(0);
        response.addCookie(token);

        Cookie refresh = new Cookie("refreshToken", null);
        refresh.setHttpOnly(true);
        refresh.setSecure(secure);
        refresh.setPath("/");
        refresh.setMaxAge(0);
        response.addCookie(refresh);
    }
}