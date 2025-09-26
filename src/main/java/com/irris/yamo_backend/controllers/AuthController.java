package com.irris.yamo_backend.controllers;

import com.irris.yamo_backend.entities.Role;
import com.irris.yamo_backend.entities.UserYamo;
import com.irris.yamo_backend.repositories.UserYamoRepository;
import com.irris.yamo_backend.security.JwtUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserYamoRepository userRepo;
    private final PasswordEncoder encoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq req) {
        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        UserYamo user = userRepo.findByUsername(req.getUsername()).orElseThrow();
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("customerId", user.getCustomerId());
        claims.put("livreurId", user.getLivreurId());
        String token = jwtUtil.generateToken(user.getUsername(), claims);
        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "role", user.getRole().name(),
                        "customerId", user.getCustomerId(),
                        "livreurId", user.getLivreurId()
                )
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterReq req) {
        if (userRepo.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", true, "message", "Username already exists"));
        }
        UserYamo u = UserYamo.builder()
                .username(req.getUsername())
                .passwordHash(encoder.encode(req.getPassword()))
                .role(req.getRole() == null ? Role.CUSTOMER : req.getRole())
                .customerId(req.getCustomerId())
                .livreurId(req.getLivreurId())
                .active(true)
                .build();
        userRepo.save(u);
        return ResponseEntity.ok(Map.of("created", true));
    }

    @Data
    public static class LoginReq {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }

    @Data
    public static class RegisterReq {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        private Role role;
        private Long customerId;
        private Long livreurId;
    }
}
