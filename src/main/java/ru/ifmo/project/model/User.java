package ru.ifmo.project.model;

import java.time.LocalDateTime;

public class User extends BaseEntity {
    private String username;
    private String passwordHash;
    private Role role;
    private String fullName;
    private LocalDateTime createdAt;

    public User() {
    }

    // Конструктор без id (для нового пользователя)
    public User(String username, String passwordHash, Role role, String fullName) {
        this(null, username, passwordHash, role, fullName, LocalDateTime.now());
    }

    // Полный конструктор (для загрузки из БД)
    public User(Long id, String username, String passwordHash, Role role, String fullName, LocalDateTime createdAt) {
        super(id);
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
        this.createdAt = createdAt;
    }

    // Геттеры и сеттеры
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", fullName='" + fullName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}