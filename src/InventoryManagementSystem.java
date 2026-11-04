import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Scanner;

/**
 * Inventory Management System
 * JDBC with MySQL — Menu-Driven Application
 * Course Outcome: CO4
 */
public class InventoryManagementSystem {

    // Database Connection Parameters
    // Configurable via Environment Variables or directly editable below
    private static final String DB_URL = System.getenv("DB_URL") != null 
            ? System.getenv("DB_URL") 
            : "jdbc:mysql://localhost:3306/retail_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = System.getenv("DB_USER") != null 
            ? System.getenv("DB_USER") 
            : "root";
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD") != null 
            ? System.getenv("DB_PASSWORD") 
            : "Mayank@2007";

    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Establishes and returns a database connection.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println("  RETAIL STORE INVENTORY MANAGEMENT SYSTEM (JDBC)    ");
        System.out.println("======================================================");

        // Test connection on startup
        try (Connection conn = getConnection()) {
            System.out.println("[SUCCESS] Connected to MySQL database 'retail_db' successfully.\n");
        } catch (SQLException e) {
            System.err.println("[WARNING] Could not connect to MySQL: " + e.getMessage());
            System.err.println("Please ensure MySQL service is running and 'retail_db' is created.");
            System.err.println("Default credentials used: " + DB_USER + " / " + DB_PASSWORD + "\n");
        }

