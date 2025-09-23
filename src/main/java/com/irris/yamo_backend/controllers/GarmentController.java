package com.irris.yamo_backend.controllers;

import com.irris.yamo_backend.entities.Category;
import com.irris.yamo_backend.entities.Garment;
import com.irris.yamo_backend.repositories.GarmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/catalog/garments")
@RequiredArgsConstructor
public class GarmentController {

    private final GarmentRepository repo;

    @PostMapping
    public ResponseEntity<Garment> create(@RequestBody Garment g) {
        Garment saved = repo.save(g);
        return ResponseEntity.created(URI.create("/api/catalog/garments/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Garment> update(@PathVariable Long id, @RequestBody Garment patch) {
        Garment cur = repo.findById(id).orElseThrow();
        cur.setName(patch.getName());
        cur.setIconUrl(patch.getIconUrl());
        cur.setCategory(patch.getCategory());
        cur.setActive(patch.getActive());
        return ResponseEntity.ok(repo.save(cur));
    }

    @GetMapping
    public List<Garment> list(@RequestParam(required = false) Long categoryId,
                              @RequestParam(required = false) Boolean active) {
        if (active != null && active) return repo.findByActiveTrue();
        if (categoryId != null) {
            Category c = new Category();
            c.setId(categoryId);
            return repo.findByCategory(c);
        }
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Garment> get(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
