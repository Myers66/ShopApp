package ru.ifmo.project;

import ru.ifmo.project.console.Menu;
import ru.ifmo.project.web.WebServer;

public class Main {
    public static void main(String[] args) {
        // запуск веб-сервера в отдельном потоке
        new Thread(() -> {
            try {
                WebServer webServer = new WebServer(8080);
                webServer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // запуск консольного меню
        Menu menu = new Menu();
        menu.start();
    }
}