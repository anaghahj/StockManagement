package com.ofss.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import com.ofss.model.Customer;
import com.ofss.model.Stock;
import com.ofss.model.Transaction;
import com.ofss.model.TransactionType;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByCustomerEmailId(String emailId);

    List<Transaction> findByStock(Stock stock);

    List<Transaction> findByCustomerAndStock(Customer customer, Stock stock);

    List<Transaction> findByCustomerAndStockAndType(Customer customer, Stock stock, TransactionType transactionType);
}
