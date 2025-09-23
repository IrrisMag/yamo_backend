package com.irris.yamo_backend.services;

import com.irris.yamo_backend.entities.Customer;
import com.irris.yamo_backend.entities.Livreur;
import com.irris.yamo_backend.entities.Order;
import com.irris.yamo_backend.entities.Task;
import com.irris.yamo_backend.repositories.CustomerRepository;
import com.irris.yamo_backend.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final CustomerRepository customerRepository;

    public Task createTask(Task task, Long customerId, Long orderId) {
        if (customerId != null) {
            Customer customer = customerRepository.findById(customerId).orElseThrow();
            task.setCustomer(customer);
        }
        // Order linking can be set by caller if needed
        if (task.getStatus() == null) task.setStatus(Task.TaskStatus.SCHEDULED);
        if (task.getRemindBeforeMinutes() == null) task.setRemindBeforeMinutes(30);
        return taskRepository.save(task);
    }

    public Task assignLivreur(Long taskId, Livreur livreur) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        task.setAssigneeLivreur(livreur);
        return taskRepository.save(task);
    }

    public Task updateStatus(Long taskId, Task.TaskStatus status, String notes) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        task.setStatus(status);
        if (notes != null) task.setNotes(notes);
        return taskRepository.save(task);
    }

    public List<Task> listForLivreur(Long livreurId, LocalDateTime from, LocalDateTime to) {
        Livreur l = new Livreur();
        l.setId(livreurId);
        return taskRepository.findByAssigneeLivreurAndScheduledAtBetween(l, from, to);
    }

    public List<Task> listForCustomer(Long customerId) {
        Customer c = new Customer();
        c.setId(customerId);
        return taskRepository.findByCustomer(c);
    }

    public List<Task> listBetween(LocalDateTime from, LocalDateTime to) {
        return taskRepository.findByScheduledAtBetween(from, to);
    }
}
