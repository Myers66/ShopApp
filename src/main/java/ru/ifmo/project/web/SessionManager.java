package ru.ifmo.project.web;

import com.sun.net.httpserver.HttpExchange;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final Map<String, String> sessions = new ConcurrentHashMap<>(); // sessionId -> username

    public String getSessionIdFromCookie(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                String[] parts = cookie.trim().split("=");
                if (parts.length == 2 && "JSESSIONID".equals(parts[0])) {
                    return parts[1];
                }
            }
        }
        return null;
    }

    public void setSessionCookie(HttpExchange exchange, String sessionId) {
        exchange.getResponseHeaders().set("Set-Cookie", "JSESSIONID=" + sessionId + "; Path=/");
    }

    public boolean isAuthenticated(HttpExchange exchange) {
        String sessionId = getSessionIdFromCookie(exchange);
        return sessionId != null && sessions.containsKey(sessionId);
    }

    public String getUsername(HttpExchange exchange) {
        String sessionId = getSessionIdFromCookie(exchange);
        return sessionId != null ? sessions.get(sessionId) : null;
    }

    public void createSession(HttpExchange exchange, String username) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, username);
        setSessionCookie(exchange, sessionId);
    }

    public void logout(HttpExchange exchange) {
        String sessionId = getSessionIdFromCookie(exchange);
        if (sessionId != null) {
            sessions.remove(sessionId);
            exchange.getResponseHeaders().set("Set-Cookie", "JSESSIONID=; Max-Age=0");
        }
    }
}