package ru.ifmo.project.dao;

import ru.ifmo.project.model.User;

public interface UserDao extends GenericDao<User> {
    User findByUsername(String username);
}