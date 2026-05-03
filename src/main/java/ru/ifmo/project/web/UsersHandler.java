package ru.ifmo.project.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.ifmo.project.model.Role;
import ru.ifmo.project.model.User;
import ru.ifmo.project.service.UserService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UsersHandler implements HttpHandler {
    private final SessionManager sessionManager;
    private final UserService userService;

    public UsersHandler(SessionManager sessionManager, UserService userService) {
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

        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.startsWith("delete=")) {
                String idStr = query.substring(7);
                try {
                    long id = Long.parseLong(idStr);
                    userService.deleteUser(id);
                } catch (NumberFormatException ignored) {}
                Helper.redirect(exchange, "/users");
                return;
            }
            StringBuilder content = new StringBuilder("<h2>Управление пользователями</h2>");
            content.append("<h3>Создать пользователя</h3>");
            content.append("<form method='post'>Логин: <input name='username'/><br/>Пароль: <input name='password'/><br/>" +
                    "Полное имя: <input name='fullName'/><br/>Роль: <select name='role'><option>USER</option><option>ADMIN</option></select><br/>" +
                    "<input type='submit' value='Создать'/></form>");
            content.append("<h3>Список пользователей</h3><ul>");
            for (User u : userService.getAllUsers()) {
                content.append("<li>").append(u.getId()).append(": ").append(u.getUsername())
                        .append(" (").append(u.getRole()).append(") - ").append(u.getFullName())
                        .append(" <a href='/users?delete=").append(u.getId()).append("'>Удалить</a>")
                        .append(" <a href='/editUser?id=").append(u.getId()).append("'>Редактировать</a></li>");
            }
            content.append("</ul><a href='/'>На главную</a>");
            Helper.sendHtml(exchange, HtmlTemplate.wrap("Пользователи", content.toString(), true));
        } else if ("POST".equalsIgnoreCase(method)) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = Helper.parseFormData(body);
            String login = params.get("username");
            String pass = params.get("password");
            String fullName = params.get("fullName");
            String roleStr = params.get("role");
            Role role = "ADMIN".equalsIgnoreCase(roleStr) ? Role.ADMIN : Role.USER;
            try {
                userService.createUser(login, pass, role, fullName);
            } catch (Exception e) {
                // ignore
            }
            Helper.redirect(exchange, "/users");
        }
    }
}