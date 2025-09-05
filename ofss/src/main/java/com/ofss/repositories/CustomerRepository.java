package com.ofss.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ofss.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    // Customer findByEmailId(String emailId);
    Optional<Customer> findByEmailId(String emailId);

    void deleteByEmailId(String emailId);
}
