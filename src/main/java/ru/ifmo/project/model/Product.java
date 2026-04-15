package ru.ifmo.project.model;

public class Product extends BaseEntity {
    private String name;
    private double price;
    private String description;
    private int stockQuantity;

    public Product() {
    }

    public Product(String name, double price, String description, int stockQuantity) {
        this(null, name, price, description, stockQuantity);
    }

    public Product(Long id, String name, double price, String description, int stockQuantity) {
        super(id);
        this.name = name;
        this.price = price;
        this.description = description;
        this.stockQuantity = stockQuantity;
    }

    // Геттеры и сеттеры
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stock=" + stockQuantity +
                '}';
    }
}