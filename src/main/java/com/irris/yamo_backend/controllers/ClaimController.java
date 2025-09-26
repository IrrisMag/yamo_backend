package com.irris.yamo_backend.controllers;

import com.irris.yamo_backend.entities.Claim;
import com.irris.yamo_backend.entities.Customer;
import com.irris.yamo_backend.repositories.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimRepository repo;

    @PostMapping
    public ResponseEntity<Claim> create(@RequestBody Claim c) {
        Claim saved = repo.save(c);
        return ResponseEntity.created(URI.create("/api/claims/" + saved.getId())).body(saved);
    }

    @GetMapping
    public List<Claim> list(@RequestParam(required = false) Claim.Status status,
                            @RequestParam(required = false) Long customerId) {
        if (status != null) return repo.findByStatus(status);
        if (customerId != null) {
            Customer c = new Customer();
            c.setId(customerId);
            return repo.findByCustomer(c);
        }
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Claim> get(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<Claim> updateStatus(@PathVariable Long id, @RequestParam Claim.Status status, @RequestParam(required = false) String notes) {
        Claim cur = repo.findById(id).orElseThrow();
        cur.setStatus(status);
        if (notes != null) cur.setResolutionNotes(notes);
        return ResponseEntity.ok(repo.save(cur));
    }
}
