package ru.ifmo.project.dao.impl;

import org.junit.jupiter.api.*;
import ru.ifmo.project.model.Role;
import ru.ifmo.project.model.User;
import ru.ifmo.project.util.PasswordUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserDaoImplTest {
    private UserDaoImpl userDao;
    private Connection connection;

    @BeforeAll
    void setUp() throws SQLException {
        // Используем H2 в памяти для тестов
        connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        // Создаём таблицу users вручную (можно выполнить schema.sql)
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE users (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password_hash VARCHAR(100) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL, " +
                    "full_name VARCHAR(100), " +
                    "created_at TIMESTAMP NOT NULL)");
        }
        userDao = new UserDaoImpl(connection); // потребуется добавить конструктор в UserDaoImpl, принимающий Connection
    }

    @AfterAll
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void testSaveAndFind() {
        User user = new User("testuser", PasswordUtil.hashPassword("pass"), Role.USER, "Test User");
        User saved = userDao.save(user);
        assertNotNull(saved.getId());
        var found = userDao.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void testFindByUsername() {
        User user = new User("uniqueuser", PasswordUtil.hashPassword("pass"), Role.ADMIN, "Admin User");
        userDao.save(user);
        User found = userDao.findByUsername("uniqueuser");
        assertNotNull(found);
        assertEquals("Admin User", found.getFullName());
    }
}