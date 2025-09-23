package com.irris.yamo_backend.controllers;

import com.irris.yamo_backend.entities.Address;
import com.irris.yamo_backend.services.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;

    @GetMapping("/customers/{customerId}/addresses")
    public List<Address> list(@PathVariable Long customerId) {
        return addressService.listByCustomer(customerId);
    }

    @PostMapping("/customers/{customerId}/addresses")
    public ResponseEntity<Address> add(@PathVariable Long customerId, @RequestBody Address address) {
        Address saved = addressService.addAddress(customerId, address);
        return ResponseEntity.created(URI.create("/api/addresses/" + saved.getId())).body(saved);
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }
}
