package com.irris.yamo_backend.repositories;

import com.irris.yamo_backend.entities.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long> {
    Optional<ServiceType> findByCode(String code);
    List<ServiceType> findByMode(ServiceType.Mode mode);
    List<ServiceType> findByActiveTrue();
}
