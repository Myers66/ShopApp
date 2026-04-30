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
    private final Map<String, String> sessions = new ConcurrentHashMap<>(); // sessionId -> username

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
        server.createContext("/editUser", new EditUserHandler());
        server.createContext("/editProduct", new EditProductHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Веб-сервер запущен на http://localhost:" + port);
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    // --- работа с Cookie ---
    private String getSessionIdFromCookie(HttpExchange exchange) {
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

    private void setSessionCookie(HttpExchange exchange, String sessionId) {
        exchange.getResponseHeaders().set("Set-Cookie", "JSESSIONID=" + sessionId + "; Path=/");
    }

    private boolean isAuthenticated(HttpExchange exchange) {
        String sessionId = getSessionIdFromCookie(exchange);
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

    // ---- Обработчики ----
    class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isAuthenticated(exchange)) {
                redirect(exchange, "/login");
                return;
            }
            String username = sessions.get(getSessionIdFromCookie(exchange));
            String html = "<html><body>" +
                    "<h1>Панель управления</h1>" +
                    "<p>Вы вошли как: " + username + "</p>" +
                    "<ul>" +
                    "<li><a href='/users'>Пользователи</a></li>" +
                    "<li><a href='/products'>Товары</a></li>" +
                    "<li><a href='/login?logout=1'>Выйти</a></li>" +
                    "</ul></body></html>";
            sendHtml(exchange, html);
        }
    }

    class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.contains("logout=1")) {
                    String sessionId = getSessionIdFromCookie(exchange);
                    if (sessionId != null) {
                        sessions.remove(sessionId);
                        exchange.getResponseHeaders().set("Set-Cookie", "JSESSIONID=; Max-Age=0");
                    }
                    redirect(exchange, "/login");
                    return;
                }
                String html = "<html><body><h2>Вход</h2>" +
                        "<form method='post'>Логин: <input name='username'/><br/>" +
                        "Пароль: <input type='password' name='password'/><br/>" +
                        "<input type='submit' value='Войти'/></form>" +
                        "<a href='/register.htm'>Регистрация</a></body></html>";
                sendHtml(exchange, html);
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseFormData(body);
                String username = params.get("username");
                String password = params.get("password");
                try {
                    authService.login(username, password);
                    String sessionId = UUID.randomUUID().toString();
                    sessions.put(sessionId, username);
                    setSessionCookie(exchange, sessionId);
                    redirect(exchange, "/");
                } catch (AuthenticationException e) {
                    String html = "<html><body><h2>Ошибка: " + e.getMessage() + "</h2><a href='/login'>Назад</a></body></html>";
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
            String method = exchange.getRequestMethod();
            String sessionId = getSessionIdFromCookie(exchange);
            String username = sessions.get(sessionId);
            User currentUser = userService.findByUsername(username);
            if (currentUser.getRole() != Role.ADMIN) {
                sendHtml(exchange, "<html><body><h2>Доступ запрещён</h2><a href='/'>На главную</a></body></html>");
                return;
            }

            // Обработка GET параметров delete
            if ("GET".equalsIgnoreCase(method)) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("delete=")) {
                    String idStr = query.substring(7);
                    try {
                        long id = Long.parseLong(idStr);
                        userService.deleteUser(id);
                    } catch (NumberFormatException ignored) {}
                    redirect(exchange, "/users");
                    return;
                }
                // Показать список и форму создания
                StringBuilder html = new StringBuilder("<html><body><h2>Управление пользователями</h2>");
                html.append("<h3>Создать пользователя</h3>");
                html.append("<form method='post'>Логин: <input name='username'/><br/>Пароль: <input name='password'/><br/>" +
                        "Полное имя: <input name='fullName'/><br/>Роль: <select name='role'><option>USER</option><option>ADMIN</option></select><br/>" +
                        "<input type='submit' value='Создать'/></form>");
                html.append("<h3>Список пользователей</h3><ul>");
                for (User u : userService.getAllUsers()) {
                    html.append("<li>").append(u.getId()).append(": ").append(u.getUsername())
                            .append(" (").append(u.getRole()).append(") - ").append(u.getFullName())
                            .append(" <a href='/users?delete=").append(u.getId()).append("'>Удалить</a>")
                            .append(" <a href='/editUser?id=").append(u.getId()).append("'>Редактировать</a></li>");
                }
                html.append("</ul><a href='/'>На главную</a></body></html>");
                sendHtml(exchange, html.toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseFormData(body);
                String login = params.get("username");
                String pass = params.get("password");
                String fullName = params.get("fullName");
                String roleStr = params.get("role");
                Role role = "ADMIN".equalsIgnoreCase(roleStr) ? Role.ADMIN : Role.USER;
                try {
                    userService.createUser(login, pass, role, fullName);
                } catch (Exception e) {
                    // можно показать ошибку
                }
                redirect(exchange, "/users");
            }
        }
    }

    class ProductsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isAuthenticated(exchange)) {
                redirect(exchange, "/login");
                return;
            }
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("delete=")) {
                    String idStr = query.substring(7);
                    try {
                        long id = Long.parseLong(idStr);
                        productService.deleteProduct(id);
                    } catch (NumberFormatException ignored) {}
                    redirect(exchange, "/products");
                    return;
                }
                StringBuilder html = new StringBuilder("<html><body><h2>Управление товарами</h2>");
                html.append("<h3>Добавить товар</h3>");
                html.append("<form method='post'>Название: <input name='name'/><br/>Цена: <input name='price' type='number' step='0.01'/><br/>" +
                        "Описание: <input name='description'/><br/>Количество: <input name='stockQuantity' type='number'/><br/>" +
                        "<input type='submit' value='Создать'/></form>");
                html.append("<h3>Список товаров</h3><ul>");
                for (Product p : productService.getAllProducts()) {
                    html.append("<li>").append(p.getId()).append(": ").append(p.getName())
                            .append(" - ").append(p.getPrice()).append(" руб. (остаток: ").append(p.getStockQuantity()).append(")")
                            .append(" <a href='/products?delete=").append(p.getId()).append("'>Удалить</a>")
                            .append(" <a href='/editProduct?id=").append(p.getId()).append("'>Редактировать</a></li>");
                }
                html.append("</ul><a href='/'>На главную</a></body></html>");
                sendHtml(exchange, html.toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseFormData(body);
                String name = params.get("name");
                double price = Double.parseDouble(params.get("price"));
                String desc = params.get("description");
                int stock = Integer.parseInt(params.get("stockQuantity"));
                productService.createProduct(name, price, desc, stock);
                redirect(exchange, "/products");
            }
        }
    }

    class EditUserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isAuthenticated(exchange)) {
                redirect(exchange, "/login");
                return;
            }
            String sessionId = getSessionIdFromCookie(exchange);
            String username = sessions.get(sessionId);
            User currentUser = userService.findByUsername(username);
            if (currentUser.getRole() != Role.ADMIN) {
                sendHtml(exchange, "<html><body><h2>Доступ запрещён</h2><a href='/'>На главную</a></body></html>");
                return;
            }
            String query = exchange.getRequestURI().getQuery();
            if (query == null || !query.startsWith("id=")) {
                redirect(exchange, "/users");
                return;
            }
            long id = Long.parseLong(query.substring(3));
            User user = userService.findById(id);
            if (user == null) {
                redirect(exchange, "/users");
                return;
            }
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String html = "<html><body><h2>Редактирование пользователя</h2>" +
                        "<form method='post'>Логин: <input name='username' value='" + user.getUsername() + "'/><br/>" +
                        "Полное имя: <input name='fullName' value='" + user.getFullName() + "'/><br/>" +
                        "Роль: <select name='role'><option " + ("ADMIN".equals(user.getRole().name()) ? "selected" : "") + ">ADMIN</option>" +
                        "<option " + ("USER".equals(user.getRole().name()) ? "selected" : "") + ">USER</option></select><br/>" +
                        "Новый пароль (оставьте пустым, если не менять): <input type='password' name='password'/><br/>" +
                        "<input type='submit' value='Сохранить'/></form><a href='/users'>Назад</a></body></html>";
                sendHtml(exchange, html);
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseFormData(body);
                user.setUsername(params.get("username"));
                user.setFullName(params.get("fullName"));
                user.setRole(Role.valueOf(params.get("role")));
                String newPass = params.get("password");
                if (newPass != null && !newPass.isEmpty()) {
                    user.setPasswordHash(ru.ifmo.project.util.PasswordUtil.hashPassword(newPass));
                }
                userService.updateUser(user);
                redirect(exchange, "/users");
            }
        }
    }

    class EditProductHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!isAuthenticated(exchange)) {
                redirect(exchange, "/login");
                return;
            }
            String query = exchange.getRequestURI().getQuery();
            if (query == null || !query.startsWith("id=")) {
                redirect(exchange, "/products");
                return;
            }
            long id = Long.parseLong(query.substring(3));
            Product product = productService.findById(id);
            if (product == null) {
                redirect(exchange, "/products");
                return;
            }
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String html = "<html><body><h2>Редактирование товара</h2>" +
                        "<form method='post'>Название: <input name='name' value='" + product.getName() + "'/><br/>" +
                        "Цена: <input name='price' type='number' step='0.01' value='" + product.getPrice() + "'/><br/>" +
                        "Описание: <input name='description' value='" + product.getDescription() + "'/><br/>" +
                        "Количество: <input name='stockQuantity' type='number' value='" + product.getStockQuantity() + "'/><br/>" +
                        "<input type='submit' value='Сохранить'/></form><a href='/products'>Назад</a></body></html>";
                sendHtml(exchange, html);
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseFormData(body);
                product.setName(params.get("name"));
                product.setPrice(Double.parseDouble(params.get("price")));
                product.setDescription(params.get("description"));
                product.setStockQuantity(Integer.parseInt(params.get("stockQuantity")));
                productService.updateProduct(product);
                redirect(exchange, "/products");
            }
        }
    }
}