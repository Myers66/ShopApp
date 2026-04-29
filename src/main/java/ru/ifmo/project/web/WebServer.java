package ru.ifmo.project.web;

import com.sun.net.httpserver.HttpServer;
import ru.ifmo.project.service.UserService;
import ru.ifmo.project.service.ProductService;
import ru.ifmo.project.service.AuthService;

import java.io.IOException;
import java.net.InetSocketAddress;

public class WebServer {
    private final int port;
    private HttpServer server;
    private final UserService userService;
    private final ProductService productService;
    private final AuthService authService;

    public WebServer(int port) {
        this.port = port;
        this.userService = new UserService();
        this.productService = new ProductService();
        this.authService = AuthService.getInstance();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        // контексты будут добавлены позже
        System.out.println("Веб-сервер запущен на http://localhost:" + port);
        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}