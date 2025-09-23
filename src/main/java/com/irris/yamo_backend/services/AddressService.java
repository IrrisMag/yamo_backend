package com.irris.yamo_backend.services;

import com.irris.yamo_backend.entities.Address;
import com.irris.yamo_backend.entities.Customer;
import com.irris.yamo_backend.repositories.AddressRepository;
import com.irris.yamo_backend.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    public List<Address> listByCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId).orElseThrow();
        return addressRepository.findByCustomer(customer);
    }

    public Address addAddress(Long customerId, Address address) {
        Customer customer = customerRepository.findById(customerId).orElseThrow();
        address.setCustomer(customer);
        return addressRepository.save(address);
    }

    public void deleteAddress(Long addressId) {
        addressRepository.deleteById(addressId);
    }
}
