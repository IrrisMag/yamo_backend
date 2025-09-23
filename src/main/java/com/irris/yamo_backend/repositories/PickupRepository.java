package com.irris.yamo_backend.repositories;

import com.irris.yamo_backend.entities.Livreur;
import com.irris.yamo_backend.entities.Pickup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PickupRepository extends JpaRepository<Pickup, Long> {
    List<Pickup> findByLivreur(Livreur livreur);
}
