package ru.ifmo.project.service;

import ru.ifmo.project.exception.AuthenticationException;
import ru.ifmo.project.model.User;
import ru.ifmo.project.util.PasswordUtil;

public class AuthService {
    private static AuthService instance;
    private final UserService userService;
    private Session currentSession;

    private AuthService() {
        userService = new UserService();
    }

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    // Вложенный класс для хранения информации о клиенте
    public static class Session {
        private final User user;
        private final long loginTime;

        public Session(User user) {
            this.user = user;
            this.loginTime = System.currentTimeMillis();
        }

        public User getUser() {
            return user;
        }

        public long getLoginTime() {
            return loginTime;
        }
    }

    public void login(String username, String password) throws AuthenticationException {
        User user = userService.findByUsername(username);
        if (user == null || !PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            throw new AuthenticationException("Неверное имя пользователя или пароль");
        }
        currentSession = new Session(user);
        LoggingService.getInstance().log("Пользователь " + username + " вошёл в систему");
    }

    public void logout() {
        if (currentSession != null) {
            LoggingService.getInstance().log("Пользователь " + currentSession.getUser().getUsername() + " вышел");
            currentSession = null;
        }
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public boolean isAuthenticated() {
        return currentSession != null;
    }
}