package ru.ifmo.project.dao;

import ru.ifmo.project.model.Product;

import java.util.List;

public interface ProductDao extends GenericDao<Product> {
    List<Product> findByPriceRange(double min, double max);
}