-- Database setup script for Inventory Management System (retail_db)

CREATE DATABASE IF NOT EXISTS retail_db;
USE retail_db;

DROP TABLE IF EXISTS Product;

CREATE TABLE Product (
    product_id INT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL
);

-- Sample initial records (Optional)
INSERT INTO Product (product_id, product_name, category, quantity, price) VALUES
(101, 'Wireless Mouse', 'Electronics', 15, 799.00),
(102, 'Mechanical Keyboard', 'Electronics', 8, 2499.50),
(103, 'USB-C Cable', 'Electronics', 25, 299.00),
(104, 'Ballpoint Pen Set', 'Stationery', 50, 120.00),
(105, 'Notebook A5', 'Stationery', 12, 85.00),
(106, 'Desk Lamp', 'Home Decor', 0, 899.00);
