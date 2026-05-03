package ru.ifmo.project.web;

import com.sun.net.httpserver.HttpServer;
import ru.ifmo.project.service.AuthService;
import ru.ifmo.project.service.ProductService;
import ru.ifmo.project.service.UserService;
import java.io.IOException;
import java.net.InetSocketAddress;

public class WebServer {
    private final int port;
    private HttpServer server;
    private final UserService userService;
    private final ProductService productService;
    private final AuthService authService;
    private final SessionManager sessionManager;

    public WebServer(int port) {
        this.port = port;
        this.userService = new UserService();
        this.productService = new ProductService();
        this.authService = AuthService.getInstance();
        this.sessionManager = new SessionManager();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RootHandler(sessionManager, userService));
        server.createContext("/login", new LoginHandler(sessionManager, authService, userService));
        server.createContext("/users", new UsersHandler(sessionManager, userService));
        server.createContext("/products", new ProductsHandler(sessionManager, productService));
        server.createContext("/editUser", new EditUserHandler(sessionManager, userService));
        server.createContext("/editProduct", new EditProductHandler(sessionManager, productService));
        server.setExecutor(null);
        server.start();
        System.out.println("Веб-сервер запущен на http://localhost:" + port);
    }

    public void stop() {
        if (server != null) server.stop(0);
    }
}