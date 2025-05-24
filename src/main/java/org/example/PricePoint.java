package org.example;

class PricePoint {
    private String date;
    private String productName;
    private String store;
    private String brand;
    private String category;
    private double regularPrice;
    private double finalPrice;
    private boolean hasDiscount;

    public PricePoint(String date, String productName, String store, String brand,
                      String category, double regularPrice, double finalPrice, boolean hasDiscount) {
        this.date = date;
        this.productName = productName;
        this.store = store;
        this.brand = brand;
        this.category = category;
        this.regularPrice = regularPrice;
        this.finalPrice = finalPrice;
        this.hasDiscount = hasDiscount;
    }
    public String getDate() {
        return date;
    }
    public String getProductName() {
        return productName;
    }
    public String getStore() {
        return store;
    }
    public String getBrand() {
        return brand;
    }
    public String getCategory() {
        return category;
    }
    public double getRegularPrice() {
        return regularPrice;
    }
    public double getFinalPrice() {
        return finalPrice;
    }
    public boolean hasDiscount() {
        return hasDiscount;
    }

}
