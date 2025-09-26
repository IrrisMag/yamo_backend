package com.irris.yamo_backend.controllers;

import com.irris.yamo_backend.entities.Livreur;
import com.irris.yamo_backend.entities.Task;
import com.irris.yamo_backend.services.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<Task> create(@RequestBody Task task,
                                       @RequestParam(required = false) Long customerId,
                                       @RequestParam(required = false) Long orderId) {
        Task saved = taskService.createTask(task, customerId, orderId);
        return ResponseEntity.created(URI.create("/api/tasks/" + saved.getId())).body(saved);
    }

    @PostMapping("/{taskId}/assign-livreur")
    public Task assignLivreur(@PathVariable Long taskId, @RequestBody Livreur livreur) {
        return taskService.assignLivreur(taskId, livreur);
    }

    @PostMapping("/{taskId}/status")
    public Task updateStatus(@PathVariable Long taskId, @RequestParam Task.TaskStatus status,
                             @RequestParam(required = false) String notes) {
        return taskService.updateStatus(taskId, status, notes);
    }

    @GetMapping
    public List<Task> listBetween(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return taskService.listBetween(from, to);
    }

    @GetMapping("/livreur/{livreurId}")
    public List<Task> listForLivreur(@PathVariable Long livreurId,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return taskService.listForLivreur(livreurId, from, to);
    }

    @GetMapping("/customer/{customerId}")
    public List<Task> listForCustomer(@PathVariable Long customerId) {
        return taskService.listForCustomer(customerId);
    }
}
