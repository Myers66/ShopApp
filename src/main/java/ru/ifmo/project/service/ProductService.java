package ru.ifmo.project.service;

import ru.ifmo.project.dao.ProductDao;
import ru.ifmo.project.dao.impl.ProductDaoImpl;
import ru.ifmo.project.model.Product;

import java.util.List;

public class ProductService {
    private final ProductDao productDao;

    public ProductService() {
        this.productDao = new ProductDaoImpl();
    }

    public Product createProduct(String name, double price, String description, int stockQuantity) {
        Product product = new Product(name, price, description, stockQuantity);
        return productDao.save(product);
    }

    public Product findById(Long id) {
        return productDao.findById(id).orElse(null);
    }

    public List<Product> getAllProducts() {
        return productDao.findAll();
    }

    public Product updateProduct(Product product) {
        return productDao.update(product);
    }

    public void deleteProduct(Long id) {
        productDao.delete(id);
    }

    public List<Product> findByName(String name) {
        return productDao.findByName(name);
    }

    public List<Product> findByPriceRange(double min, double max) {
        return productDao.findByPriceRange(min, max);
    }
}