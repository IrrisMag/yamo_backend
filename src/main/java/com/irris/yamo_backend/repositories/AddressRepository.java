package com.irris.yamo_backend.repositories;

import com.irris.yamo_backend.entities.Address;
import com.irris.yamo_backend.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCustomer(Customer customer);
}
