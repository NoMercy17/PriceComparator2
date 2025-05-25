package org.example;

class PriceAlert {
    private String productName;
    private double targetPrice;
    private String alertId;
    private boolean isActive;

    public PriceAlert(String productName, double targetPrice) {
        this.productName = productName.toLowerCase();
        this.targetPrice = targetPrice;
        this.alertId = productName.toLowerCase() + "_" + targetPrice;
        this.isActive = true;
    }

    // Getters
    public String getProductName() { return productName; }
    public double getTargetPrice() { return targetPrice; }
    public String getAlertId() { return alertId; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
}
