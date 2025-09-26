package com.irris.yamo_backend.controllers;

import com.irris.yamo_backend.entities.LivreurLocation;
import com.irris.yamo_backend.services.TrackingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/livreurs/{id}/location")
    public ResponseEntity<LivreurLocation> postLocation(@PathVariable("id") Long livreurId,
                                                        @RequestBody LocationUpdate req) {
        LivreurLocation saved = trackingService.submitLocation(livreurId, req.getLat(), req.getLng(), req.getHeading(), req.getSpeed(), req.getOrderId());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/orders/{orderId}/livreur/latest")
    public ResponseEntity<LivreurLocation> latest(@PathVariable Long orderId) {
        LivreurLocation loc = trackingService.latestForOrder(orderId);
        if (loc == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(loc);
    }

    @GetMapping(value = "/orders/{orderId}/livreur/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable Long orderId) {
        return trackingService.streamForOrder(orderId);
    }

    @GetMapping("/livreurs/{id}/location/recent")
    public List<LivreurLocation> recent(@PathVariable("id") Long livreurId,
                                        @RequestParam(name = "minutes", defaultValue = "30") long minutes) {
        return trackingService.recentForLivreur(livreurId, Duration.ofMinutes(minutes));
    }

    @Data
    public static class LocationUpdate {
        private Double lat;
        private Double lng;
        private Double heading;
        private Double speed;
        private Long orderId; // optional
    }
}
