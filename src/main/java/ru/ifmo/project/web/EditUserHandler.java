package ru.ifmo.project.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.ifmo.project.model.Role;
import ru.ifmo.project.model.User;
import ru.ifmo.project.service.UserService;
import ru.ifmo.project.util.PasswordUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class EditUserHandler implements HttpHandler {
    private final SessionManager sessionManager;
    private final UserService userService;

    public EditUserHandler(SessionManager sessionManager, UserService userService) {
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
        User currentUser = userService.findByUsername(username);
        if (currentUser.getRole() != Role.ADMIN) {
            Helper.sendHtml(exchange, HtmlTemplate.wrap("Доступ запрещён", "<h2>Доступ запрещён</h2><a href='/'>На главную</a>", true));
            return;
        }
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.startsWith("id=")) {
            Helper.redirect(exchange, "/users");
            return;
        }
        long id = Long.parseLong(query.substring(3));
        User user = userService.findById(id);
        if (user == null) {
            Helper.redirect(exchange, "/users");
            return;
        }
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            StringBuilder form = new StringBuilder("<h2>Редактирование пользователя</h2><form method='post'>");
            form.append("Логин: <input name='username' value='").append(user.getUsername()).append("'/><br/>");
            form.append("Полное имя: <input name='fullName' value='").append(user.getFullName()).append("'/><br/>");
            form.append("Роль: <select name='role'><option ").append("ADMIN".equals(user.getRole().name()) ? "selected" : "").append(">ADMIN</option>");
            form.append("<option ").append("USER".equals(user.getRole().name()) ? "selected" : "").append(">USER</option></select><br/>");
            form.append("Новый пароль (оставьте пустым, если не менять): <input type='password' name='password'/><br/>");
            form.append("<input type='submit' value='Сохранить'/></form><a href='/users'>Назад</a>");
            Helper.sendHtml(exchange, HtmlTemplate.wrap("Редактирование пользователя", form.toString(), true));
        } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = Helper.parseFormData(body);
            user.setUsername(params.get("username"));
            user.setFullName(params.get("fullName"));
            user.setRole(Role.valueOf(params.get("role")));
            String newPass = params.get("password");
            if (newPass != null && !newPass.isEmpty()) {
                user.setPasswordHash(PasswordUtil.hashPassword(newPass));
            }
            userService.updateUser(user);
            Helper.redirect(exchange, "/users");
        }
    }
}