        while (true) {
            printMenu();
            System.out.print("Enter your choice (1-7): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    handleInsertProducts();
                    break;
                case "2":
                    handleConditionalRetrievalAndSorting();
                    break;
                case "3":
                    handleUpdateProductStock();
                    break;
                case "4":
                    handleSearchAndModifyProductDetails();
                    break;
                case "5":
                    handleAggregateAndConditionalDeletion();
                    break;
                case "6":
                    displayAllProducts();
                    break;
                case "7":
                    System.out.println("\nExiting Inventory Management System. Goodbye!");
                    return;
                default:
                    System.out.println("\n[ERROR] Invalid option. Please select a valid choice between 1 and 7.\n");
            }
        }
    }

    private static void printMenu() {
        System.out.println("------------------------- MENU -------------------------");
        System.out.println("1. (a) Insert Product Records (Batch / Single with Duplicate Check)");
        System.out.println("2. (b) Conditional Retrieval and Sorting (category & qty < 20)");
        System.out.println("3. (c) Update Product Stock (Add Quantity)");
        System.out.println("4. (d) Search & Modify Product Price (Validate Price > 0)");
        System.out.println("5. (e) Aggregate Query (Inventory Value) & Conditional Deletion (qty = 0)");
        System.out.println("6. Display All Products");
        System.out.println("7. Exit");
        System.out.println("--------------------------------------------------------");
    }

    // =========================================================================
    // (a) Insert Product Records — 2 Marks
    // Establish connection and insert records using PreparedStatement.
    // Handle duplicate product IDs appropriately.
    // =========================================================================
    private static void handleInsertProducts() {
        System.out.println("\n--- (a) Insert Product Records ---");
        System.out.println("Choose an insertion mode:");
        System.out.println("  1. Insert at least 3 sample products automatically");
        System.out.println("  2. Insert a new product manually");
        System.out.print("Enter choice (1/2): ");
        String subChoice = scanner.nextLine().trim();

        if ("1".equals(subChoice)) {
            insertSampleRecords();
        } else if ("2".equals(subChoice)) {
            insertSingleProductManually();
        } else {
            System.out.println("[ERROR] Invalid choice. Returning to main menu.\n");
        }
    }

    /**
     * Inserts at least three predefined sample product records using PreparedStatement,
     * demonstrating duplicate key handling.
     */
    private static void insertSampleRecords() {
        Object[][] sampleData = {
            {101, "Wireless Mouse", "Electronics", 15, 799.00},
            {102, "Mechanical Keyboard", "Electronics", 8, 2499.50},
            {103, "USB-C Fast Cable", "Electronics", 25, 299.00},
            {104, "A5 Spiral Notebook", "Stationery", 12, 85.00},
            {105, "Desk Lamp", "Home Decor", 0, 899.00}
        };

        String sql = "INSERT INTO Product (product_id, product_name, category, quantity, price) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int successCount = 0;
            for (Object[] row : sampleData) {
                int id = (int) row[0];
                String name = (String) row[1];
                String category = (String) row[2];
                int qty = (int) row[3];
                double price = (double) row[4];

                pstmt.setInt(1, id);
                pstmt.setString(2, name);
                pstmt.setString(3, category);
                pstmt.setInt(4, qty);
                pstmt.setDouble(5, price);

                try {
                    pstmt.executeUpdate();
                    System.out.printf("[INSERTED] ID: %d | Name: %-20s | Cat: %-12s | Qty: %-3d | Price: %.2f%n",
                            id, name, category, qty, price);
                    successCount++;
                } catch (SQLIntegrityConstraintViolationException ex) {
                    System.out.printf("[DUPLICATE SKIPPED] Product ID %d already exists. Duplicate ID handled gracefully.%n", id);
                }
            }
            System.out.printf("[SUMMARY] Successfully inserted %d sample records.\n\n", successCount);

        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] " + e.getMessage() + "\n");
        }
    }

    /**
     * Accepts a single product record from the user and inserts it using PreparedStatement.
     */
    private static void insertSingleProductManually() {
        try {
            System.out.print("Enter Product ID (Integer): ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Product Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter Category: ");
            String category = scanner.nextLine().trim();

            System.out.print("Enter Quantity: ");
            int quantity = Integer.parseInt(scanner.nextLine().trim());
            if (quantity < 0) {
                System.out.println("[ERROR] Quantity cannot be negative.\n");
                return;
            }

            System.out.print("Enter Price: ");
            double price = Double.parseDouble(scanner.nextLine().trim());
            if (price < 0) {
                System.out.println("[ERROR] Price cannot be negative.\n");
                return;
            }

            String sql = "INSERT INTO Product (product_id, product_name, category, quantity, price) VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, id);
                pstmt.setString(2, name);
                pstmt.setString(3, category);
                pstmt.setInt(4, quantity);
                pstmt.setDouble(5, price);

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("[SUCCESS] Product record inserted successfully.\n");
                }
            } catch (SQLIntegrityConstraintViolationException e) {
                System.out.println("[ERROR] Duplicate Entry: A product with ID " + id + " already exists in retail_db.\n");
            } catch (SQLException e) {
                System.err.println("[DATABASE ERROR] " + e.getMessage() + "\n");
            }

        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid numeric input entered.\n");
        }
    }

    // =========================================================================
    // (b) Conditional Retrieval and Sorting — 2 Marks
    // Accept category from user and display products where quantity < 20,
    // sorted by price in descending order.
    // =========================================================================
    private static void handleConditionalRetrievalAndSorting() {
        System.out.println("\n--- (b) Conditional Retrieval & Sorting ---");
        System.out.print("Enter Category to search (e.g., Electronics, Stationery): ");
        String category = scanner.nextLine().trim();

        if (category.isEmpty()) {
            System.out.println("[ERROR] Category cannot be blank.\n");
            return;
        }

        String sql = "SELECT product_id, product_name, category, quantity, price " +
                     "FROM Product " +
                     "WHERE category = ? AND quantity < 20 " +
                     "ORDER BY price DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category);

            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\nProducts in category '" + category + "' with Quantity < 20 (Sorted by Price DESC):");
                printTableHeader();

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    printRow(rs.getInt("product_id"),
                             rs.getString("product_name"),
                             rs.getString("category"),
                             rs.getInt("quantity"),
                             rs.getDouble("price"));
                }
                printTableFooter();

                if (!found) {
                    System.out.println("No products found matching category '" + category + "' with quantity < 20.\n");
                } else {
                    System.out.println();
                }
            }

        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] " + e.getMessage() + "\n");
        }
    }

    // =========================================================================
    // (c) Update Product Stock — 2 Marks
    // Accept a product ID and quantity to be added to the existing stock.
    // Update quantity and display message if product does not exist.
    // =========================================================================
    private static void handleUpdateProductStock() {
        System.out.println("\n--- (c) Update Product Stock ---");
        try {
            System.out.print("Enter Product ID to update stock: ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Quantity to add to existing stock: ");
            int additionalQty = Integer.parseInt(scanner.nextLine().trim());

            if (additionalQty <= 0) {
                System.out.println("[ERROR] Quantity to add must be greater than zero.\n");
                return;
            }

            // Update existing stock: quantity = quantity + ?
            String sql = "UPDATE Product SET quantity = quantity + ? WHERE product_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, additionalQty);
                pstmt.setInt(2, id);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.printf("[SUCCESS] Added %d units to Product ID %d.%n", additionalQty, id);
                    displayProductById(id);
                } else {
                    System.out.printf("[NOT FOUND] Product with ID %d does not exist. Stock could not be updated.%n%n", id);
                }
            } catch (SQLException e) {
                System.err.println("[DATABASE ERROR] " + e.getMessage() + "\n");
            }

        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid numeric input entered.\n");
        }
    }

    // =========================================================================
    // (d) Search and Modify Product Details — 2 Marks
    // Accept product ID and new price. Retrieve details and update price
    // only if the product exists and the new price is greater than zero.
    // Display updated product details.
    // =========================================================================
    private static void handleSearchAndModifyProductDetails() {
        System.out.println("\n--- (d) Search & Modify Product Price ---");
        try {
            System.out.print("Enter Product ID to modify price: ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter New Price: ");
            double newPrice = Double.parseDouble(scanner.nextLine().trim());

            // Condition: new price must be greater than zero
            if (newPrice <= 0) {
                System.out.println("[ERROR] Price modification rejected: New price must be strictly greater than 0.\n");
                return;
            }

            try (Connection conn = getConnection()) {
                // Step 1: Check if product exists & fetch existing details
                String selectSql = "SELECT product_id, product_name, category, quantity, price FROM Product WHERE product_id = ?";
                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setInt(1, id);

                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (!rs.next()) {
                            System.out.printf("[NOT FOUND] Product ID %d does not exist in the database.%n%n", id);
                            return;
                        }

                        // Product exists, show old details
                        System.out.println("\n[EXISTING DETAILS FOUND]");
                        printTableHeader();
                        printRow(rs.getInt("product_id"),
                                 rs.getString("product_name"),
                                 rs.getString("category"),
                                 rs.getInt("quantity"),
                                 rs.getDouble("price"));
                        printTableFooter();
                    }
                }

                // Step 2: Update the product's price
                String updateSql = "UPDATE Product SET price = ? WHERE product_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, newPrice);
                    updateStmt.setInt(2, id);

                    int rows = updateStmt.executeUpdate();
                    if (rows > 0) {
                        System.out.println("[SUCCESS] Price updated successfully. New Details:");
                        displayProductById(id);
                    }
                }

            } catch (SQLException e) {
                System.err.println("[DATABASE ERROR] " + e.getMessage() + "\n");
            }

        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid numeric input entered.\n");
        }
    }

    // =========================================================================
    // (e) Aggregate Query and Conditional Deletion — 2 Marks
    // Display total inventory value using SUM(quantity * price).
    // Accept product ID and delete only if quantity == 0.
    // Display appropriate message if it cannot be deleted.
    // =========================================================================
    private static void handleAggregateAndConditionalDeletion() {
        System.out.println("\n--- (e) Aggregate Query & Conditional Deletion ---");

        try (Connection conn = getConnection()) {
            // Part 1: Display Total Inventory Value using SUM(quantity * price)
            String sumSql = "SELECT COALESCE(SUM(quantity * price), 0.0) AS total_value, " +
                            "COUNT(*) AS total_items, " +
                            "COALESCE(SUM(quantity), 0) AS total_qty FROM Product";

            try (PreparedStatement sumStmt = conn.prepareStatement(sumSql);
                 ResultSet rs = sumStmt.executeQuery()) {

                if (rs.next()) {
                    double totalValue = rs.getDouble("total_value");
                    int totalItems = rs.getInt("total_items");
                    int totalQty = rs.getInt("total_qty");

                    System.out.println("+----------------------------------------------------------------+");
                    System.out.printf("| Total Unique Products : %-38d |%n", totalItems);
                    System.out.printf("| Total Quantity Count   : %-38d |%n", totalQty);
                    System.out.printf("| TOTAL INVENTORY VALUE  : Rs. %-34.2f |%n", totalValue);
                    System.out.println("+----------------------------------------------------------------+");
                }
            }

            // Part 2: Conditional Deletion (delete only if quantity is 0)
            System.out.print("\nEnter Product ID to delete (only allowed if quantity is 0): ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            // First check the current product status
            String checkSql = "SELECT product_name, quantity FROM Product WHERE product_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, id);

                try (ResultSet checkRs = checkStmt.executeQuery()) {
                    if (!checkRs.next()) {
                        System.out.printf("[CANNOT DELETE] Product ID %d does not exist in the database.%n%n", id);
                        return;
                    }

                    String pName = checkRs.getString("product_name");
                    int currentQty = checkRs.getInt("quantity");

                    if (currentQty != 0) {
                        System.out.printf("[CANNOT DELETE] Product '%s' (ID: %d) has %d units in stock. " +
                                          "Only products with quantity = 0 can be deleted.%n%n",
                                          pName, id, currentQty);
                        return;
                    }

                    // Quantity is 0, proceed with deletion
                    String deleteSql = "DELETE FROM Product WHERE product_id = ? AND quantity = 0";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                        deleteStmt.setInt(1, id);
                        int rowsDeleted = deleteStmt.executeUpdate();

                        if (rowsDeleted > 0) {
                            System.out.printf("[SUCCESS] Product '%s' (ID: %d) with 0 quantity was deleted successfully.%n%n",
                                    pName, id);
                        } else {
                            System.out.printf("[ERROR] Failed to delete product ID %d.%n%n", id);
                        }
                    }
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid product ID entered.\n");
        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] " + e.getMessage() + "\n");
        }
    }

    // =========================================================================
    // Helper / Utility Methods
    // =========================================================================

    private static void displayAllProducts() {
        System.out.println("\n--- Current Inventory (Product Table) ---");
        String sql = "SELECT product_id, product_name, category, quantity, price FROM Product ORDER BY product_id ASC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            printTableHeader();
            boolean any = false;
            while (rs.next()) {
                any = true;
                printRow(rs.getInt("product_id"),
                         rs.getString("product_name"),
                         rs.getString("category"),
                         rs.getInt("quantity"),
                         rs.getDouble("price"));
            }
            printTableFooter();

            if (!any) {
                System.out.println("The Product table is currently empty.\n");
            } else {
                System.out.println();
            }

        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] " + e.getMessage() + "\n");
        }
    }

    private static void displayProductById(int id) {
        String sql = "SELECT product_id, product_name, category, quantity, price FROM Product WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    printTableHeader();
                    printRow(rs.getInt("product_id"),
                             rs.getString("product_name"),
                             rs.getString("category"),
                             rs.getInt("quantity"),
                             rs.getDouble("price"));
                    printTableFooter();
                }
            }
        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] " + e.getMessage());
        }
        System.out.println();
    }

    private static void printTableHeader() {
        System.out.println("+------------+--------------------------------+-----------------+----------+------------+");
        System.out.println("| Product ID | Product Name                   | Category        | Quantity | Price (Rs) |");
        System.out.println("+------------+--------------------------------+-----------------+----------+------------+");
    }

    private static void printRow(int id, String name, String category, int qty, double price) {
        System.out.printf("| %-10d | %-30s | %-15s | %-8d | %10.2f |%n",
                id,
                name.length() > 30 ? name.substring(0, 27) + "..." : name,
                category.length() > 15 ? category.substring(0, 12) + "..." : category,
                qty,
                price);
    }

    private static void printTableFooter() {
        System.out.println("+------------+--------------------------------+-----------------+----------+------------+");
    }
}
