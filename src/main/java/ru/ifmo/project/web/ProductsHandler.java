package ru.ifmo.project.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.ifmo.project.model.Product;
import ru.ifmo.project.service.ProductService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ProductsHandler implements HttpHandler {
    private final SessionManager sessionManager;
    private final ProductService productService;

    public ProductsHandler(SessionManager sessionManager, ProductService productService) {
        this.sessionManager = sessionManager;
        this.productService = productService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!sessionManager.isAuthenticated(exchange)) {
            Helper.redirect(exchange, "/login");
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
                Helper.redirect(exchange, "/products");
                return;
            }
            StringBuilder content = new StringBuilder("<h2>Управление товарами</h2>");
            content.append("<h3>Добавить товар</h3>");
            content.append("<form method='post'>Название: <input name='name'/><br/>Цена: <input name='price' type='number' step='0.01'/><br/>" +
                    "Описание: <input name='description'/><br/>Количество: <input name='stockQuantity' type='number'/><br/>" +
                    "<input type='submit' value='Создать'/></form>");
            content.append("<h3>Список товаров</h3><ul>");
            for (Product p : productService.getAllProducts()) {
                content.append("<li>").append(p.getId()).append(": ").append(p.getName())
                        .append(" - ").append(p.getPrice()).append(" руб. (остаток: ").append(p.getStockQuantity()).append(")")
                        .append(" <a href='/products?delete=").append(p.getId()).append("'>Удалить</a>")
                        .append(" <a href='/editProduct?id=").append(p.getId()).append("'>Редактировать</a></li>");
            }
            content.append("</ul><a href='/'>На главную</a>");
            Helper.sendHtml(exchange, HtmlTemplate.wrap("Товары", content.toString(), true));
        } else if ("POST".equalsIgnoreCase(method)) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = Helper.parseFormData(body);
            String name = params.get("name");
            double price = Double.parseDouble(params.get("price"));
            String desc = params.get("description");
            int stock = Integer.parseInt(params.get("stockQuantity"));
            productService.createProduct(name, price, desc, stock);
            Helper.redirect(exchange, "/products");
        }
    }
}