package com.irris.yamo_backend.controllers;

import com.irris.yamo_backend.services.GeoService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
public class GeoController {

    private final GeoService geoService;

    @PostMapping("/coverage")
    public ResponseEntity<?> coverage(@RequestBody LatLng req) {
        var result = geoService.checkCoverage(req.getLat(), req.getLng());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/estimate")
    public ResponseEntity<?> estimate(@RequestBody EstimateReq req) {
        var result = geoService.estimate(req.getOriginLat(), req.getOriginLng(), req.getDestLat(), req.getDestLng());
        return ResponseEntity.ok(result);
    }

    @Data
    public static class LatLng {
        private double lat;
        private double lng;
    }

    @Data
    public static class EstimateReq {
        private double originLat;
        private double originLng;
        private double destLat;
        private double destLng;
    }
}
