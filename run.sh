#!/bin/bash
cd "$(dirname "$0")"

echo "Compiling InventoryManagementSystem.java..."
javac -cp ".:lib/*" -d out src/InventoryManagementSystem.java
if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo "Running Retail Store Inventory Management System..."
java -cp "out:lib/*" InventoryManagementSystem
