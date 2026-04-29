package ru.ifmo.project.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ru.ifmo.project.exception.AuthenticationException;
import ru.ifmo.project.model.Product;
import ru.ifmo.project.model.Role;
import ru.ifmo.project.model.User;
import ru.ifmo.project.service.AuthService;
import ru.ifmo.project.service.ProductService;
import ru.ifmo.project.service.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WebServer {
    private final int port;
    private HttpServer server;
    private final UserService userService;
    private final ProductService productService;
    private final AuthService authService;
    // простая имитация сессий: sessionId -> имя пользователя
    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    public WebServer(int port) {
        this.port = port;
        this.userService = new UserService();
        this.productService = new ProductService();
        this.authService = AuthService.getInstance();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/users", new UsersHandler());
        server.createContext("/products", new ProductsHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Веб-сервер запущен на http://localhost:" + port);
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    // --- Вспомогательные методы ---
    private String getSessionId(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "sessionId".equals(pair[0])) {
                    return pair[1];
                }
            }
        }
        return null;
    }

    private boolean isAuthenticated(HttpExchange exchange) {
        String sessionId = getSessionId(exchange);
        return sessionId != null && sessions.containsKey(sessionId);
    }

    private void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    private void sendHtml(HttpExchange exchange, String html) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // --- Обработчики ---
    class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (isAuthenticated(exchange)) {
                String username = sessions.get(getSessionId(exchange));
                String html = "<html><body>" +
                        "<h1>Панель управления</h1>" +
                        "<p>Вы вошли как: " + username + "</p>" +
                        "<ul><li><a href='/users'>Пользователи</a></li>" +
                        "<li><a href='/products'>Товары</a></li>" +
                        "<li><a href='/login?logout=1'>Выйти</a></li></ul>" +
                        "</body></html>";
                sendHtml(exchange, html);
            } else {
                redirect(exchange, "/login");
            }
        }
    }

    class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if (method.equalsIgnoreCase("GET")) {
                // параметр logout?
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.contains("logout=1")) {
                    String sessionId = getSessionId(exchange);
                    if (sessionId != null) sessions.remove(sessionId);
                    redirect(exchange, "/login");
                    return;
                }
                // форма логина
                String html = "<html><body><h2>Вход</h2>" +
                        "<form method='post'>Логин: <input name='username'/><br/>" +
                        "Пароль: <input type='password' name='password'/><br/>" +
                        "<input type='submit' value='Войти'/></form>" +
                        "<p><a href='/register.htm'>Регистрация</a></p>" +
                        "</body></html>";
                sendHtml(exchange, html);
            } else if (method.equalsIgnoreCase("POST")) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseFormData(body);
                String username = params.get("username");
                String password = params.get("password");
                try {
                    authService.login(username, password);
                    // успешный вход: создаём сессию
                    String sessionId = UUID.randomUUID().toString();
                    sessions.put(sessionId, username);
                    redirect(exchange, "/?sessionId=" + sessionId);
                } catch (AuthenticationException e) {
                    String html = "<html><body><h2>Ошибка: " + e.getMessage() +
                            "</h2><a href='/login'>Назад</a></body></html>";
                    sendHtml(exchange, html);
                }
            }
        }
    }

    class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isAuthenticated(exchange)) {
                redirect(exchange, "/login");
                return;
            }
            String sessionId = getSessionId(exchange);
            String username = sessions.get(sessionId);
            User currentUser = userService.findByUsername(username);
            if (currentUser.getRole() != Role.ADMIN) {
                sendHtml(exchange, "<html><body><h2>Доступ запрещён</h2><a href='/'>На главную</a></body></html>");
                return;
            }
            // Покажем список пользователей и формы для CRUD
            StringBuilder html = new StringBuilder("<html><body><h2>Управление пользователями</h2>");
            html.append("<h3>Создать пользователя</h3>");
            html.append("<form method='post' action='/users'>" +
                    "Логин: <input name='username'/><br/>" +
                    "Пароль: <input name='password'/><br/>" +
                    "Полное имя: <input name='fullName'/><br/>" +
                    "Роль: <select name='role'><option>USER</option><option>ADMIN</option></select><br/>" +
                    "<input type='submit' value='Создать'/></form>");
            html.append("<h3>Список пользователей</h3><ul>");
            for (User u : userService.getAllUsers()) {
                html.append("<li>").append(u.getId()).append(": ").append(u.getUsername())
                        .append(" (").append(u.getRole()).append(") - ").append(u.getFullName())
                        .append(" <a href='/users?delete=").append(u.getId()).append("&sessionId=").append(sessionId)
                        .append("'>Удалить</a>")
                        .append(" <a href='/users?edit=").append(u.getId()).append("&sessionId=").append(sessionId)
                        .append("'>Редактировать</a></li>");
            }
            html.append("</ul><a href='/'>На главную</a></body></html>");
            sendHtml(exchange, html.toString());
        }
    }

    class ProductsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isAuthenticated(exchange)) {
                redirect(exchange, "/login");
                return;
            }
            // аналогично UsersHandler, но для товаров
            StringBuilder html = new StringBuilder("<html><body><h2>Управление товарами</h2>");
            html.append("<h3>Добавить товар</h3>");
            html.append("<form method='post' action='/products'>" +
                    "Название: <input name='name'/><br/>" +
                    "Цена: <input name='price'/><br/>" +
                    "Описание: <input name='description'/><br/>" +
                    "Количество: <input name='stockQuantity'/><br/>" +
                    "<input type='submit' value='Создать'/></form>");
            html.append("<h3>Список товаров</h3><ul>");
            for (Product p : productService.getAllProducts()) {
                html.append("<li>").append(p.getId()).append(": ").append(p.getName())
                        .append(" - ").append(p.getPrice()).append(" руб. (остаток: ").append(p.getStockQuantity())
                        .append(") <a href='/products?delete=").append(p.getId()).append("&sessionId=").append(getSessionId(exchange))
                        .append("'>Удалить</a></li>");
            }
            html.append("</ul><a href='/'>На главную</a></body></html>");
            sendHtml(exchange, html.toString());
        }
    }

    private Map<String, String> parseFormData(String body) {
        Map<String, String> params = new HashMap<>();
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                params.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }
}