package ru.ifmo.project.console;

import ru.ifmo.project.model.Product;
import ru.ifmo.project.model.Role;
import ru.ifmo.project.model.User;
import ru.ifmo.project.service.ProductService;
import ru.ifmo.project.service.UserService;
import ru.ifmo.project.util.PasswordUtil;

import java.util.Scanner;

public class AdminMenu {
    private final Scanner scanner = new Scanner(System.in);
    private final UserService userService = new UserService();
    private final ProductService productService = new ProductService();

    public void show() {
        while (true) {
            System.out.println("\n=== АДМИНИСТРИРОВАНИЕ ===");
            System.out.println("1. Управление пользователями");
            System.out.println("2. Управление товарами");
            System.out.println("0. Назад");
            System.out.print("Выбор: ");
            int choice = readInt();
            switch (choice) {
                case 1:
                    userManagement();
                    break;
                case 2:
                    productManagement();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Неверный выбор");
            }
        }
    }

    private void userManagement() {
        while (true) {
            System.out.println("\n--- ПОЛЬЗОВАТЕЛИ ---");
            System.out.println("1. Список всех пользователей");
            System.out.println("2. Создать пользователя");
            System.out.println("3. Обновить пользователя");
            System.out.println("4. Удалить пользователя");
            System.out.println("0. Назад");
            System.out.print("Выбор: ");
            int choice = readInt();
            switch (choice) {
                case 1:
                    listUsers();
                    break;
                case 2:
                    createUser();
                    break;
                case 3:
                    updateUser();
                    break;
                case 4:
                    deleteUser();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Неверный выбор");
            }
        }
    }

    private void listUsers() {
        var users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("Нет пользователей.");
        } else {
            System.out.println("\nСПИСОК ПОЛЬЗОВАТЕЛЕЙ:");
            for (User u : users) {
                System.out.printf("%d. %s (%s) - %s\n", u.getId(), u.getUsername(), u.getRole(), u.getFullName());
            }
        }
        System.out.println("\nНажмите Enter...");
        scanner.nextLine();
    }

    private void createUser() {
        System.out.print("Логин: ");
        String login = scanner.nextLine();
        System.out.print("Пароль: ");
        String password = scanner.nextLine();
        System.out.print("Полное имя: ");
        String fullName = scanner.nextLine();
        System.out.print("Роль (USER/ADMIN): ");
        String roleStr = scanner.nextLine();
        Role role;
        try {
            role = Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Неверная роль, установлена USER");
            role = Role.USER;
        }
        try {
            userService.createUser(login, password, role, fullName);
            System.out.println("Пользователь создан.");
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void updateUser() {
        System.out.print("ID пользователя для обновления: ");
        Long id = readLong();
        User user = userService.findById(id);
        if (user == null) {
            System.out.println("Пользователь не найден.");
            return;
        }
        System.out.println("Текущие данные: " + user);
        System.out.print("Новый логин (Enter - оставить): ");
        String login = scanner.nextLine();
        if (!login.isBlank()) user.setUsername(login);
        System.out.print("Новое полное имя (Enter - оставить): ");
        String fullName = scanner.nextLine();
        if (!fullName.isBlank()) user.setFullName(fullName);
        System.out.print("Новая роль (USER/ADMIN, Enter - оставить): ");
        String roleStr = scanner.nextLine();
        if (!roleStr.isBlank()) {
            try {
                user.setRole(Role.valueOf(roleStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                System.out.println("Неверная роль, оставлена прежняя.");
            }
        }
        System.out.print("Изменить пароль? (y/n): ");
        if (scanner.nextLine().equalsIgnoreCase("y")) {
            System.out.print("Новый пароль: ");
            String newPass = scanner.nextLine();
            user.setPasswordHash(PasswordUtil.hashPassword(newPass));
        }
        userService.updateUser(user);
        System.out.println("Пользователь обновлён.");
    }

    private void deleteUser() {
        System.out.print("ID пользователя для удаления: ");
        Long id = readLong();
        userService.deleteUser(id);
        System.out.println("Пользователь удалён.");
    }

    private void productManagement() {
        while (true) {
            System.out.println("\n--- ТОВАРЫ ---");
            System.out.println("1. Список всех товаров");
            System.out.println("2. Создать товар");
            System.out.println("3. Обновить товар");
            System.out.println("4. Удалить товар");
            System.out.println("0. Назад");
            System.out.print("Выбор: ");
            int choice = readInt();
            switch (choice) {
                case 1:
                    listProducts();
                    break;
                case 2:
                    createProduct();
                    break;
                case 3:
                    updateProduct();
                    break;
                case 4:
                    deleteProduct();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Неверный выбор");
            }
        }
    }

    private void listProducts() {
        var products = productService.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("Товаров нет.");
        } else {
            System.out.println("\nСПИСОК ТОВАРОВ:");
            for (Product p : products) {
                System.out.printf("%d. %s - %.2f руб. (в наличии: %d)\n", p.getId(), p.getName(), p.getPrice(), p.getStockQuantity());
                System.out.println("   Описание: " + p.getDescription());
            }
        }
        System.out.println("\nНажмите Enter...");
        scanner.nextLine();
    }

    private void createProduct() {
        System.out.print("Название: ");
        String name = scanner.nextLine();
        System.out.print("Цена: ");
        double price = readDouble();
        System.out.print("Описание: ");
        String desc = scanner.nextLine();
        System.out.print("Количество на складе: ");
        int stock = readInt();
        productService.createProduct(name, price, desc, stock);
        System.out.println("Товар создан.");
    }

    private void updateProduct() {
        System.out.print("ID товара для обновления: ");
        Long id = readLong();
        Product product = productService.findById(id);
        if (product == null) {
            System.out.println("Товар не найден.");
            return;
        }
        System.out.println("Текущие данные: " + product);
        System.out.print("Новое название (Enter - оставить): ");
        String name = scanner.nextLine();
        if (!name.isBlank()) product.setName(name);
        System.out.print("Новая цена (0 - оставить): ");
        double price = readDouble();
        if (price > 0) product.setPrice(price);
        System.out.print("Новое описание (Enter - оставить): ");
        String desc = scanner.nextLine();
        if (!desc.isBlank()) product.setDescription(desc);
        System.out.print("Новый остаток (-1 - оставить): ");
        int stock = readInt();
        if (stock >= 0) product.setStockQuantity(stock);
        productService.updateProduct(product);
        System.out.println("Товар обновлён.");
    }

    private void deleteProduct() {
        System.out.print("ID товара для удаления: ");
        Long id = readLong();
        productService.deleteProduct(id);
        System.out.println("Товар удалён.");
    }

    private int readInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Введите целое число: ");
            }
        }
    }

    private long readLong() {
        while (true) {
            try {
                return Long.parseLong(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Введите число: ");
            }
        }
    }

    private double readDouble() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Введите число: ");
            }
        }
    }
}