package ru.ifmo.project.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.ifmo.project.model.Product;
import ru.ifmo.project.service.ProductService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class EditProductHandler implements HttpHandler {
    private final SessionManager sessionManager;
    private final ProductService productService;

    public EditProductHandler(SessionManager sessionManager, ProductService productService) {
        this.sessionManager = sessionManager;
        this.productService = productService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!sessionManager.isAuthenticated(exchange)) {
            Helper.redirect(exchange, "/login");
            return;
        }
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.startsWith("id=")) {
            Helper.redirect(exchange, "/products");
            return;
        }
        long id = Long.parseLong(query.substring(3));
        Product product = productService.findById(id);
        if (product == null) {
            Helper.redirect(exchange, "/products");
            return;
        }
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            StringBuilder form = new StringBuilder("<h2>Редактирование товара</h2><form method='post'>");
            form.append("Название: <input name='name' value='").append(product.getName()).append("'/><br/>");
            form.append("Цена: <input name='price' type='number' step='0.01' value='").append(product.getPrice()).append("'/><br/>");
            form.append("Описание: <input name='description' value='").append(product.getDescription()).append("'/><br/>");
            form.append("Количество: <input name='stockQuantity' type='number' value='").append(product.getStockQuantity()).append("'/><br/>");
            form.append("<input type='submit' value='Сохранить'/></form><a href='/products'>Назад</a>");
            Helper.sendHtml(exchange, HtmlTemplate.wrap("Редактирование товара", form.toString(), true));
        } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = Helper.parseFormData(body);
            product.setName(params.get("name"));
            product.setPrice(Double.parseDouble(params.get("price")));
            product.setDescription(params.get("description"));
            product.setStockQuantity(Integer.parseInt(params.get("stockQuantity")));
            productService.updateProduct(product);
            Helper.redirect(exchange, "/products");
        }
    }
}