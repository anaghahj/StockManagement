package com.ofss.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ofss.model.Customer;
import com.ofss.services.CustomerService;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    CustomerService service;

    @GetMapping("/{emailId}")
    public Customer getByEmailId(@PathVariable String emailId) {
        return service.getCustomerByEmailId(emailId);
    }

    @PostMapping
    public Customer create(@RequestBody Customer customer) {
        return service.create(customer);
    }

    @PutMapping("/{emailId}")
    public Customer update(@PathVariable String emailId, @RequestBody Customer customer) {
        return service.update(emailId, customer);
    }

    @PatchMapping("/{emailId}")
    public Customer patch(@PathVariable String emailId, @RequestBody Customer customer) {
        return service.patch(emailId, customer);
    }

    @DeleteMapping("/{emailId}")
    public ResponseEntity<Void> delete(@PathVariable String emailId) {
        service.delete(emailId);
        return ResponseEntity.noContent().build();
    }

}
