package org.example;

class ShoppingItem {
    private String productName;
    private int quantity;

    public ShoppingItem(String productName, int quantity) {
        this.productName = productName;
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }
}