package ru.ifmo.project.dao.impl;

import ru.ifmo.project.dao.ProductDao;
import ru.ifmo.project.exception.DataAccessException;
import ru.ifmo.project.model.Product;
import ru.ifmo.project.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDaoImpl implements ProductDao {

    private final Connection connection;

    public ProductDaoImpl() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Product save(Product product) {
        String sql = "INSERT INTO products (name, price, description, stock_quantity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getName());
            stmt.setDouble(2, product.getPrice());
            stmt.setString(3, product.getDescription());
            stmt.setInt(4, product.getStockQuantity());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    product.setId(rs.getLong(1));
                }
            }
            return product;
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка сохранения товара", e);
        }
    }

    @Override
    public Optional<Product> findById(Long id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToProduct(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка поиска товара по id", e);
        }
    }

    @Override
    public List<Product> findAll() {
        String sql = "SELECT * FROM products";
        List<Product> products = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
            return products;
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка получения всех товаров", e);
        }
    }

    @Override
    public Product update(Product product) {
        String sql = "UPDATE products SET name = ?, price = ?, description = ?, stock_quantity = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setDouble(2, product.getPrice());
            stmt.setString(3, product.getDescription());
            stmt.setInt(4, product.getStockQuantity());
            stmt.setLong(5, product.getId());
            stmt.executeUpdate();
            return product;
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка обновления товара", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка удаления товара", e);
        }
    }

    @Override
    public List<Product> findByName(String name) {
        return findByName(name, false);
    }

    @Override
    public List<Product> findByName(String name, boolean exactMatch) {
        String sql;
        if (exactMatch) {
            sql = "SELECT * FROM products WHERE name = ?";
        } else {
            sql = "SELECT * FROM products WHERE name LIKE ?";
            name = "%" + name + "%";
        }
        List<Product> products = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
            return products;
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка поиска товаров по имени", e);
        }
    }

    @Override
    public List<Product> findByPriceRange(double min, double max) {
        String sql = "SELECT * FROM products WHERE price BETWEEN ? AND ?";
        List<Product> products = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, min);
            stmt.setDouble(2, max);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
            return products;
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка поиска по диапазону цен", e);
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        double price = rs.getDouble("price");
        String description = rs.getString("description");
        int stock = rs.getInt("stock_quantity");
        return new Product(id, name, price, description, stock);
    }
}