# Price Comparator System

A Java-based price comparison and shopping optimization system that analyzes prices from multiple grocery stores (Kaufland, Lidl, Profi) and provides intelligent shopping recommendations with discount tracking and price history analysis.

## Project Structure

```
src/
├── main/
│   └── java/
│       └── org/
│           └── example/
│               ├── PriceComparator.java    # Main application class
│               ├── Price.java              # Price data model
│               ├── Discount.java           # Discount data model
│               ├── BestDeal.java           # Best deal result model
│               ├── ShoppingItem.java       # Shopping list item model
│               ├── ShoppingPlan.java       # Optimized shopping plan model
│               ├── ShoppingPlanItem.java   # Individual plan item model
│               ├── ValuePerUnitItem.java   # Value analysis model
│               ├── PricePoint.java         # Price history data point
│               ├── PriceAlert.java         # Price alert model
│               └── AlertMatch.java         # Alert matching result model
│
└── resources/
    ├── prices/                             # Price data CSV files
    │   ├── kaufland_2025-05-01.csv
    │   ├── kaufland_2025-05-08.csv
    │   ├── lidl_2025-05-01.csv
    │   ├── lidl_2025-05-08.csv
    │   ├── profi_2025-05-01.csv
    │   └── profi_2025-05-08.csv
    │
    └── discounts/                          # Discount data CSV files
        ├── kaufland_discounts_2025-05-01.csv
        ├── kaufland_discounts_2025-05-08.csv
        ├── lidl_discounts_2025-05-01.csv
        ├── lidl_discounts_2025-05-08.csv
        ├── profi_discounts_2025-05-01.csv
        └── profi_discounts_2025-05-08.csv
```

### Key Components

- **PriceComparator**: Main service class containing all business logic
- **Data Models**: Price, Discount, and various result models for structured data handling
- **CSV Data Files**: Historical price and discount data from multiple stores
- **Analysis Features**: Price comparison, shopping optimization, value analysis, and price alerts

## Prerequisites

Before running this application, ensure you have the following installed:

- **Java Development Kit (JDK)** 11 or higher
- **Gradle** 6.0 or higher (optional, for build automation)
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code with Java extensions)

