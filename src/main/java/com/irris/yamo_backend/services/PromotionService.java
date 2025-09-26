package com.irris.yamo_backend.services;

import com.irris.yamo_backend.entities.Promotion;
import com.irris.yamo_backend.repositories.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromotionService {
    private final PromotionRepository promotionRepository;

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    public Promotion getPromotionById(Long id) {
        return promotionRepository.findById(id).orElse(null);
    }

    public Promotion savePromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    public void deletePromotionById(Long id) {
        promotionRepository.deleteById(id);
    }

    public Promotion updatePromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    public Optional<Promotion> findPromotionByCode(String code) {
        return promotionRepository.findByCodeAndActiveTrue(code);
    }

}
