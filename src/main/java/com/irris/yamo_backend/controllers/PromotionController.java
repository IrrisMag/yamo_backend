package com.irris.yamo_backend.controllers;

import com.irris.yamo_backend.repositories.PromotionRepository;
import com.irris.yamo_backend.services.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/promos")
@RequiredArgsConstructor
public class PromotionController {
    private final PromotionService promotionService;


}
