import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.text.SimpleDateFormat;
import org.json.JSONObject;
import org.json.JSONArray;

public class StockTracker {
    private static final String API_KEY = "80ca4805ba6079fhtpw6c8261fec2d0a"; // Example API. THIS API KEY IS NOT WORKING
    private static final String API_URL = "http://api.marketstack.com/v1/eod?access_key=" + API_KEY + "&symbols=";
    private static final String STOCK_FILE = "stocks.txt";
    private static List<String> trackedStocks = new ArrayList<>();
    private static Map<String, List<Double>> priceHistory = new HashMap<>();
    private static Map<String, Double> alerts = new HashMap<>(); // Stock -> Alert Price

    public static void main(String[] args) {
        loadStocksFromFile();
        Scanner scanner = new Scanner(System.in);

        // Start a timer to fetch prices every 5 minutes (300,000 ms)
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchAndUpdatePrices();
                checkAlerts();
            }
        }, 0, 300000);

        // Main loop for user interaction
        while (true) {
            System.out.println("\nStock Market Tracker");
            System.out.println("1. Add Stock");
            System.out.println("2. Remove Stock");
            System.out.println("3. View Stocks");
            System.out.println("4. Set Price Alert");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter stock symbol (e.g., AAPL): ");
                    String symbol = scanner.nextLine().toUpperCase();
                    addStock(symbol);
                    break;
                case 2:
                    System.out.print("Enter stock symbol to remove: ");
                    symbol = scanner.nextLine().toUpperCase();
                    removeStock(symbol);
                    break;
                case 3:
                    displayStocks();
                    break;
                case 4:
                    System.out.print("Enter stock symbol for alert: ");
                    symbol = scanner.nextLine().toUpperCase();
                    System.out.print("Enter alert price: ");
                    double price = scanner.nextDouble();
                    setAlert(symbol, price);
                    break;
                case 5:
                    saveStocksToFile();
                    timer.cancel();
                    System.out.println("Exiting...");
                    System.exit(0);
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    // Fetch stock prices via API
    private static void fetchAndUpdatePrices() {
        if (trackedStocks.isEmpty()) return;

        String symbols = String.join(",", trackedStocks);
        try {
            URL url = new URL(API_URL + symbols);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(response.toString());
            JSONArray data = json.getJSONArray("data");

            for (int i = 0; i < data.length(); i++) {
                JSONObject stock = data.getJSONObject(i);
                String symbol = stock.getString("symbol");
                double price = stock.getDouble("close");

                // Update price history
                priceHistory.computeIfAbsent(symbol, k -> new ArrayList<>()).add(price);
                if (priceHistory.get(symbol).size() > 10) { // Limit history to 10 entries
                    priceHistory.get(symbol).remove(0);
                }

                System.out.println("Updated " + symbol + ": $" + price + " at " + 
                    new SimpleDateFormat("HH:mm:ss").format(new Date()));
            }
            conn.disconnect();
        } catch (Exception e) {
            System.out.println("Error fetching stock prices: " + e.getMessage());
        }
    }

    // Display stocks and simple "chart"
    private static void displayStocks() {
        if (trackedStocks.isEmpty()) {
            System.out.println("No stocks tracked.");
            return;
        }
        for (String symbol : trackedStocks) {
            List<Double> prices = priceHistory.getOrDefault(symbol, Collections.emptyList());
            System.out.println("\n" + symbol + ":");
            if (prices.isEmpty()) {
                System.out.println("No data yet.");
            } else {
                System.out.println("Latest Price: $" + prices.get(prices.size() - 1));
                System.out.println("Price History (last " + prices.size() + " updates):");
                for (int i = 0; i < prices.size(); i++) {
                    System.out.print(String.format("%.2f ", prices.get(i)));
                    for (int j = 0; j < prices.get(i) / 10; j++) System.out.print("*"); // Simple "chart"
                    System.out.println();
                }
            }
        }
    }

    // Add a stock to track
    private static void addStock(String symbol) {
        if (!trackedStocks.contains(symbol)) {
            trackedStocks.add(symbol);
            System.out.println(symbol + " added.");
            fetchAndUpdatePrices(); // Immediate update
        } else {
            System.out.println(symbol + " already tracked.");
        }
    }

    // Remove a stock
    private static void removeStock(String symbol) {
        if (trackedStocks.remove(symbol)) {
            priceHistory.remove(symbol);
            alerts.remove(symbol);
            System.out.println(symbol + " removed.");
        } else {
            System.out.println(symbol + " not found.");
        }
    }

    // Set a price alert
    private static void setAlert(String symbol, double price) {
        if (trackedStocks.contains(symbol)) {
            alerts.put(symbol, price);
            System.out.println("Alert set for " + symbol + " at $" + price);
        } else {
            System.out.println(symbol + " not tracked. Add it first.");
        }
    }

    // Check alerts in a separate thread
    private static void checkAlerts() {
        for (Map.Entry<String, Double> entry : alerts.entrySet()) {
            String symbol = entry.getKey();
            double alertPrice = entry.getValue();
            List<Double> prices = priceHistory.getOrDefault(symbol, Collections.emptyList());
            if (!prices.isEmpty()) {
                double currentPrice = prices.get(prices.size() - 1);
                if ((currentPrice >= alertPrice && prices.get(prices.size() - 2) < alertPrice) ||
                    (currentPrice <= alertPrice && prices.get(prices.size() - 2) > alertPrice)) {
                    System.out.println("ALERT: " + symbol + " hit $" + currentPrice + 
                        " (target: $" + alertPrice + ")");
                }
            }
        }
    }

    // Load stocks from file
    private static void loadStocksFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(STOCK_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                trackedStocks.add(line.trim());
            }
            System.out.println("Loaded " + trackedStocks.size() + " stocks from file.");
        } catch (FileNotFoundException e) {
            System.out.println("No saved stocks found.");
        } catch (IOException e) {
            System.out.println("Error loading stocks: " + e.getMessage());
        }
    }

    // Save stocks to file
    private static void saveStocksToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STOCK_FILE))) {
            for (String stock : trackedStocks) {
                writer.write(stock);
                writer.newLine();
            }
            System.out.println("Saved " + trackedStocks.size() + " stocks to file.");
        } catch (IOException e) {
            System.out.println("Error saving stocks: " + e.getMessage());
        }
    }
}
