package com.bikeparts.config;

import com.bikeparts.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    @Profile("prod")
    public SecurityFilterChain filterChainProd(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Später aktivieren

                .authorizeHttpRequests(auth -> auth
                        // Öffentliche URLs
                        // TODO later: "/register"
                        .requestMatchers("/api/auth/**").permitAll() //  JWT Login ohne Auth erlauben
                        .requestMatchers("/", "/login").permitAll()
                        .requestMatchers("/h2-console/**").permitAll() // H2-Console erlauben
                        // Admin-Only URLs
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Alle anderen brauchen nur Login
                        .anyRequest().authenticated()
                )
                // JWT Filter hinzufügen
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // Session Management soll durch JWT STATELESS sein!
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        // default parameter ist username!
                        .usernameParameter("email")
                        .defaultSuccessUrl("/bikes", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                // frames in h2-console erlauben
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }

    @Bean
    @Profile("dev")
    public SecurityFilterChain filterChainForDevelopment(HttpSecurity http) throws Exception {
        log.debug("******** use application-dev.properties");
        http
                .csrf(csrf -> csrf.disable()) // Später aktivieren

                .authorizeHttpRequests(auth -> auth
                                // Öffentliche URLs
                                .requestMatchers("/api/auth/**").permitAll() //  JWT Login ohne Auth erlauben
                                .requestMatchers("/h2-console/**").permitAll() // H2-Console erlauben
                                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                                // Admin-Only URLs
//                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                // Authority-basiert
                                // Alle anderen brauchen nur Login
                                .anyRequest().authenticated()
                )
                // JWT Filter hinzufügen
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // IF_REQUIRED: Session fuer Browser-Login (Thymeleaf), JwtAuthenticationFilter.doFilterInternal() wird immer aufgerufen
                //        curl:    Header vorhanden → Token validieren → User setzen
                //        Browser: Header fehlt     → macht nichts, ruft filterChain.doFilter() auf
                //                                  → Spring prüft Session → User gefunden
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                // Für Login im Browser
                .formLogin(form -> form
                        .loginPage("/login")
                        // default parameter ist username!
                        .usernameParameter("email")
                        .defaultSuccessUrl("/bikes", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                // frames in h2-console erlauben
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                // für Bruno/Postman. Basic Auth
                .httpBasic(basic -> {
                });

        return http.build();
    }
}
