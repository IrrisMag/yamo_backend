package com.irris.yamo_backend.services;

import com.irris.yamo_backend.entities.Livreur;
import com.irris.yamo_backend.entities.LivreurLocation;
import com.irris.yamo_backend.entities.Order;
import com.irris.yamo_backend.repositories.LivreurLocationRepository;
import com.irris.yamo_backend.repositories.LivreurRepository;
import com.irris.yamo_backend.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TrackingService {
    private static final Logger log = LoggerFactory.getLogger(TrackingService.class);

    private final LivreurLocationRepository locationRepo;
    private final LivreurRepository livreurRepo;
    private final OrderRepository orderRepo;

    // Emitters per orderId
    private final Map<Long, SseEmitter> orderEmitters = new ConcurrentHashMap<>();

    public LivreurLocation submitLocation(Long livreurId, Double lat, Double lng, Double heading, Double speed, Long orderId) {
        Livreur l = livreurRepo.findById(livreurId).orElseThrow();
        Order o = null;
        if (orderId != null) {
            o = orderRepo.findById(orderId).orElse(null);
        }
        LivreurLocation loc = LivreurLocation.builder()
                .livreur(l)
                .order(o)
                .latitude(lat)
                .longitude(lng)
                .heading(heading)
                .speed(speed)
                .build();
        LivreurLocation saved = locationRepo.save(loc);

        // Broadcast to SSE subscribers for this order
        if (o != null) {
            SseEmitter em = orderEmitters.get(o.getId());
            if (em != null) {
                try {
                    em.send(SseEmitter.event().name("location").data(Map.of(
                            "orderId", o.getId(),
                            "livreurId", l.getId(),
                            "lat", saved.getLatitude(),
                            "lng", saved.getLongitude(),
                            "heading", saved.getHeading(),
                            "speed", saved.getSpeed(),
                            "ts", saved.getCapturedAt()
                    )));
                } catch (IOException ex) {
                    log.warn("SSE send failed for order {}: {}", o.getId(), ex.getMessage());
                    orderEmitters.remove(o.getId());
                }
            }
        }
        return saved;
    }

    public LivreurLocation latestForOrder(Long orderId) {
        Order o = orderRepo.findById(orderId).orElseThrow();
        return locationRepo.findFirstByOrderOrderByCapturedAtDesc(o).orElse(null);
    }

    public List<LivreurLocation> recentForLivreur(Long livreurId, Duration since) {
        Livreur l = livreurRepo.findById(livreurId).orElseThrow();
        LocalDateTime cutoff = LocalDateTime.now().minus(since);
        return locationRepo.findByLivreurAndCapturedAtAfterOrderByCapturedAtDesc(l, cutoff);
    }

    public SseEmitter streamForOrder(Long orderId) {
        // Default timeout 30 minutes
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        orderEmitters.put(orderId, emitter);
        emitter.onCompletion(() -> orderEmitters.remove(orderId));
        emitter.onTimeout(() -> orderEmitters.remove(orderId));
        try {
            emitter.send(SseEmitter.event().name("init").data(Map.of("orderId", orderId)));
        } catch (IOException ignored) {}
        return emitter;
    }
}
