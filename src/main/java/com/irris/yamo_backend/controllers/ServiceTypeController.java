package com.irris.yamo_backend.controllers;

import com.irris.yamo_backend.entities.ServiceType;
import com.irris.yamo_backend.repositories.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/catalog/service-types")
@RequiredArgsConstructor
public class ServiceTypeController {

    private final ServiceTypeRepository repo;

    @PostMapping
    public ResponseEntity<ServiceType> create(@RequestBody ServiceType s) {
        ServiceType saved = repo.save(s);
        return ResponseEntity.created(URI.create("/api/catalog/service-types/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceType> update(@PathVariable Long id, @RequestBody ServiceType patch) {
        ServiceType cur = repo.findById(id).orElseThrow();
        cur.setCode(patch.getCode());
        cur.setName(patch.getName());
        cur.setMode(patch.getMode());
        cur.setPrice(patch.getPrice());
        cur.setDescription(patch.getDescription());
        cur.setActive(patch.getActive());
        return ResponseEntity.ok(repo.save(cur));
    }

    @GetMapping
    public List<ServiceType> list(@RequestParam(required = false) ServiceType.Mode mode,
                                  @RequestParam(required = false) Boolean active) {
        if (mode != null) return repo.findByMode(mode);
        if (active != null && active) return repo.findByActiveTrue();
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceType> get(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
