package com.irris.yamo_backend.security;

import com.irris.yamo_backend.entities.UserYamo;
import com.irris.yamo_backend.repositories.UserYamoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserYamoRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserYamo u = userRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        GrantedAuthority auth = new SimpleGrantedAuthority("ROLE_" + u.getRole().name());
        return new User(u.getUsername(), u.getPasswordHash(), u.getActive() != null && u.getActive(), true, true, true, Collections.singleton(auth));
    }
}
