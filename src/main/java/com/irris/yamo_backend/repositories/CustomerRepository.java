package com.irris.yamo_backend.repositories;

import com.irris.yamo_backend.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository <Customer, Long>{
    Customer findByCode(String code);
}
