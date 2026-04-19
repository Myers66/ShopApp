package ru.ifmo.project.service;

import ru.ifmo.project.dao.UserDao;
import ru.ifmo.project.dao.impl.UserDaoImpl;
import ru.ifmo.project.exception.DataAccessException;
import ru.ifmo.project.model.Role;
import ru.ifmo.project.model.User;
import ru.ifmo.project.util.PasswordUtil;

import java.util.List;

public class UserService {
    private final UserDao userDao;

    public UserService() {
        this.userDao = new UserDaoImpl();
    }

    public User createUser(String username, String plainPassword, Role role, String fullName) {
        // Проверка, существует ли пользователь с таким именем
        if (userDao.findByUsername(username) != null) {
            throw new DataAccessException("Пользователь с именем '" + username + "' уже существует");
        }
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        User user = new User(username, hashedPassword, role, fullName);
        return userDao.save(user);
    }

    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    public User findById(Long id) {
        return userDao.findById(id).orElse(null);
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public User updateUser(User user) {
        return userDao.update(user);
    }

    public void deleteUser(Long id) {
        userDao.delete(id);
    }
}