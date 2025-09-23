package com.irris.yamo_backend.repositories;

import com.irris.yamo_backend.entities.Livreur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LivreurRepository extends JpaRepository<Livreur, Long> {
    List<Livreur> findByActiveTrue();
    List<Livreur> findByVehicleType(String vehicleType);
    List<Livreur> findByPhone(String phone);
}
