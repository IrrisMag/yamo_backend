package com.irris.yamo_backend.repositories;

import com.irris.yamo_backend.entities.Livreur;
import com.irris.yamo_backend.entities.LivreurLocation;
import com.irris.yamo_backend.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LivreurLocationRepository extends JpaRepository<LivreurLocation, Long> {
    // Latest N locations for a livreur
    List<LivreurLocation> findTop50ByLivreurOrderByCapturedAtDesc(Livreur livreur);

    // All locations for an order, newest first
    List<LivreurLocation> findByOrderOrderByCapturedAtDesc(Order order);

    // Recent locations since timestamp for a livreur
    List<LivreurLocation> findByLivreurAndCapturedAtAfterOrderByCapturedAtDesc(Livreur livreur, LocalDateTime since);

    // Latest single point by order or livreur
    Optional<LivreurLocation> findFirstByOrderOrderByCapturedAtDesc(Order order);
    Optional<LivreurLocation> findFirstByLivreurOrderByCapturedAtDesc(Livreur livreur);
}
