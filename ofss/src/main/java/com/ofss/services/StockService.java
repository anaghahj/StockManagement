package com.ofss.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ofss.model.Stock;
import com.ofss.model.StockHistory;
import com.ofss.repositories.StockHistoryRepository;
import com.ofss.repositories.StockRepository;

@Service
public class StockService {

    @Autowired
    private StockRepository sr;

    @Autowired
    private StockHistoryRepository shr;
    @Autowired
    private StockRecomendation stockrec;

    public ResponseEntity<Object> viewAllStocks() {
        List<Stock> stocks = sr.findAll();

        for (Stock stock : stocks) {
            List<StockHistory> history = shr.findTop2ByStockOrderByTradeDateDesc(stock);
            if (history.size() >= 2) {
                double latest = history.get(0).getClosePrice();
                double previous = history.get(1).getClosePrice();
                stock.setColorTag(latest > previous ? "green" : "red");
            } else {
                stock.setColorTag("grey");
            }
        }

        return ResponseEntity.ok(stocks);
    }

    public ResponseEntity<Object> addStock(Stock stock) {
        return ResponseEntity.status(201).body(sr.save(stock));
    }

    public ResponseEntity<Object> findBySymbol(String Symbol) {
        Stock stock = sr.findBySymbol(Symbol);
        if (stock == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stock);
    }

    public ResponseEntity<Object> updateStock(String symbol, Stock updatedStock) {
        Stock stock = sr.findBySymbol(symbol);
        if (stock == null) {
            return ResponseEntity.notFound().build();
        }

        stock.setName(updatedStock.getName());
        stock.setBasePrice(updatedStock.getBasePrice());
        stock.setIndustry(updatedStock.getIndustry());

        return ResponseEntity.ok(sr.save(stock));
    }

    public ResponseEntity<Object> deleteBySymbol(String symbol) {
        Stock stock = sr.findBySymbol(symbol);
        if (stock == null) {
            return ResponseEntity.notFound().build();
        }

        sr.delete(stock);
        return ResponseEntity.ok("Stock deleted successfully.");
    }

    // public ResponseEntity<Object> searchBySector(String sector) {
    //     return ResponseEntity.ok(sr.findBySectorIgnoreCase(sector));
    // }
    public ResponseEntity<Object> getLatestPrice(String symbol) {
        Stock stock = sr.findBySymbol(symbol);
        if (stock == null) {
            return ResponseEntity.notFound().build();
        }

        StockHistory latest = shr.findTopByStockOrderByTradeDateDesc(stock);
        return ResponseEntity.ok(latest != null ? latest.getClosePrice() : "No history available.");
    }

    public ResponseEntity<Object> getStockHistory(String symbol) {
        Stock stock = sr.findBySymbol(symbol);
        if (stock == null) {
            return ResponseEntity.notFound().build();
        }

        List<StockHistory> history = shr.findByStockOrderByTradeDateAsc(stock);
        return ResponseEntity.ok(history);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public ResponseEntity<Object> getAverageClosePrice(String symbol, Date start, Date end) {
        Stock stock = sr.findBySymbol(symbol);
        if (stock == null) {
            return ResponseEntity.notFound().build();
        }

        List<StockHistory> history = shr.findByStockAndTradeDateBetween(stock, start, end);
        if (history.isEmpty()) {
            return ResponseEntity.ok("No data available for the given date range.");
        }

        double average = round(history.stream()
                .mapToDouble(StockHistory::getClosePrice)
                .average()
                .orElse(0.0));

        return ResponseEntity.ok(average);
    }

    public ResponseEntity<Object> deleteAllHistoryForStock(String symbol) {
        Stock stock = sr.findBySymbol(symbol);
        if (stock == null) {
            return ResponseEntity.notFound().build();
        }

        shr.deleteByStock(stock);
        return ResponseEntity.ok("All history deleted for stock: " + symbol);
    }

    public ResponseEntity<Object> getTopMovers() {
        List<Stock> stocks = sr.findAll();
        List<Map<String, Object>> movers = new ArrayList<>();

        for (Stock stock : stocks) {
            List<StockHistory> historyList = shr.findTop2ByStockOrderByTradeDateDesc(stock);
            if (historyList.size() < 2) {
                continue;
            }

            StockHistory latest = historyList.get(0);
            StockHistory previous = historyList.get(1);

            double changePercent = ((latest.getClosePrice() - previous.getClosePrice()) / previous.getClosePrice())
                    * 100;

            Map<String, Object> map = new HashMap<>();
            map.put("symbol", stock.getSymbol());
            map.put("changePercent", round(changePercent));
            movers.add(map);
        }

        movers.sort((a, b) -> Double.compare((Double) b.get("changePercent"), (Double) a.get("changePercent")));

        return ResponseEntity.ok(movers.subList(0, 5));

    }

    public ResponseEntity<Object> chatrec(String message, Double budget, String risk) {
        if (message == null) {
            return ResponseEntity.badRequest().body("Message cannot be null");
        }

        message = message.toLowerCase();

        if (message.contains("recommend") || message.contains("buy")) {
            List<Map<String, Object>> recommendations = (List<Map<String, Object>>) stockrec.getSmartRecommendations(risk).getBody();

            if (budget == null) {
                // No budget constraints, return all recommendations
                return ResponseEntity.ok(recommendations);
            }

            double remainingBudget = budget;
            List<Map<String, Object>> affordableRecommendations = recommendations.stream()
                    .filter(r -> {
                        Object priceObj = r.get("latestPrice");
                        return priceObj instanceof Number && ((Number) priceObj).doubleValue() <= budget;

                    })
                    .collect(Collectors.toList());
            List<Map<String, Object>> strongBuys = affordableRecommendations.stream()
                    .filter(r -> "Strong Buy".equalsIgnoreCase((String) r.get("recommendation")))
                    .sorted(Comparator.comparingDouble(r -> ((Number) r.get("latestPrice")).doubleValue()))
                    .collect(Collectors.toList());

            List<Map<String, Object>> buys = affordableRecommendations.stream()
                    .filter(r -> "Buy".equalsIgnoreCase((String) r.get("recommendation")))
                    .sorted(Comparator.comparingDouble(r -> ((Number) r.get("latestPrice")).doubleValue()))
                    .collect(Collectors.toList());

            List<Map<String, Object>> selectedStocks = new ArrayList<>();

            remainingBudget = buyStocksWithinBudget(strongBuys, remainingBudget, selectedStocks);

            if (remainingBudget > 0) {
                remainingBudget = buyStocksWithinBudget(buys, remainingBudget, selectedStocks);
            }

            return ResponseEntity.ok(selectedStocks);
        } else {
            return ResponseEntity.ok("Sorry, I can only help with stock recommendations currently.");
        }
    }

    private double buyStocksWithinBudget(List<Map<String, Object>> stocks, double budget, List<Map<String, Object>> boughtStocks) {
        Iterator<Map<String, Object>> it = stocks.iterator();
        while (it.hasNext()) {
            Map<String, Object> stock = it.next();
            double price = ((Number) stock.get("latestPrice")).doubleValue();

            if (price <= budget) {
                boughtStocks.add(stock);
                budget -= price;
                it.remove();
            } else {
                break;
            }
        }
        return budget;
    }

}
