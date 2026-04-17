package ru.ifmo.project.dao;

import java.util.List;
import java.util.Optional;

public interface GenericDao<T> {
    T save(T entity);
    Optional<T> findById(Long id);
    List<T> findAll();
    T update(T entity);
    void delete(Long id);

    // Перегрузка методов
    List<T> findByName(String name);
    List<T> findByName(String name, boolean exactMatch);
}