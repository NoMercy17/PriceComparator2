package org.example;

class ShoppingPlanItem {
    private String productName;
    private int quantity;
    private int originalPrice;
    private int finalPrice;
    private boolean discounted;

    public ShoppingPlanItem(String productName, int quantity, int originalPrice, int finalPrice, boolean discounted) {
        this.productName = productName;
        this.quantity = quantity;
        this.originalPrice = originalPrice;
        this.finalPrice = finalPrice;
        this.discounted = discounted;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getOriginalPrice() {
        return originalPrice;
    }

    public int getFinalPrice() {
        return finalPrice;
    }

    public boolean getDiscounted() {
        return discounted;
    }
}