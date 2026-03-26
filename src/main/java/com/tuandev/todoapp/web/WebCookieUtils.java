package com.tuandev.todoapp.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

/**
 * Helpers for the web demo authentication cookie.
 */
public final class WebCookieUtils {

    public static final String AUTH_COOKIE = "TODO_APP_AUTH";

    private WebCookieUtils() {}

    public static void addAuthCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(AUTH_COOKIE, token)
            .httpOnly(true)
            .path("/")
            .sameSite("Lax")
            .maxAge(maxAgeSeconds)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void clearAuthCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(AUTH_COOKIE, "").httpOnly(true).path("/").sameSite("Lax").maxAge(0).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
