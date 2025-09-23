package com.irris.yamo_backend.repositories;

import com.irris.yamo_backend.entities.Category;
import com.irris.yamo_backend.entities.Garment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GarmentRepository extends JpaRepository<Garment, Long> {
    List<Garment> findByActiveTrue();
    List<Garment> findByCategory(Category category);
}
