package com.irris.yamo_backend.repositories;

import com.irris.yamo_backend.entities.Customer;
import com.irris.yamo_backend.entities.Livreur;
import com.irris.yamo_backend.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssigneeLivreurAndScheduledAtBetween(Livreur livreur, LocalDateTime from, LocalDateTime to);
    List<Task> findByCustomer(Customer customer);
    List<Task> findByScheduledAtBetween(LocalDateTime from, LocalDateTime to);
}
