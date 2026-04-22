package ru.ifmo.project.console;

import ru.ifmo.project.exception.AuthenticationException;
import ru.ifmo.project.model.Role;
import ru.ifmo.project.model.User;
import ru.ifmo.project.service.AuthService;
import ru.ifmo.project.service.ProductService;
import ru.ifmo.project.service.UserService;

import java.util.Scanner;

public class Menu {
    private final Scanner scanner = new Scanner(System.in);
    private final AuthService authService = AuthService.getInstance();
    private final UserService userService = new UserService();
    private final ProductService productService = new ProductService();

    public void start() {
        if (userService.getAllUsers().isEmpty()) {
            userService.createUser("admin", "admin", Role.ADMIN, "Administrator");
            System.out.println("Создан администратор по умолчанию: admin / admin");
        }

        while (true) {
            if (!authService.isAuthenticated()) {
                showAuthMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private void showAuthMenu() {
        System.out.println("\n=== АВТОРИЗАЦИЯ ===");
        System.out.println("1. Вход");
        System.out.println("2. Регистрация");
        System.out.println("0. Выход");
        System.out.print("Выберите действие: ");
        int choice = readInt();
        switch (choice) {
            case 1:
                login();
                break;
            case 2:
                register();
                break;
            case 0:
                System.exit(0);
                break;
            default:
                System.out.println("Неверный выбор");
        }
    }

    private void login() {
        System.out.print("Логин: ");
        String login = scanner.nextLine();
        System.out.print("Пароль: ");
        String password = scanner.nextLine();
        try {
            authService.login(login, password);
            System.out.println("Добро пожаловать, " + login);
        } catch (AuthenticationException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void register() {
        System.out.print("Логин: ");
        String login = scanner.nextLine();
        System.out.print("Пароль: ");
        String password = scanner.nextLine();
        System.out.print("Полное имя: ");
        String fullName = scanner.nextLine();
        try {
            userService.createUser(login, password, Role.USER, fullName);
            System.out.println("Регистрация успешна! Теперь войдите.");
        } catch (Exception e) {
            System.out.println("Ошибка регистрации: " + e.getMessage());
        }
    }

    private void showMainMenu() {
        recursiveMenu(0);
    }

    private void recursiveMenu(int depth) {
        if (depth > 10) {
            System.out.println("Слишком глубокий уровень меню, возврат...");
            return;
        }
        System.out.println("\n=== ГЛАВНОЕ МЕНЮ ===");
        System.out.println("1. Просмотр товаров");
        if (authService.getCurrentSession().getUser().getRole() == Role.ADMIN) {
            System.out.println("2. Администрирование (CRUD)");
        }
        System.out.println("0. Выйти из аккаунта");
        System.out.print("Выберите действие: ");
        int choice = readInt();
        switch (choice) {
            case 1:
                showProducts();
                break;
            case 2:
                if (authService.getCurrentSession().getUser().getRole() == Role.ADMIN) {
                    // TODO: после создания AdminMenu раскомментировать
                    System.out.println("Админ-меню в разработке...");
                    // new AdminMenu().show();
                } else {
                    System.out.println("Нет прав");
                }
                break;
            case 0:
                authService.logout();
                System.out.println("Вы вышли из системы");
                return;
            default:
                System.out.println("Неверный выбор");
        }
        recursiveMenu(depth + 1);
    }

    private void showProducts() {
        var products = productService.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("Товаров пока нет.");
        } else {
            System.out.println("\n=== СПИСОК ТОВАРОВ ===");
            for (var p : products) {
                System.out.printf("%d. %s - %.2f руб. (остаток: %d)\n", p.getId(), p.getName(), p.getPrice(), p.getStockQuantity());
                System.out.println("   " + p.getDescription());
            }
        }
        System.out.println("\nНажмите Enter для продолжения...");
        scanner.nextLine();
    }

    private int readInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Введите число: ");
            }
        }
    }
}