package com.irris.yamo_backend.services;

import com.irris.yamo_backend.entities.Customer;
import com.irris.yamo_backend.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public Customer createCustomer(Customer customer) {
        Customer saved = customerRepository.save(customer);
        saved.setCode("CLI-" + String.format("%03d", saved.getId()));
        return customerRepository.save(saved);
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

    public Customer findByCode(String code) {
        return customerRepository.findByCode(code);
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public void delete(Long id) {
        customerRepository.deleteById(id);
    }
}
