package com.irris.yamo_backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class GeoService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.maps.apiKey:}")
    private String googleApiKey;

    @Value("${pricing.baseFee:1000}")
    private double baseFee;

    @Value("${pricing.perKm:200}")
    private double perKm;

    // Douala approximate center and radius for service coverage
    @Value("${geo.center.lat:4.0511}")
    private double centerLat;

    @Value("${geo.center.lng:9.7679}")
    private double centerLng;

    @Value("${geo.coverage.radiusKm:25}")
    private double coverageRadiusKm;

    public CoverageResult checkCoverage(double lat, double lng) {
        double distanceKm = haversineKm(lat, lng, centerLat, centerLng);
        boolean withinRadius = distanceKm <= coverageRadiusKm;
        Boolean inDoualaCm = null;
        try {
            inDoualaCm = isInDoualaCameroon(lat, lng);
        } catch (Exception ignored) {
            // If Geocoding API not enabled or fails, we'll fallback to radius-only check
        }
        boolean serviceable = withinRadius && (inDoualaCm == null || inDoualaCm);
        String message;
        if (!withinRadius) {
            message = String.format("Out of coverage: %.1f km from Douala center (max %.0f km)", distanceKm, coverageRadiusKm);
        } else if (inDoualaCm != null && !inDoualaCm) {
            message = "Service limited to Douala, Cameroon";
        } else if (inDoualaCm == null) {
            message = "In coverage radius (city check unavailable). Service is limited to Douala, Cameroon.";
        } else {
            message = "In coverage area (Douala, Cameroon)";
        }
        return new CoverageResult(serviceable, message, distanceKm);
    }

    public EstimateResult estimate(double originLat, double originLng, double destLat, double destLng) {
        // Enforce service area for both points
        CoverageResult origin = checkCoverage(originLat, originLng);
        if (!origin.serviceable) {
            throw new IllegalArgumentException("Origin not in service area (Douala, Cameroon). " + origin.message);
        }
        CoverageResult dest = checkCoverage(destLat, destLng);
        if (!dest.serviceable) {
            throw new IllegalArgumentException("Destination not in service area (Douala, Cameroon). " + dest.message);
        }

        if (googleApiKey == null || googleApiKey.isBlank()) {
            throw new IllegalStateException("Missing google.maps.apiKey property");
        }
        String origins = originLat + "," + originLng;
        String destinations = destLat + "," + destLng;
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json" +
                "?origins=" + URLEncoder.encode(origins, StandardCharsets.UTF_8) +
                "&destinations=" + URLEncoder.encode(destinations, StandardCharsets.UTF_8) +
                "&mode=driving&units=metric&key=" + URLEncoder.encode(googleApiKey, StandardCharsets.UTF_8);

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("Empty response from Distance Matrix API");
        }
        Object apiStatus = body.get("status");
        if (apiStatus == null || !"OK".equals(apiStatus.toString())) {
            throw new IllegalStateException("Distance Matrix API status not OK: " + apiStatus);
        }
        List rows = (List) body.getOrDefault("rows", Collections.emptyList());
        if (rows.isEmpty()) {
            throw new IllegalStateException("Distance Matrix API returned no rows");
        }
        Map firstRow = (Map) rows.get(0);
        List elements = (List) firstRow.getOrDefault("elements", Collections.emptyList());
        if (elements.isEmpty()) {
            throw new IllegalStateException("Distance Matrix API returned no elements");
        }
        Map firstEl = (Map) elements.get(0);
        Object elStatus = firstEl.get("status");
        if (elStatus == null || !"OK".equals(elStatus.toString())) {
            throw new IllegalStateException("Distance Matrix element status not OK: " + elStatus);
        }
        Map distance = (Map) firstEl.get("distance");
        Map duration = (Map) firstEl.get("duration");
        if (distance == null || duration == null) {
            throw new IllegalStateException("Distance Matrix missing distance or duration");
        }
        Number distanceMeters = (Number) distance.get("value");
        Number durationSeconds = (Number) duration.get("value");
        double km = distanceMeters.doubleValue() / 1000.0;
        double fee = computeFee(km);
        return new EstimateResult(distanceMeters.longValue(), durationSeconds.longValue(), km, fee);
    }

    public double computeFee(double distanceKm) {
        // Simple linear pricing. Extend with zones/surge as needed.
        return Math.round((baseFee + perKm * distanceKm));
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    private Boolean isInDoualaCameroon(double lat, double lng) {
        if (googleApiKey == null || googleApiKey.isBlank()) {
            return null; // cannot verify city without API key
        }
        String latlng = lat + "," + lng;
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" +
                URLEncoder.encode(latlng, StandardCharsets.UTF_8) +
                "&key=" + URLEncoder.encode(googleApiKey, StandardCharsets.UTF_8);
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map body = response.getBody();
        if (body == null) return null;
        Object status = body.get("status");
        if (status == null || !"OK".equals(status.toString())) return null;
        List results = (List) body.getOrDefault("results", Collections.emptyList());
        for (Object r : results) {
            if (!(r instanceof Map)) continue;
            Map result = (Map) r;
            List components = (List) result.getOrDefault("address_components", Collections.emptyList());
            boolean countryCM = false;
            boolean cityDouala = false;
            for (Object c : components) {
                if (!(c instanceof Map)) continue;
                Map comp = (Map) c;
                Object typesObj = comp.get("types");
                List types = typesObj instanceof List ? (List) typesObj : Collections.emptyList();
                String longName = String.valueOf(comp.get("long_name"));
                String shortName = String.valueOf(comp.get("short_name"));
                if (types.contains("country") && "CM".equalsIgnoreCase(shortName)) {
                    countryCM = true;
                }
                if ((types.contains("locality") || types.contains("administrative_area_level_2"))
                        && "Douala".equalsIgnoreCase(longName)) {
                    cityDouala = true;
                }
            }
            if (countryCM && cityDouala) return true;
        }
        return false;
    }

    // DTO-like result classes
    public static class CoverageResult {
        public final boolean serviceable;
        public final String message;
        public final double distanceFromCenterKm;
        public CoverageResult(boolean serviceable, String message, double distanceFromCenterKm) {
            this.serviceable = serviceable;
            this.message = message;
            this.distanceFromCenterKm = distanceFromCenterKm;
        }
        public boolean isServiceable() { return serviceable; }
        public String getMessage() { return message; }
        public double getDistanceFromCenterKm() { return distanceFromCenterKm; }
    }

    public static class EstimateResult {
        public final long distanceMeters;
        public final long durationSeconds;
        public final double distanceKm;
        public final double fee;
        public EstimateResult(long distanceMeters, long durationSeconds, double distanceKm, double fee) {
            this.distanceMeters = distanceMeters;
            this.durationSeconds = durationSeconds;
            this.distanceKm = distanceKm;
            this.fee = fee;
        }
        public long getDistanceMeters() { return distanceMeters; }
        public long getDurationSeconds() { return durationSeconds; }
        public double getDistanceKm() { return distanceKm; }
        public double getFee() { return fee; }
    }
}
