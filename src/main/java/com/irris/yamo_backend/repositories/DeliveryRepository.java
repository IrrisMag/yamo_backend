package com.irris.yamo_backend.repositories;

import com.irris.yamo_backend.entities.Delivery;
import com.irris.yamo_backend.entities.Livreur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByLivreur(Livreur livreur);
}
