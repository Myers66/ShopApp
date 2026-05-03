package ru.ifmo.project.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.ifmo.project.service.UserService;
import java.io.IOException;

public class RootHandler implements HttpHandler {
    private final SessionManager sessionManager;
    private final UserService userService;

    public RootHandler(SessionManager sessionManager, UserService userService) {
        this.sessionManager = sessionManager;
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!sessionManager.isAuthenticated(exchange)) {
            Helper.redirect(exchange, "/login");
            return;
        }
        String username = sessionManager.getUsername(exchange);
        String content = "<h1>Панель управления</h1><p>Вы вошли как: " + username + "</p>" +
                "<ul><li><a href='/users'>Пользователи</a></li>" +
                "<li><a href='/products'>Товары</a></li>" +
                "<li><a href='/login?logout=1'>Выйти</a></li></ul>";
        Helper.sendHtml(exchange, HtmlTemplate.wrap("Главная", content, true));
    }
}