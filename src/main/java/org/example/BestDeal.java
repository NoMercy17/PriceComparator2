package org.example;

class BestDeal {
    private String store;
    private int regularPrice;
    private int finalPrice;
    private boolean discounted;
    private String productName; // Added field to store actual product name

    public BestDeal(String store, int regularPrice, int finalPrice, boolean discounted, String productName) {
        this.store = store;
        this.regularPrice = regularPrice;
        this.finalPrice = finalPrice;
        this.discounted = discounted;
        this.productName = productName;
    }

    public String getStore() {
        return store;
    }

    public int getRegularPrice() {
        return regularPrice;
    }

    public int getFinalPrice() {
        return finalPrice;
    }

    public boolean isDiscounted() {
        return discounted;
    }

    public String getProductName() {
        return productName;
    }
}