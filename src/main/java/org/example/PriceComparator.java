package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PriceComparator {
    private static final String PRICES_DIR = "prices";
    private static final String DISCOUNTS_DIR = "discounts";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Map<String, Map<String, Price>> pricesByStoreAndProduct = new HashMap<>();
    private Map<String, List<Discount>> discountsByStore = new HashMap<>();
    private LocalDate currentDate;

    public static void main(String[] args) {
        PriceComparator comparator = new PriceComparator();

        comparator.setCurrentDate(LocalDate.parse("2025-05-08", DATE_FORMATTER));

        // Load all price and discount data
        comparator.loadAllData();

        List<ShoppingItem> shoppingList = Arrays.asList(
                new ShoppingItem("lapte", 2),
                new ShoppingItem("pâine", 1),
                new ShoppingItem("vin alb demisec", 1),
                new ShoppingItem("paste", 2),
                new ShoppingItem("brânză", 1)
        );

        ShoppingPlan plan = comparator.generateOptimizedShoppingPlan(shoppingList);

        System.out.println("=== OPTIMIZED SHOPPING PLAN ===");
        System.out.println("Date: " + comparator.currentDate);
        System.out.println("\nSHOPPING LISTS BY STORE:");
        plan.getStoreItems().forEach((store, items) -> {
            System.out.println("\n" + store.toUpperCase() + ":");

            double storeTotal = 0.0;
            for (ShoppingPlanItem item : items) {
                String priceInfo = item.getDiscounted() ?
                        String.format("%.2f (DISCOUNTED from %.2f)",
                                item.getFinalPrice() / 100.0, item.getOriginalPrice() / 100.0) :
                        String.format("%.2f", item.getFinalPrice() / 100.0);

                System.out.printf("- %s x%d: %s RON%n",
                        item.getProductName(),
                        item.getQuantity(),
                        priceInfo);

                storeTotal += item.getFinalPrice() * item.getQuantity();
            }
            System.out.printf("STORE SUBTOTAL: %.2f RON%n", storeTotal / 100.0);
        });

        System.out.println("\nTOTAL COST: " + String.format("%.2f", plan.getTotalCost() / 100.0) + " RON");
        System.out.println("TOTAL SAVINGS: " + String.format("%.2f", plan.getTotalSavings() / 100.0) + " RON");

        // Display price comparison
        System.out.println("\n=== PRICE COMPARISON - LAPTE ===");
        comparator.comparePrices("lapte").forEach((store, prices) -> {
            prices.forEach(price -> {
                System.out.printf("%s - %s %.0f%s: %.2f RON%n",
                        store.toUpperCase(),
                        price.getProductName(),
                        price.getPackageQuantity(),
                        price.getPackageUnit(),
                        price.getPrice() / 100.0);
            });
        });
    }

    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    /**
     * Loads the price and discount from CSV files
     */
    public void loadAllData() {
        pricesByStoreAndProduct.clear();
        discountsByStore.clear();

        List<String> priceFiles = listFilesInDirectory(PRICES_DIR);
        for (String file : priceFiles) {
            String store = extractStoreFromFilename(file);
            List<Price> prices = loadPricesFromCsv(PRICES_DIR + "/" + file);

            Map<String, Price> productPrices = pricesByStoreAndProduct
                    .computeIfAbsent(store, k -> new HashMap<>());

            for (Price price : prices) {
                // Store both by ID and partial name match for better lookup
                productPrices.put(price.getProductId().toLowerCase(), price);
                productPrices.put(price.getProductName().toLowerCase(), price);
            }
        }

        List<String> discountFiles = listFilesInDirectory(DISCOUNTS_DIR);
        for (String file : discountFiles) {
            String store = extractStoreFromFilename(file);
            List<Discount> discounts = loadDiscountsFromCsv(DISCOUNTS_DIR + "/" + file);
            discountsByStore.put(store, discounts);
        }
    }


    public ShoppingPlan generateOptimizedShoppingPlan(List<ShoppingItem> shoppingList) {
        ShoppingPlan plan = new ShoppingPlan();

        for (ShoppingItem item : shoppingList) {
            BestDeal bestDeal = findBestDealForProduct(item.getProductName(), item.getQuantity());

            if (bestDeal != null) {
                plan.addItem(bestDeal.getStore(), new ShoppingPlanItem(
                        bestDeal.getProductName(), // Use actual product name from best deal
                        item.getQuantity(),
                        bestDeal.getRegularPrice(),
                        bestDeal.getFinalPrice(),
                        bestDeal.isDiscounted()
                ));
            }
        }

        return plan;
    }


    public BestDeal findBestDealForProduct(String productName, int quantity) {
        String normalizedProductName = productName.toLowerCase();
        int lowestPrice = Integer.MAX_VALUE;
        String bestStore = null;
        int regularPrice = 0;
        boolean isDiscounted = false;
        String actualProductName = productName;

        for (String store : pricesByStoreAndProduct.keySet()) {
            Map<String, Price> storePrices = pricesByStoreAndProduct.get(store);

            // Try to find product by exact name
            Price price = null;
            if (storePrices.containsKey(normalizedProductName)) {
                price = storePrices.get(normalizedProductName);
            } else {
                // or by partial match
                for (String key : storePrices.keySet()) {
                    if (key.contains(normalizedProductName)) {
                        price = storePrices.get(key);
                        break;
                    }
                }
            }

            if (price == null) {
                continue;
            }

            int currentPrice = price.getPrice();
            boolean discounted = false;

            // Check if there's an active discount for this product
            if (discountsByStore.containsKey(store)) {
                for (Discount discount : discountsByStore.get(store)) {
                    // Match discount by product ID or name
                    if (discount.getProductId().equalsIgnoreCase(price.getProductId()) ||
                            discount.getProductName().toLowerCase().contains(normalizedProductName)) {

                        LocalDate fromDate = LocalDate.parse(discount.getFromDate(), DATE_FORMATTER);
                        LocalDate toDate = LocalDate.parse(discount.getToDate(), DATE_FORMATTER);

                        if ((currentDate.isEqual(fromDate) || currentDate.isAfter(fromDate)) &&
                                (currentDate.isEqual(toDate) || currentDate.isBefore(toDate))) {
                            // Apply discount using the percentage from the discount
                            double discountFactor = 1.0 - (discount.getDiscountPercentage() / 100.0);
                            currentPrice = (int)(currentPrice * discountFactor);
                            discounted = true;
                            break;
                        }
                    }
                }
            }

            if (currentPrice < lowestPrice) {
                lowestPrice = currentPrice;
                bestStore = store;
                regularPrice = price.getPrice();
                isDiscounted = discounted;
                actualProductName = price.getProductName();
            }
        }

        if (bestStore != null) {
            return new BestDeal(bestStore, regularPrice, lowestPrice, isDiscounted, actualProductName);
        }

        return null;
    }


    public Map<String, List<Price>> comparePrices(String productName) {
        Map<String, List<Price>> results = new HashMap<>();
        String normalizedName = productName.toLowerCase();

        pricesByStoreAndProduct.forEach((store, products) -> {
            List<Price> matches = products.values().stream()
                    .filter(p -> p.getProductName().toLowerCase().contains(normalizedName))
                    .distinct()
                    .collect(Collectors.toList());

            if (!matches.isEmpty()) {
                results.put(store, matches);
            }
        });

        return results;
    }


    public Set<String> getAllCategories() {
        Set<String> categories = new HashSet<>();

        for (Map<String, Price> storePrices : pricesByStoreAndProduct.values()) {
            for (Price price : storePrices.values()) {
                categories.add(price.getProductCategory());
            }
        }

        return categories;
    }


    /**
     * Utility methods for loading data
     */
    private List<String> listFilesInDirectory(String directory) {
        List<String> fileList = new ArrayList<>();
        try (Stream<Path> paths = Files.list(Paths.get(directory))) {
            fileList = paths
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Object::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error listing files in directory: " + directory);
            e.printStackTrace();
        }
        return fileList;
    }

    private String extractStoreFromFilename(String filename) {
        // Example: kaufland_2025-05-01.csv -> kaufland
        return filename.split("_")[0];
    }

    private List<Price> loadPricesFromCsv(String filePath) {
        List<Price> prices = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] data = line.split(";");
                if (data.length >= 8) {
                    try {
                        // Convert price from string (e.g., "10.10") to cents (1010)
                        float rawPrice = Float.parseFloat(data[6]);
                        int priceInCents = (int)(rawPrice * 100);

                        Price price = new Price(
                                data[0], // product_id
                                data[1], // product_name
                                data[2], // product_category
                                data[3], // brand
                                Float.parseFloat(data[4]), // package_quantity
                                data[5], // package_unit
                                priceInCents, // price (in cents)
                                data[7]  // currency
                        );
                        prices.add(price);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing numeric value in line: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading price file: " + filePath);
            e.printStackTrace();
        }

        return prices;
    }

    private List<Discount> loadDiscountsFromCsv(String filePath) {
        List<Discount> discounts = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] data = line.split(";");
                if (data.length >= 9) { // Updated to include discount percentage
                    try {
                        Discount discount = new Discount(
                                data[0], // product_id
                                data[1], // product_name
                                data[2], // brand
                                Float.parseFloat(data[3]), // package_quantity
                                data[4], // package_unit
                                data[5], // product_category
                                data[6], // from_date
                                data[7], // to_date
                                Float.parseFloat(data[8]) // percentage_of_discount
                        );
                        discounts.add(discount);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing numeric value in line: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading discount file: " + filePath);
            e.printStackTrace();
        }

        return discounts;
    }
}