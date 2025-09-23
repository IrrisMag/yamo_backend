package com.irris.yamo_backend.repositories;

import com.irris.yamo_backend.entities.UserYamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserYamoRepository extends JpaRepository<UserYamo, Long> {
    Optional<UserYamo> findByUsername(String username);
}
