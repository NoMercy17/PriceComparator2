package org.example;

class Price {
    private String product_id;
    private String product_name;
    private String product_category;
    private String brand;
    private float package_quantity;
    private String package_unit;
    private int price; // in cents
    private String currency;

    public Price(String product_id, String product_name, String product_category,
                 String brand, float package_quantity, String package_unit,
                 int price, String currency) {
        this.product_id = product_id;
        this.product_name = product_name;
        this.product_category = product_category;
        this.brand = brand;
        this.package_quantity = package_quantity;
        this.package_unit = package_unit;
        this.price = price;
        this.currency = currency;
    }

    public String getProductId() {
        return product_id;
    }

    public String getProductName() {
        return product_name;
    }

    public String getProductCategory() {
        return product_category;
    }

    public String getBrand() {
        return brand;
    }

    public float getPackageQuantity() {
        return package_quantity;
    }

    public String getPackageUnit() {
        return package_unit;
    }

    public int getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public String toString() {
        return "src.main.java.src.main.java.org.priceComparator.priceComparator.Price{" +
                "product_id='" + product_id + '\'' +
                ", product_name='" + product_name + '\'' +
                ", product_category='" + product_category + '\'' +
                ", brand='" + brand + '\'' +
                ", package_quantity=" + package_quantity +
                ", package_unit='" + package_unit + '\'' +
                ", price=" + price +
                ", currency='" + currency + '\'' +
                '}';
    }
}