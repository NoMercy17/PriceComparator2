package org.example;

class ValuePerUnitItem {
    private String productName;
    private String brand;
    private String store;
    private double price;
    private double finalPrice;
    private float packageQuantity;
    private String packageUnit;
    private double valuePerUnit;
    private double finalValuePerUnit;
    private boolean hasDiscount;
    private float discountPercentage;

    public ValuePerUnitItem(String productName, String brand, String store, double price,
                            double finalPrice, float packageQuantity, String packageUnit,
                            boolean hasDiscount, float discountPercentage) {
        this.productName = productName;
        this.brand = brand;
        this.store = store;
        this.price = price;
        this.finalPrice = finalPrice;
        this.packageQuantity = packageQuantity;
        this.packageUnit = packageUnit;
        this.hasDiscount = hasDiscount;
        this.discountPercentage = discountPercentage;

        // Calculate value per unit
        this.valuePerUnit = packageQuantity > 0 ? price / packageQuantity : 0;
        this.finalValuePerUnit = packageQuantity > 0 ? finalPrice / packageQuantity : 0;
    }

    // Getters
    public String getProductName() {
        return productName;
    }
    public String getBrand() {
        return brand;
    }
    public String getStore() {
        return store;
    }
    public double getPrice() {
        return price;
    }
    public double getFinalPrice() {
        return finalPrice;
    }
    public float getPackageQuantity() {
        return packageQuantity;
    }
    public String getPackageUnit() {
        return packageUnit;
    }
    public double getValuePerUnit() {
        return valuePerUnit;
    }
    public double getFinalValuePerUnit() {
        return finalValuePerUnit;
    }
    public boolean hasDiscount() {
        return hasDiscount;
    }
    public float getDiscountPercentage() {
        return discountPercentage;
    }

    public String getFullProductName() {
        return productName + " - " + brand + " " + packageQuantity + packageUnit;
    }
}
