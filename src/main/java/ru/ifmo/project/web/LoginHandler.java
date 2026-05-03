package ru.ifmo.project.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.ifmo.project.exception.AuthenticationException;
import ru.ifmo.project.service.AuthService;
import ru.ifmo.project.service.UserService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class LoginHandler implements HttpHandler {
    private final SessionManager sessionManager;
    private final AuthService authService;
    private final UserService userService;

    public LoginHandler(SessionManager sessionManager, AuthService authService, UserService userService) {
        this.sessionManager = sessionManager;
        this.authService = authService;
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.contains("logout=1")) {
                sessionManager.logout(exchange);
                Helper.redirect(exchange, "/login");
                return;
            }
            String content = "<h2>Вход</h2>" +
                    "<form method='post'>Логин: <input name='username'/><br/>" +
                    "Пароль: <input type='password' name='password'/><br/>" +
                    "<input type='submit' value='Войти'/></form>" +
                    "<a href='/register.htm'>Регистрация</a>";
            Helper.sendHtml(exchange, HtmlTemplate.wrap("Вход", content, false));
        } else if ("POST".equalsIgnoreCase(method)) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = Helper.parseFormData(body);
            String username = params.get("username");
            String password = params.get("password");
            try {
                authService.login(username, password);
                sessionManager.createSession(exchange, username);
                Helper.redirect(exchange, "/");
            } catch (AuthenticationException e) {
                String content = "<h2>Ошибка: " + e.getMessage() + "</h2><a href='/login'>Назад</a>";
                Helper.sendHtml(exchange, HtmlTemplate.wrap("Ошибка входа", content, false));
            }
        }
    }
}