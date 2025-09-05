package com.ofss.controller;

import com.ofss.model.TradeRequest;
import com.ofss.model.Transaction;
import com.ofss.services.TransactionService;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyStock(@RequestBody TradeRequest tradeRequest) {
        try {
            Transaction transaction = transactionService.buyStock(tradeRequest);
            return ResponseEntity.ok(transaction);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong while buying stock");
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sellStock(@RequestBody TradeRequest tradeRequest) {
        try {
            Transaction transaction = transactionService.sellStock(tradeRequest);
            return ResponseEntity.ok(transaction);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong while selling stock");
        }
    }

    // Get all transaction history
    @GetMapping("/history/{email}")
    public ResponseEntity<List<Transaction>> getTransactionHistory(@PathVariable String email) {
        return ResponseEntity.ok(transactionService.getTransactionHistory(email));
    }

    // Get portfolio (current holdings)
    @GetMapping("/portfolio/{email}")
    public ResponseEntity<Map<String, Integer>> getPortfolio(@PathVariable String email) {
        return ResponseEntity.ok(transactionService.getPortfolio(email));
    }
}
