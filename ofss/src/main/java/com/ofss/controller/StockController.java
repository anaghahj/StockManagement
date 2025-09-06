package com.ofss.controller;

import java.time.LocalDate;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ofss.model.ChatRequest;
import com.ofss.model.Stock;
import com.ofss.services.StockRecomendation;
import com.ofss.services.StockService;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class StockController {

    @Autowired
    private StockService stockService;
    @Autowired
    private StockRecomendation stockrec;

    @GetMapping("/stocks")
    public ResponseEntity<Object> viewAllStocks() {
        return stockService.viewAllStocks();
    }

    // only admin can do
    @PostMapping("/stocks")
    public ResponseEntity<Object> addStock(@RequestBody Stock stock) {
        return stockService.addStock(stock);
    }

    @GetMapping("/stock/{symbol}")
    public ResponseEntity<Object> findBySymbol(@PathVariable("symbol") String symbol) {
        return stockService.findBySymbol(symbol);
    }

    // only admin can do
    @PutMapping("/stock/{symbol}")
    public ResponseEntity<Object> updateStock(@PathVariable String symbol, @RequestBody Stock stock) {
        return stockService.updateStock(symbol, stock);
    }

    @DeleteMapping("/stock/{symbol}")
    public ResponseEntity<Object> deleteBySymbol(@PathVariable String symbol) {
        stockService.deleteAllHistoryForStock(symbol);
        return stockService.deleteBySymbol(symbol);
    }

    // @GetMapping("/stocks/search")
    // public ResponseEntity<Object> searchBySector(@RequestParam String sector) {
    // return stockService.searchBySector(sector);
    // }
    @GetMapping("/stock/{symbol}/latest-price")
    public ResponseEntity<Object> getLatestPrice(@PathVariable String symbol) {
        return stockService.getLatestPrice(symbol);
    }

    @GetMapping("/stocks/sorted")
    public ResponseEntity<Object> sorted(@RequestParam(defaultValue = "asc") String order) {
        return stockService.getAllStocksSortedByLatestPrice(order);
    }

    @GetMapping("/stocks/smart-recommendations")
    public ResponseEntity<Object> getSmartRecommendations(@RequestParam(defaultValue = "medium") String riskProfile) {
        return stockrec.getSmartRecommendations(riskProfile);
    }

    @GetMapping("/stock/{symbol}/history")
    public ResponseEntity<Object> getStockHistory(@PathVariable String symbol) {
        return stockService.getStockHistory(symbol);
    }

    @GetMapping("/stock/{symbol}/average")
    public ResponseEntity<Object> getAverageClosePrice(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        // You have LocalDate objects (start, end),
        // but your service expects java.util.Date, so convert them:
        Date startdate = java.sql.Date.valueOf(start); // Convert LocalDate to java.sql.Date (which extends
        // java.util.Date)
        Date enddate = java.sql.Date.valueOf(end);

        System.out.println("Start date: " + startdate);
        System.out.println("End date: " + enddate);

        return stockService.getAverageClosePrice(symbol, startdate, enddate);
    }

    @GetMapping("/stocks/top-movers")
    public ResponseEntity<Object> getTopMovers() {
        return stockService.getTopMovers();
    }

    @PostMapping("/chat/recommend")
    public ResponseEntity<Object> chatRecommendation(@RequestBody ChatRequest chatRequest) {
        String message = chatRequest.getMessage() != null ? chatRequest.getMessage().toLowerCase() : "";
        Double budget = chatRequest.getBudget();
        String risk = chatRequest.getRiskProfile() != null ? chatRequest.getRiskProfile() : "medium";
        return stockService.chatrec(message, budget, risk);
    }

}
