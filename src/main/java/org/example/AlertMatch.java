package org.example;

class AlertMatch {
    private PriceAlert alert;
    private String store;
    private String actualProductName;
    private double currentPrice;
    private boolean hasDiscount;
    private float discountPercentage;

    public AlertMatch(PriceAlert alert, String store, String actualProductName,
                      double currentPrice, boolean hasDiscount, float discountPercentage) {
        this.alert = alert;
        this.store = store;
        this.actualProductName = actualProductName;
        this.currentPrice = currentPrice;
        this.hasDiscount = hasDiscount;
        this.discountPercentage = discountPercentage;
    }

    // Getters
    public PriceAlert getAlert() { return alert; }
    public String getStore() { return store; }
    public String getActualProductName() { return actualProductName; }
    public double getCurrentPrice() { return currentPrice; }
    public boolean hasDiscount() { return hasDiscount; }
    public float getDiscountPercentage() { return discountPercentage; }
}
