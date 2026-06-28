package com.bikeparts.security.jwt;

import com.bikeparts.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
// OncePerRequestFilter: filter wird nur 1 mal pro Request ausgeführt
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
        // JWT token vom Request holen
        String jwtToken = getJwtTokenFromRequest(request);

        // Token validieren
        if (StringUtils.hasText(jwtToken)
                && tokenProvider.validateToken(jwtToken)) {

            // Username aus Token holen
            String username = tokenProvider.getUsernameFromToken(jwtToken);

            // User aus DB laden
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Authentication-Objekt erstellen
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // Authentication-Objekt im SecurityContext setzen
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Set Authentication for user: {}", username);
        }
    } catch (Exception ex) {
        log.error("Could not set user authentication in security context", ex);
    }

    // zum nächsten Filter weiterleiten
        filterChain.doFilter(request, response);
    }

    private String getJwtTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Format: "Bearer <JWT token>"
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " entfernen
        }

        return null;
    }
}
