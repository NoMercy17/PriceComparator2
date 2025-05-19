package org.example;


import java.util.*;

class ShoppingPlan {
    private Map<String, List<ShoppingPlanItem>> storeItems = new HashMap<>();

    public void addItem(String store, ShoppingPlanItem item) {
        List<ShoppingPlanItem> items = storeItems.computeIfAbsent(store, k -> new ArrayList<>());
        items.add(item);
    }

    public Map<String, List<ShoppingPlanItem>> getStoreItems() {
        return storeItems;
    }

    public double getTotalCost() {
        return storeItems.values().stream()
                .flatMap(Collection::stream)
                .mapToDouble(item -> item.getFinalPrice() * item.getQuantity())
                .sum();
    }

    public double getTotalSavings() {
        return storeItems.values().stream()
                .flatMap(Collection::stream)
                .mapToDouble(item -> (item.getOriginalPrice() - item.getFinalPrice()) * item.getQuantity())
                .sum();
    }
}