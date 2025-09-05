package com.ofss.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ofss.model.Customer;
import com.ofss.services.CustomerService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    CustomerService customerService;

    public AuthController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Customer customer) {
        try {
            Customer savedCustomer = customerService.register(customer);
            return ResponseEntity.ok("Customer registered successfully with email: " + savedCustomer.getEmailId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password) {
        Customer customer = customerService.login(email, password);
        if (customer != null) {
            return ResponseEntity
                    .ok("Login successful for customer: " + customer.getFirstName() + " " + customer.getLastName());
        } else {
            return ResponseEntity.status(401).body("Invalid credentials!");
        }
    }
}
