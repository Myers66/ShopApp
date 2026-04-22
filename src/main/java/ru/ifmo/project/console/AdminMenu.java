package ru.ifmo.project.console;

import java.util.Scanner;

public class AdminMenu {
    private final Scanner scanner = new Scanner(System.in);

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
        System.out.println("Управление пользователями (в разработке)");
        // Заглушка
    }

    private void productManagement() {
        System.out.println("Управление товарами (в разработке)");
        // Заглушка
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