## Installation & Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd price-comparator
   ```

2. **Compile the Java project**
   ```bash
   # Using Gradle (if build.gradle exists)
   ./gradlew build
   
   # Or using javac directly
   javac -d out src/main/java/org/example/*.java
   ```

3. **Ensure CSV data files are in place**
   - Verify that the `resources/prices/` directory contains all required CSV files
   - Verify that the `resources/discounts/` directory contains all required CSV files
   - The application expects specific file naming patterns: `{store}_{date}.csv`

## Building the Application

### Development Build
```bash
# Using Gradle
./gradlew compileJava

# Using javac directly
javac -cp src/main/java -d out src/main/java/org/example/*.java
```

### Production Build
```bash
# Create JAR file using Gradle
./gradlew jar

# Or create JAR manually
jar cvf price-comparator.jar -C out/ .
```

## Running the Application

### Using Java directly
```bash
# From project root
java -cp "out:src/main/resources" org.example.PriceComparator

# Or if using JAR
java -cp "price-comparator.jar:src/main/resources" org.example.PriceComparator
```

### Using Gradle
```bash
./gradlew run
```

The application will execute automatically and display:
- **Optimized Shopping Plan** for a predefined shopping list
- **Price Comparisons** for specific products
- **Top 10 Discounts** currently available  
- **Recent Discounts** from the last 24 hours
- **Price History Analysis** by store, category, and brand
- **Value Per Unit Analysis** for different products
- **Price Alert System** demonstrations

## Core Features & Usage

This is a console-based application that demonstrates various price comparison and shopping optimization features. The main functionality is accessed through method calls in the `PriceComparator` class.

### 1. Shopping Plan Optimization
**Purpose**: Generate an optimized shopping plan that finds the best deals across multiple stores.

```java
List<ShoppingItem> shoppingList = Arrays.asList(
    new ShoppingItem("lapte", 2),
    new ShoppingItem("pâine", 1),
    new ShoppingItem("vin alb demisec", 1)
);

ShoppingPlan plan = comparator.generateOptimizedShoppingPlan(shoppingList);
```

**Output**: 
- Shopping list organized by store
- Best prices for each item (including discounts)
- Total cost and savings breakdown

### 2. Price Comparison
**Purpose**: Compare prices for a specific product across all stores.

```java
// Compare prices for milk across all stores
Map<String, List<Price>> results = comparator.comparePrices("lapte");
```

**Output**: List of all matching products with prices from different stores

### 3. Discount Analysis
**Purpose**: Find the best available discounts and recent promotional offers.

```java
// Get top 10 biggest discounts
comparator.listBestDiscounts();

// Get discounts that started in the last 24 hours
comparator.recentDiscounts();
```

### 4. Price History Tracking
**Purpose**: Analyze price trends over time by store, category, or brand.

```java
// Search by store
comparator.printPriceHistory("kaufland");

// Search by product category  
comparator.printPriceHistory("lactate");

// Search by brand
comparator.printPriceHistory("zuzu");
```

**Output**: Historical price data showing regular prices, discounted prices, and trends

### 5. Value Per Unit Analysis
**Purpose**: Find the best value for money by comparing price per unit measurements.

```java
// Find best value per unit for milk products
comparator.findBestValuePerUnit("lapte");

// Compare package sizes for bread
comparator.comparePackageSizes("pâine");
```

**Output**: Products ranked by best value per unit (lowest price per kg, liter, etc.)

### 6. Price Alert System
**Purpose**: Set up alerts for when products drop below target prices.

```java
// Set price alerts
comparator.addPriceAlert("lapte", 4.00);    // Alert when milk ≤ 4.00 RON
comparator.addPriceAlert("pâine", 2.50);    // Alert when bread ≤ 2.50 RON

// Check triggered alerts
comparator.checkAndShowTriggeredAlerts();

// Show active alerts
comparator.showActiveAlerts();

// Remove alerts
comparator.removePriceAlert("lapte");
```

## CSV Data Format

### Price Data Files
Expected format for `prices/{store}_{date}.csv`:
```csv
product_id;product_name;brand;product_category;package_quantity;package_unit;price;store_location
PROD001;lapte;Zuzu;lactate;1.0;L;4.50;Kaufland Brasov
```

### Discount Data Files  
Expected format for `discounts/{store}_discounts_{date}.csv`:
```csv
product_id;product_name;brand;package_quantity;package_unit;store_location;from_date;to_date;discount_percentage
PROD001;lapte;Zuzu;1.0;L;Kaufland Brasov;2025-05-01;2025-05-08;15.0
```

## Assumptions Made & Simplifications

1. **Data Storage**: The application uses in-memory data structures and CSV files instead of a database for simplicity
2. **Date Handling**: Current date is manually set in the code (`2025-05-08`) rather than using system date
3. **Product Matching**: Uses simple string matching and contains operations for product searches
4. **Price Comparison**: Assumes all prices are in Romanian Lei (RON) and stores prices as integers (cents)
5. **Discount Logic**: Discounts are applied as percentage reductions and assumed to be valid within date ranges
6. **Store Data**: Limited to three stores (Kaufland, Lidl, Profi) with predefined data files
7. **Product Categories**: Uses Romanian product names and categories (lapte, pâine, etc.)
8. **Console Output**: All results are displayed via console output rather than structured API responses

## Design Choices

1. **Single Class Design**: All functionality is consolidated in `PriceComparator` class for simplicity
2. **CSV Data Source**: Uses CSV files as the data source to avoid database setup complexity  
3. **In-Memory Processing**: All data is loaded into memory for fast access during analysis
4. **Model Classes**: Separate model classes for clean data representation
5. **Stream API Usage**: Leverages Java 8+ Stream API for data processing and filtering
6. **Date Formatting**: Uses consistent `yyyy-MM-dd` date format throughout the application

## Creating a Demo Video

To create the 5-10 minute demonstration video requested:

### Video Content Structure:
1. **Introduction (1 min)**
   - Briefly explain the project purpose
   - Show the project structure in your IDE

2. **Code Walkthrough (2-3 min)**
   - Highlight key methods in `PriceComparator.java`
   - Explain the data models and CSV structure
   - Show the main optimization logic

3. **Live Demonstration (4-5 min)**
   - Run the application and show the console output
   - Explain each feature as it executes:
     - Shopping plan optimization
     - Price comparisons
     - Discount analysis
     - Price history tracking
     - Value per unit analysis
     - Price alert system

4. **Key Features Summary (1 min)**
   - Summarize the implemented functionality
   - Mention potential extensions or improvements

### Recording Tools:
- **Screen Recording**: Use OBS Studio, Camtasia, or built-in screen recording
- **Audio**: Ensure clear voice explanation
- **Resolution**: Record in 1080p for clarity

### Video Tips:
- Prepare talking points in advance
- Run the application once before recording to ensure smooth execution
- Consider adding captions or annotations to highlight key points
- Keep explanations concise but informative

## Sample Output

When you run the application, you'll see output like:

```
=== OPTIMIZED SHOPPING PLAN ===
Date: 2025-05-08

SHOPPING LISTS BY STORE:

KAUFLAND:
- lapte zuzu x2: 3.83 (DISCOUNTED from 4.50) RON
- pâine alba x1: 2.20 RON
STORE SUBTOTAL: 9.85 RON

LIDL:
- vin alb demisec x1: 12.99 RON
STORE SUBTOTAL: 12.99 RON

TOTAL COST: 22.84 RON
TOTAL SAVINGS: 1.34 RON

=== PRICE COMPARISON - LAPTE ===
KAUFLAND - lapte zuzu 1L: 4.50 RON
LIDL - lapte zuzu 1L: 4.75 RON
PROFI - lapte zuzu 1L: 4.60 RON

=== TOP 10 DISCOUNTS ===
1. vin rosu - Murfatlar 750ml (25% OFF) - Valid: 2025-05-01 to 2025-05-08
2. lapte - Zuzu 1L (15% OFF) - Valid: 2025-05-01 to 2025-05-08
3. brânză - Delaco 200g (12% OFF) - Valid: 2025-05-01 to 2025-05-10

=== RECENT DISCOUNTS (Last 24 Hours) ===
1. paste - Barilla 500g (10% OFF) - Started: 2025-05-08 (Valid until: 2025-05-15)
2. pâine - Vel Pitar 500g (8% OFF) - Started: 2025-05-08 (Valid until: 2025-05-12)

=== PRICE HISTORY FOR: KAUFLAND ===

lapte zuzu (Zuzu):
  2025-05-01 - KAUFLAND: 4.50 RON
  2025-05-08 - KAUFLAND: 3.83 (was 4.50) RON [DISCOUNT]

=== BEST VALUE PER UNIT: LAPTE ===
1. lapte zuzu 1L
   Store: KAUFLAND | Total: 3.83 RON (was 4.50 RON, 15% OFF)
   Value per unit: 3.83 RON/L (was 4.50 RON/L) BEST DEAL

=== PRICE ALERTS ===
Active Alerts:
- lapte: Alert when price ≤ 4.00 RON
- pâine: Alert when price ≤ 2.50 RON

Triggered Alerts:
 ALERT: lapte is now 3.83 RON (target: ≤ 4.00 RON) at KAUFLAND
 ALERT: pâine is now 2.20 RON (target: ≤ 2.50 RON) at KAUFLAND

