package com.bikeparts.security.jwt;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {
    private final SecretKey secretKey;

    @Value("${app.jwt.expiration:86400000}")  // default = 24h
    private long jwtExpirationMs;

    public JwtTokenProvider(@Value("${app.jwt.secret:MySecretKeyForJWTTokenGenerationThatIsLongEnough}") String secret) {
        this.secretKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + jwtExpirationMs);

        // später: Alle Authorities als String
//        String authorities = authentication.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(username)
//                .claim("authorities", authorities)
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(secretKey)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        io.jsonwebtoken.Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;

        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT token invalid: {}", e.getMessage());
            return false;
        }
    }
}
