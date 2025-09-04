package com.ofss.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ofss.model.Stock;

public interface StockRepository extends JpaRepository<Stock, Integer> {

    Stock findBySymbol(String Symbol);

    List<Stock> findAllByIndustry(String industry);

}
