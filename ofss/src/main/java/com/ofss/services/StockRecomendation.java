package com.ofss.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ofss.model.Stock;
import com.ofss.model.StockHistory;
import com.ofss.repositories.StockHistoryRepository;
import com.ofss.repositories.StockRepository;

@Service
public class StockRecomendation {

    @Autowired
    StockHistoryRepository shr;
    @Autowired
    StockRepository sr;

    private double calculateTrendScore(List<StockHistory> history) {
        if (history.size() < 5) {
            return 50;
        }

        double start = history.get(history.size() - 5).getClosePrice();
        double end = history.get(history.size() - 1).getClosePrice();

        double percentChange = ((end - start) / start) * 100;

        if (percentChange > 10) {
            return 100;
        }
        if (percentChange > 5) {
            return 85;
        }
        if (percentChange > 0) {
            return 70;
        }
        if (percentChange > -5) {
            return 50;
        }
        return 30;
    }

    private double calculateVolatilityScore(List<StockHistory> history) {
        if (history.size() < 5) {
            return 50;
        }

        double avg = history.stream()
                .mapToDouble(StockHistory::getClosePrice)
                .average()
                .orElse(0.0);

        double variance = history.stream()
                .mapToDouble(h -> Math.pow(h.getClosePrice() - avg, 2))
                .average()
                .orElse(0.0);

        double stdDev = Math.sqrt(variance);

        // Less volatile = higher score
        if (stdDev < 2) {
            return 90;
        }
        if (stdDev < 5) {
            return 70;
        }
        if (stdDev < 10) {
            return 50;
        }
        return 30;
    }

    private double calculateRiskScore(double volatility, String riskProfile) {
        switch (riskProfile.toLowerCase()) {
            case "low":
                return volatility < 3 ? 90 : 40;
            case "medium":
                return volatility < 6 ? 85 : 50;
            case "high":
                return volatility < 10 ? 90 : 60;
            default:
                return 50;
        }
    }

    private double calculateBehaviorScore(Stock stock) {
        // Placeholder: simulate for now
        return 50 + Math.random() * 50; // Score between 50 and 100
    }

    public ResponseEntity<Object> getSmartRecommendations(String riskProfile) {
        List<Stock> stocks = sr.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Stock stock : stocks) {
            List<StockHistory> history = shr.findByStockOrderByTradeDateAsc(stock); // oldest to newest
            // if (history.size() < 5) {
            //     continue;
            // }

            double trendScore = calculateTrendScore(history);
            double volatilityScore = calculateVolatilityScore(history);
            double volatility = Math.sqrt(history.stream()
                    .mapToDouble(h -> Math.pow(h.getClosePrice()
                    - history.stream().mapToDouble(StockHistory::getClosePrice).average().orElse(0.0), 2))
                    .average().orElse(0.0));

            double riskScore = calculateRiskScore(volatility, riskProfile);
            double behaviorScore = calculateBehaviorScore(stock);

            double finalScore = round((trendScore + volatilityScore + riskScore + behaviorScore) / 4);

            String recommendation = finalScore > 80 ? "Strong Buy"
                    : finalScore > 65 ? "Buy"
                            : finalScore > 50 ? "Watchlist" : "Avoid";

            double latestPrice = history.isEmpty() ? 0.0 : round(history.get(history.size() - 1).getClosePrice());

            Map<String, Object> map = new HashMap<>();
            map.put("symbol", stock.getSymbol());
            map.put("trendScore", trendScore);
            map.put("volatilityScore", volatilityScore);
            map.put("riskScore", riskScore);
            map.put("behaviorScore", behaviorScore);
            map.put("finalScore", finalScore);
            map.put("recommendation", recommendation);
            map.put("latestPrice", latestPrice);
            result.add(map);
        }

        result.sort((a, b) -> Double.compare((Double) b.get("finalScore"), (Double) a.get("finalScore")));

        return ResponseEntity.ok(result.subList(0, result.size()));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

}
