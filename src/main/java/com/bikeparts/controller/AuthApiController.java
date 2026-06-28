package com.bikeparts.controller;

import com.bikeparts.security.jwt.JwtTokenProvider;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        // Authentication mit Username + Password
        // ruft implizit CustomUserDetailsService.loadUserByUsername() auf
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // Authentication im SecurityContext setzen
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT Token
        String jwt = tokenProvider.generateToken(authentication);

        // Token zurückgeben als JSON {"token": "askj", "type": "Bearer"}
        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    @Data
    @RequiredArgsConstructor
    public static class JwtResponse {
        private final String token;
        private final String type = "Bearer";
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
