package com.ofss.services;

import com.ofss.exception.ResourceNotFoundException;
import com.ofss.model.Customer;
import com.ofss.repositories.CustomerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ Get all customers
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    // ✅ Get customer by email ID (throws if not found)
    public Customer getCustomerByEmailId(String emailId) {
        return customerRepository.findByEmailId(emailId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with emailId: " + emailId));
    }

    // ✅ Save (create) a new customer
    public Customer create(Customer customer) {
        return customerRepository.save(customer);
    }

    // ✅ Update full customer data
    public Customer update(String emailId, Customer customer) {
        Customer existing = getCustomerByEmailId(emailId);
        existing.setFirstName(customer.getFirstName());
        existing.setLastName(customer.getLastName());
        existing.setCity(customer.getCity());
        existing.setPhoneNo(customer.getPhoneNo());
        return customerRepository.save(existing);
    }

    // ✅ Patch (partial update)
    public Customer patch(String emailId, Customer customer) {
        Customer existing = getCustomerByEmailId(emailId);
        if (customer.getFirstName() != null) {
            existing.setFirstName(customer.getFirstName());
        }
        if (customer.getLastName() != null) {
            existing.setLastName(customer.getLastName());
        }
        if (customer.getCity() != null) {
            existing.setCity(customer.getCity());
        }
        if (customer.getPhoneNo() != null) {
            existing.setPhoneNo(customer.getPhoneNo());
        }
        return customerRepository.save(existing);
    }

    // ✅ Delete customer
    public void delete(String emailId) {
        Customer existing = getCustomerByEmailId(emailId);
        customerRepository.delete(existing);
    }

    // ✅ Register customer (with email check and password hashing)
    public Customer register(Customer customer) {
        String normalizedEmail = customer.getEmailId().toLowerCase();
        customer.setEmailId(normalizedEmail);

        if (customerRepository.findByEmailId(normalizedEmail).isPresent()) {
            throw new RuntimeException("Email already registered!");
        }

        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        return customerRepository.save(customer);
    }

    // ✅ Login (authentication)
    public Customer login(String emailId, String rawPassword) {
        return customerRepository.findByEmailId(emailId)
                .filter(existingCustomer -> passwordEncoder.matches(rawPassword, existingCustomer.getPassword()))
                .orElse(null);
    }
}
