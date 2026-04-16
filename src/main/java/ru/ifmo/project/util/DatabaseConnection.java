package ru.ifmo.project.util;

import ru.ifmo.project.exception.DataAccessException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("Файл application.properties не найден");
            }
            Properties props = new Properties();
            props.load(input);
            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");
            connection = DriverManager.getConnection(url, user, password);
            initSchema();
        } catch (IOException | SQLException e) {
            throw new DataAccessException("Не удалось инициализировать подключение к БД", e);
        }
    }

    private void initSchema() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("schema.sql");
             java.util.Scanner scanner = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A")) {
            String sql = scanner.hasNext() ? scanner.next() : "";
            try (Statement stmt = connection.createStatement()) {
                for (String statement : sql.split(";")) {
                    if (!statement.trim().isEmpty()) {
                        stmt.execute(statement);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Ошибка инициализации схемы БД", e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                instance = new DatabaseConnection();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка проверки соединения", e);
        }
        return instance.connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}