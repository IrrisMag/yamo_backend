package com.irris.yamo_backend.repositories;

import com.irris.yamo_backend.entities.Claim;
import com.irris.yamo_backend.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByStatus(Claim.Status status);
    List<Claim> findByCustomer(Customer customer);
}
