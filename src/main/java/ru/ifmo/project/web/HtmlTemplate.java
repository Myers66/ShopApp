package ru.ifmo.project.web;

public class HtmlTemplate {
    public static String wrap(String title, String bodyContent, boolean showNav) {
        String nav = "";
        if (showNav) {
            nav = "<div class='nav'><a href='/'>Главная</a> | <a href='/users'>Пользователи</a> | <a href='/products'>Товары</a> | <a href='/login?logout=1'>Выйти</a></div>";
        }
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" + title + "</title>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f0f2f5; margin: 0; padding: 20px; }" +
                ".container { max-width: 900px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }" +
                "h1, h2, h3 { color: #333; }" +
                "input, select, button { padding: 8px; margin: 5px 0; border: 1px solid #ccc; border-radius: 4px; width: 100%; max-width: 300px; }" +
                "input[type='submit'], button { background-color: #007bff; color: white; border: none; cursor: pointer; }" +
                "input[type='submit']:hover, button:hover { background-color: #0056b3; }" +
                "ul { list-style: none; padding: 0; }" +
                "li { background: #f9f9f9; margin: 8px 0; padding: 10px; border-radius: 4px; }" +
                "a { text-decoration: none; color: #007bff; margin-left: 10px; }" +
                "a:hover { text-decoration: underline; }" +
                ".error { color: red; }" +
                "</style></head><body><div class='container'>" + nav + bodyContent + "</div></body></html>";
    }
}