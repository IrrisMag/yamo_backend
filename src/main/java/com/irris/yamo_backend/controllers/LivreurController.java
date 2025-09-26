package com.irris.yamo_backend.controllers;

import com.irris.yamo_backend.entities.Livreur;
import com.irris.yamo_backend.repositories.LivreurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/livreurs")
@RequiredArgsConstructor
public class LivreurController {

    private final LivreurRepository livreurRepository;

    @PostMapping
    public ResponseEntity<Livreur> create(@RequestBody Livreur livreur) {
        if (livreur.getActive() == null) livreur.setActive(true);
        Livreur saved = livreurRepository.save(livreur);
        return ResponseEntity.created(URI.create("/api/livreurs/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Livreur> update(@PathVariable Long id, @RequestBody Livreur patch) {
        Livreur cur = livreurRepository.findById(id).orElseThrow();
        // Simple field-by-field update
        cur.setName(patch.getName());
        cur.setSurname(patch.getSurname());
        cur.setNationalId(patch.getNationalId());
        cur.setPhone(patch.getPhone());
        cur.setEmail(patch.getEmail());
        cur.setVehicleType(patch.getVehicleType());
        cur.setVehicleBrand(patch.getVehicleBrand());
        cur.setVehicleColor(patch.getVehicleColor());
        cur.setVehicleRegistration(patch.getVehicleRegistration());
        cur.setPhotoUrl(patch.getPhotoUrl());
        cur.setComment(patch.getComment());
        cur.setActive(patch.getActive());
        return ResponseEntity.ok(livreurRepository.save(cur));
    }

    @GetMapping
    public List<Livreur> list(@RequestParam(required = false) Boolean active,
                              @RequestParam(required = false) String vehicleType) {
        if (active != null && active) return livreurRepository.findByActiveTrue();
        if (vehicleType != null) return livreurRepository.findByVehicleType(vehicleType);
        return livreurRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livreur> get(@PathVariable Long id) {
        return livreurRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        livreurRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
