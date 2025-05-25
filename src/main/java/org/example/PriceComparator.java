package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class PriceComparator {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Map<String, Map<String, Price>> pricesByStoreAndProduct = new HashMap<>();
    private Map<String, List<Discount>> discountsByStore = new HashMap<>();
    private LocalDate currentDate;
    private Map<String, PriceAlert> priceAlerts = new HashMap<>();

    private static final List<String> PRICE_FILES = Arrays.asList(
            "prices/kaufland_2025-05-01.csv",
            "prices/kaufland_2025-05-08.csv",
            "prices/lidl_2025-05-01.csv",
            "prices/lidl_2025-05-08.csv",
            "prices/profi_2025-05-01.csv",
            "prices/profi_2025-05-08.csv"
    );

    private static final List<String> DISCOUNT_FILES = Arrays.asList(
            "discounts/kaufland_discounts_2025-05-01.csv",
            "discounts/kaufland_discounts_2025-05-08.csv",
            "discounts/lidl_discounts_2025-05-01.csv",
            "discounts/lidl_discounts_2025-05-08.csv",
            "discounts/profi_discounts_2025-05-01.csv",
            "discounts/profi_discounts_2025-05-08.csv"
    );

    public static void main(String[] args) {
        PriceComparator comparator = new PriceComparator();
        comparator.setCurrentDate(LocalDate.parse("2025-05-08", DATE_FORMATTER));

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

        System.out.println("=== 10 products with the biggest discount ===");
        comparator.listBestDiscounts();

        // print inside the function
        comparator.recentDiscounts();

        // Search by store
        System.out.println("\n1. Testing search by STORE:");
        comparator.printPriceHistory("kaufland");

        // Search by product_category
        System.out.println("\n2. Testing search by CATEGORY:");
        comparator.printPriceHistory("lactate");

        // Search by brand
        System.out.println("\n3. Testing search by BRAND:");
        comparator.printPriceHistory("zuzu");


        // Test: Find best value
        System.out.println("\n1. Best value per unit for MILK:");
        comparator.findBestValuePerUnit("lapte");

        System.out.println("\n2. Best value per unit for DAIRY PRODUCTS:");
        comparator.findBestValuePerUnit("lactate");

        System.out.println("\n3. Package size comparison for BREAD:");
        comparator.comparePackageSizes("pâine");

        System.out.println("\n4. Best value per unit for PASTA:");
        comparator.findBestValuePerUnit("paste");

        System.out.println("\n1. Setting up price alerts:");
        comparator.addPriceAlert("lapte", 4.00);        // Set alert for milk at 4.00 RON or below
        comparator.addPriceAlert("pâine", 2.50);        // Set alert for bread at 2.50 RON or below
        comparator.addPriceAlert("brânză", 8.00);

        // Show all active alerts
        comparator.showActiveAlerts();

        //Check if any alerts are triggered
        System.out.println("\n2. Checking for triggered alerts:");
        comparator.checkAndShowTriggeredAlerts();

        // Remove an alert
        System.out.println("\n5. Removing an alert:");
        comparator.removePriceAlert("lapte");
        comparator.showActiveAlerts();

    }
    // Price Alert Methods
    public void addPriceAlert(String productName, double targetPrice) {
        PriceAlert alert = new PriceAlert(productName, targetPrice);
        priceAlerts.put(alert.getAlertId(), alert);
        System.out.printf("Price alert added: %s at %.2f RON or below%n", productName, targetPrice);
    }

    public void removePriceAlert(String productName) {
        String searchKey = productName.toLowerCase();
        String alertToRemove = null;

        for (String alertId : priceAlerts.keySet()) {
            if (alertId.startsWith(searchKey + "_")) {
                alertToRemove = alertId;
                break;
            }
        }

        if (alertToRemove != null) {
            priceAlerts.remove(alertToRemove);
            System.out.printf("Price alert removed for: %s%n", productName);
        } else {
            System.out.printf("No price alert found for: %s%n", productName);
        }
    }
    public void showActiveAlerts() {
        System.out.println("\n=== ACTIVE PRICE ALERTS ===");
        if (priceAlerts.isEmpty()) {
            System.out.println("No active price alerts.");
            return;
        }

        for (PriceAlert alert : priceAlerts.values()) {
            if (alert.isActive()) {
                System.out.printf("- %s: %.2f RON or below%n",
                        alert.getProductName(), alert.getTargetPrice());
            }
        }
    }

    public void checkAndShowTriggeredAlerts() {
        List<AlertMatch> triggeredAlerts = new ArrayList<>();

        for (PriceAlert alert : priceAlerts.values()) {
            if (!alert.isActive()) continue;

            // Search for products that match this alert
            for (String store : pricesByStoreAndProduct.keySet()) {
                Map<String, Price> storeProducts = pricesByStoreAndProduct.get(store);

                for (Price price : storeProducts.values()) {
                    if (matchesProduct(price, alert.getProductName())) {
                        double currentPrice = price.getPrice() / 100.0;
                        boolean hasDiscount = false;
                        float discountPercentage = 0f;

                        // Check for current discounts
                        if (discountsByStore.containsKey(store)) {
                            for (Discount discount : discountsByStore.get(store)) {
                                if (discount.getProductId().equals(price.getProductId())) {
                                    LocalDate fromDate = LocalDate.parse(discount.getFromDate(), DATE_FORMATTER);
                                    LocalDate toDate = LocalDate.parse(discount.getToDate(), DATE_FORMATTER);

                                    if (!currentDate.isBefore(fromDate) && !currentDate.isAfter(toDate)) {
                                        currentPrice = currentPrice * (1 - discount.getDiscountPercentage() / 100.0);
                                        hasDiscount = true;
                                        discountPercentage = discount.getDiscountPercentage();
                                        break;
                                    }
                                }
                            }
                        }

                        // Check if price triggers the alert
                        if (currentPrice <= alert.getTargetPrice()) {
                            triggeredAlerts.add(new AlertMatch(
                                    alert, store, price.getProductName(),
                                    currentPrice, hasDiscount, discountPercentage
                            ));
                        }
                    }
                }
            }
        }

        if (triggeredAlerts.isEmpty()) {
            System.out.println("No price alerts triggered at current prices.");
        } else {
            System.out.println("🔔 PRICE ALERTS TRIGGERED:");
            for (AlertMatch match : triggeredAlerts) {
                String discountInfo = match.hasDiscount() ?
                        String.format(" (%.0f%% OFF!)", match.getDiscountPercentage()) : "";

                System.out.printf("✅ %s at %s: %.2f RON (target: %.2f RON)%s%n",
                        match.getActualProductName(),
                        match.getStore().toUpperCase(),
                        match.getCurrentPrice(),
                        match.getAlert().getTargetPrice(),
                        discountInfo);
            }
        }
    }

    private boolean matchesProduct(Price price, String productName) {
        String searchTerm = productName.toLowerCase().trim();
        return price.getProductName().toLowerCase().contains(searchTerm) ||
                price.getProductCategory().toLowerCase().contains(searchTerm) ||
                price.getBrand().toLowerCase().contains(searchTerm);
    }

    public void comparePackageSizes(String productName) {
        List<ValuePerUnitItem> items = analyzeValuePerUnit(productName);

        if (items.isEmpty()) {
            System.out.println("\n=== PACKAGE SIZE COMPARISON ===");
            System.out.println("No products found for: " + productName);
            return;
        }
        // we group by similar products (same base name, different sizes)
        Map<String, List<ValuePerUnitItem>> groupedProducts = items.stream()
                .collect(Collectors.groupingBy(item ->
                        item.getProductName().toLowerCase().replaceAll("\\d+.*", "").trim()));
        System.out.println("\n=== PACKAGE SIZE COMPARISON: " + productName.toUpperCase() + " ===");

        groupedProducts.forEach((productGroup, productItems) -> {
            if (productItems.size() > 1) {  // if multiple sizes exist
                System.out.println("\n" + productGroup.toUpperCase() + ":");

                productItems.stream()
                        .sorted(Comparator.comparing(ValuePerUnitItem::getFinalValuePerUnit))
                        .forEach(item -> {
                            String badge = productItems.indexOf(item) == 0 ? " BEST VALUE" : "";

                            System.out.printf("  %s %.0f%s - %s: %.2f RON (%.2f RON/%s)%s%n",
                                    item.getBrand(),
                                    item.getPackageQuantity(),
                                    item.getPackageUnit(),
                                    item.getStore().toUpperCase(),
                                    item.getFinalPrice(),
                                    item.getFinalValuePerUnit(),
                                    item.getPackageUnit(),
                                    badge);
                        });
            }
        });
    }

    public List<ValuePerUnitItem> analyzeValuePerUnit(String productName) {
        List<ValuePerUnitItem> items = new ArrayList<>();
        String search = productName.toLowerCase().trim();

        for(String store: pricesByStoreAndProduct.keySet()){
            Map<String, Price> storePrices = pricesByStoreAndProduct.get(store);

            for(Price price: storePrices.values()){
                if(!matchesSearch(price, store, search)){
                    continue;
                }
                // skip if package is invalid
                if(price.getPackageQuantity() <= 0 ){
                    continue;
                }

                // Calculate final price with current discounts
                double regularPrice = price.getPrice() / 100.0;
                double finalPrice = regularPrice;
                boolean hasDiscount = false;
                float discountPercentage = 0f;
                if (discountsByStore.containsKey(store)) {
                    for (Discount discount : discountsByStore.get(store)) {
                        if (discount.getProductId().equals(price.getProductId())) {
                            LocalDate fromDate = LocalDate.parse(discount.getFromDate(), DATE_FORMATTER);
                            LocalDate toDate = LocalDate.parse(discount.getToDate(), DATE_FORMATTER);

                            if (!currentDate.isBefore(fromDate) && !currentDate.isAfter(toDate)) {
                                finalPrice = regularPrice * (1 - discount.getDiscountPercentage() / 100.0);
                                hasDiscount = true;
                                discountPercentage = discount.getDiscountPercentage();
                                break;
                            }
                        }
                    }
                }

                ValuePerUnitItem item = new ValuePerUnitItem(
                        price.getProductName(), price.getBrand(), store, regularPrice, finalPrice,
                        price.getPackageQuantity(), price.getPackageUnit(), hasDiscount, discountPercentage
                );

                items.add(item);
            }
        }
        return items;
    }

    public void findBestValuePerUnit(String unit) {
        List<ValuePerUnitItem> items = analyzeValuePerUnit(unit);
        if (items.isEmpty()) {
            System.out.println("\n=== VALUE PER UNIT ANALYSIS ===");
            System.out.println("No products found for: " + unit);
            return;
        }
        // sort by finalValue, the lowest = best value
        items.sort(Comparator.comparing(ValuePerUnitItem::getFinalValuePerUnit));
        System.out.println("\n=== BEST VALUE PER UNIT: " + unit.toUpperCase() + " ===");
        System.out.println("(Sorted by best value - lowest price per unit)");

        for (int i = 0; i < Math.min(items.size(), 10); i++) {
            ValuePerUnitItem item = items.get(i);

            String priceInfo = item.hasDiscount() ?
                    String.format("%.2f RON (was %.2f RON, %.0f%% OFF)",
                            item.getFinalPrice(), item.getPrice(), item.getDiscountPercentage()) :
                    String.format("%.2f RON", item.getFinalPrice());

            String valueInfo = item.hasDiscount() ?
                    String.format("%.2f RON/%s (was %.2f RON/%s)",
                            item.getFinalValuePerUnit(), item.getPackageUnit(),
                            item.getValuePerUnit(), item.getPackageUnit()) :
                    String.format("%.2f RON/%s", item.getFinalValuePerUnit(), item.getPackageUnit());

            System.out.printf("%d. %s%n", i + 1, item.getFullProductName());
            System.out.printf("   Store: %s | Total: %s%n", item.getStore().toUpperCase(), priceInfo);
            System.out.printf("   Value per unit: %s%s%n", valueInfo, item.hasDiscount() ? " BEST DEAL" : "");
            System.out.println();
        }

    }
    private String extractDateFromFilename(String filename) {
        // prices/kaufland_2025-05-01.csv
        String baseName = filename.substring(filename.lastIndexOf("/") + 1);
        String[] parts = baseName.split("_");
        return parts[parts.length - 1].replace(".csv", "");
    }
    private boolean matchesSearch(Price price, String store, String searchTerm) {
        return store.toLowerCase().contains(searchTerm) ||
                price.getProductCategory().toLowerCase().contains(searchTerm) ||
                price.getBrand().toLowerCase().contains(searchTerm) ||
                price.getProductName().toLowerCase().contains(searchTerm);
    }

    public List<PricePoint> getPriceHistory(String searchTerm) {
        List<PricePoint> pricePoints = new ArrayList<>();
        String search = searchTerm.toLowerCase().trim();

        for (String priceFile : PRICE_FILES) {
            String store = extractStoreFromFilename(priceFile);
            String dateStr = extractDateFromFilename(priceFile);

            List<Price> prices = loadPricesFromCsv(priceFile);
            // match the discount file path
            String discountFile = "discounts/" + store + "_discounts_" + dateStr + ".csv";
            List<Discount> discounts = loadDiscountsFromCsv(discountFile);

            for (Price price : prices) {
                if (!matchesSearch(price, store, search)) {
                    continue;
                }

                // Calculate final price with discount
                double regularPrice = price.getPrice() / 100.0;
                double finalPrice = regularPrice;
                boolean hasDiscount = false;

                for (Discount discount : discounts) {
                    if (discount.getProductId().equals(price.getProductId())) {
                        // we get the available intervals of the discount for each store
                        LocalDate discountStart = LocalDate.parse(discount.getFromDate(), DATE_FORMATTER);
                        LocalDate discountEnd = LocalDate.parse(discount.getToDate(), DATE_FORMATTER);
                        LocalDate priceDate = LocalDate.parse(dateStr, DATE_FORMATTER);

                        if (!priceDate.isBefore(discountStart) && !priceDate.isAfter(discountEnd)) {
                            finalPrice = regularPrice * (1 - discount.getDiscountPercentage() / 100.0);
                            hasDiscount = true;
                            break;
                        }
                    }
                }

                pricePoints.add(new PricePoint(
                        dateStr, price.getProductName(), store, price.getBrand(),
                        price.getProductCategory(), regularPrice, finalPrice, hasDiscount
                ));
            }
        }
        return pricePoints.stream()
                .sorted(Comparator.comparing(PricePoint::getDate))
                .collect(Collectors.toList());
    }




    public void printPriceHistory(String searchTerm) {
        List<PricePoint> history = getPriceHistory(searchTerm);

        System.out.println("\n=== PRICE HISTORY FOR: " + searchTerm.toUpperCase() + " ===");

        if (history.isEmpty()) {
            System.out.println("No price data found for: " + searchTerm);
            return;
        }

        // lapte zuzu (Zuzu)
        Map<String, List<PricePoint>> byProduct = history.stream()
                .collect(Collectors.groupingBy(p -> p.getProductName() + " (" + p.getBrand() + ")"));

        byProduct.forEach((product, points) -> {
            System.out.println("\n" + product + ":");
            points.forEach(point -> {
                String priceInfo = point.hasDiscount() ?
                        String.format("%.2f (was %.2f)", point.getFinalPrice(), point.getRegularPrice()) :
                        String.format("%.2f", point.getFinalPrice());

                System.out.printf("  %s - %s: %s RON%s%n",
                        point.getDate(),
                        point.getStore().toUpperCase(),
                        priceInfo,
                        point.hasDiscount() ? " [DISCOUNT]" : "");
            });
        });

        System.out.printf("\nTotal data points found: %d%n", history.size());
    }

    public void recentDiscounts() {
        List<Discount> allDiscounts = new ArrayList<>();

        for (String discountFile : DISCOUNT_FILES) {
            List<Discount> discounts = loadDiscountsFromCsv(discountFile);
            allDiscounts.addAll(discounts);
        }

        List<Discount> recentDiscounts = allDiscounts.stream()
                .filter(discount -> {
                    LocalDate fromDate = LocalDate.parse(discount.getFromDate(), DATE_FORMATTER);

                    return !fromDate.isBefore(currentDate.minusDays(1)) && !fromDate.isAfter(currentDate);
                }).sorted( (d1,d2) -> {
                    LocalDate date1 = LocalDate.parse(d1.getFromDate(), DATE_FORMATTER);
                    LocalDate date2 = LocalDate.parse(d2.getFromDate(), DATE_FORMATTER);
                    return date1.compareTo(date2);
                })
                .toList();

        System.out.println("\n=== RECENT DISCOUNTS (Last 24 Hours) ===");
        if (recentDiscounts.isEmpty()) {
            System.out.println("No discounts started in the last 24 hours.");
        } else {
            for (int i = 0; i < recentDiscounts.size(); i++) {
                Discount discount = recentDiscounts.get(i);
                System.out.printf("%d. %s - %s %.0f%s (%.0f%% OFF) - Started: %s (Valid until: %s)%n",
                        i + 1,
                        discount.getProductName(),
                        discount.getBrand(),
                        discount.getPackageQuantity(),
                        discount.getPackageUnit(),
                        discount.getDiscountPercentage(),
                        discount.getFromDate(),
                        discount.getToDate());
            }
            System.out.printf("Total recent discounts found: %d%n", recentDiscounts.size());
        }

    }
    public void listBestDiscounts() {
        List<Discount> allDiscounts = new ArrayList<>();

        for (String discountFile : DISCOUNT_FILES) {
            List<Discount> discounts = loadDiscountsFromCsv(discountFile);
            allDiscounts.addAll(discounts);
        }
        List<Discount> topDiscounts = allDiscounts.stream()
                .sorted((d1, d2) -> Float.compare(d2.getDiscountPercentage(), d1.getDiscountPercentage()))
                .limit(10)
                .toList();

        System.out.println("\n=== TOP 10 DISCOUNTS ===");
        for (int i = 0; i < topDiscounts.size(); i++) {
            Discount discount = topDiscounts.get(i);
            System.out.printf("%d. %s - %s %.0f%s (%.0f%% OFF) - Valid: %s to %s%n",
                    i + 1,
                    discount.getProductName(),
                    discount.getBrand(),
                    discount.getPackageQuantity(),
                    discount.getPackageUnit(),
                    discount.getDiscountPercentage(),
                    discount.getFromDate(),
                    discount.getToDate());
        }
    }

    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void loadAllData() {
        pricesByStoreAndProduct.clear();
        discountsByStore.clear();

        for (String file : PRICE_FILES) {
            String store = extractStoreFromFilename(file);
            List<Price> prices = loadPricesFromCsv(file);

            Map<String, Price> productPrices = pricesByStoreAndProduct
                    .computeIfAbsent(store, k -> new HashMap<>());

            for (Price price : prices) {
                productPrices.put(price.getProductId().toLowerCase(), price);
                productPrices.put(price.getProductName().toLowerCase(), price);
            }
        }

        for (String file : DISCOUNT_FILES) {
            String store = extractStoreFromFilename(file);
            List<Discount> discounts = loadDiscountsFromCsv(file);
            discountsByStore.put(store, discounts);
        }
    }

    public ShoppingPlan generateOptimizedShoppingPlan(List<ShoppingItem> shoppingList) {
        ShoppingPlan plan = new ShoppingPlan();

        for (ShoppingItem item : shoppingList) {
            BestDeal bestDeal = findBestDealForProduct(item.getProductName(), item.getQuantity());

            if (bestDeal != null) {
                plan.addItem(bestDeal.getStore(), new ShoppingPlanItem(
                        bestDeal.getProductName(),
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

            Price price = null;
            if (storePrices.containsKey(normalizedProductName)) {
                price = storePrices.get(normalizedProductName);
            } else {
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

            if (discountsByStore.containsKey(store)) {
                for (Discount discount : discountsByStore.get(store)) {
                    if (discount.getProductId().equalsIgnoreCase(price.getProductId()) ||
                            discount.getProductName().toLowerCase().contains(normalizedProductName)) {

                        LocalDate fromDate = LocalDate.parse(discount.getFromDate(), DATE_FORMATTER);
                        LocalDate toDate = LocalDate.parse(discount.getToDate(), DATE_FORMATTER);

                        if ((currentDate.isEqual(fromDate) || currentDate.isAfter(fromDate)) &&
                                (currentDate.isEqual(toDate) || currentDate.isBefore(toDate))) {
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

    private String extractStoreFromFilename(String filename) {
        String baseName = filename.substring(filename.lastIndexOf("/") + 1);
        return baseName.split("_")[0];
    }

    private List<Price> loadPricesFromCsv(String resourcePath) {
        List<Price> prices = new ArrayList<>();

        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                System.err.println("Resource not found: " + resourcePath);
                return prices;
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line = br.readLine(); // Skip header


                while ((line = br.readLine()) != null) {
                    String[] data = line.split(";");
                    if (data.length >= 8) {
                        try {
                            Price price = getPrice(data);
                            prices.add(price);
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing numeric value in line: " + line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading price file: " + resourcePath);
            e.printStackTrace();
        }

        return prices;
    }

    private static Price getPrice(String[] data) {
        float rawPrice = Float.parseFloat(data[6]);
        int priceInCents = (int)(rawPrice * 100);

        Price price = new Price(
                data[0],
                data[1],
                data[2],
                data[3],
                Float.parseFloat(data[4]),
                data[5],
                priceInCents,
                data[7]
        );
        return price;
    }

    private List<Discount> loadDiscountsFromCsv(String resourcePath) {
        List<Discount> discounts = new ArrayList<>();

        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                System.err.println("Resource not found: " + resourcePath);
                return discounts;
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line = br.readLine(); // Skip header

                while ((line = br.readLine()) != null) {
                    String[] data = line.split(";");
                    if (data.length >= 9) {
                        try {
                            Discount discount = new Discount(
                                    data[0],
                                    data[1],
                                    data[2],
                                    Float.parseFloat(data[3]),
                                    data[4],
                                    data[5],
                                    data[6],
                                    data[7],
                                    Float.parseFloat(data[8])
                            );
                            discounts.add(discount);
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing numeric value in line: " + line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading discount file: " + resourcePath);
            e.printStackTrace();
        }

        return discounts;
    }
}