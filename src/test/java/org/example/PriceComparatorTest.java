package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PriceComparatorTest {

    private PriceComparator priceComparator;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @BeforeEach
    void setUp() {
        priceComparator = new PriceComparator();
        priceComparator.setCurrentDate(LocalDate.parse("2025-05-08", DATE_FORMATTER));
        priceComparator.loadAllData();
    }

    @Test
    void testComparePrices() {
        // Test comparing prices for "lapte" (milk)
        Map<String, List<Price>> milkPrices = priceComparator.comparePrices("lapte");

        assertFalse(milkPrices.isEmpty(), "Should find milk prices in at least one store");

        // If we have prices from a store, check they actually contain "lapte"
        milkPrices.values().forEach(prices -> {
            assertFalse(prices.isEmpty(), "Each store should have at least one matching product");

            prices.forEach(price ->
                    assertTrue(price.getProductName().toLowerCase().contains("lapte"),
                            "Product name should contain 'lapte': " + price.getProductName())
            );
        });
    }

    @Test
    void testGenerateOptimizedShoppingPlan() {
        List<ShoppingItem> shoppingList = Arrays.asList(
                new ShoppingItem("lapte", 2),
                new ShoppingItem("pâine", 1)
        );

        ShoppingPlan plan = priceComparator.generateOptimizedShoppingPlan(shoppingList);

        assertNotNull(plan, "Shopping plan should not be null");

        // Verify all items from the shopping list are in the plan
        int totalItemsInPlan = plan.getStoreItems().values().stream()
                .mapToInt(List::size)
                .sum();

        assertEquals(shoppingList.size(), totalItemsInPlan,
                "All items from shopping list should be in the plan");

        assertTrue(plan.getTotalCost() > 0, "Total cost should be greater than zero");
    }
}