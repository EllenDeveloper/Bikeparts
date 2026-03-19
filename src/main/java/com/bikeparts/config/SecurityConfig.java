package com.bikeparts.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor

public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Profile("prod")
    public SecurityFilterChain filterChainProd(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Später aktivieren

                .authorizeHttpRequests(auth -> auth
                        // Öffentliche URLs
                        // TODO later: "/register"
                        .requestMatchers("/", "/login").permitAll()
                        .requestMatchers("/h2-console/**").permitAll() // H2-Console erlauben
                        // Admin-Only URLs
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Alle anderen brauchen nur Login
                        .anyRequest().authenticated()
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
    @Profile("h2")
    public SecurityFilterChain filterChainForDevelopment(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Später aktivieren

                .authorizeHttpRequests(auth -> auth
                                // Öffentliche URLs
//                                .requestMatchers("/", "/login").permitAll()
                        // TODO API ist freigeschaltet. Das entfernen!
                                .requestMatchers("/", "/login", "/api/**").permitAll()
                                .requestMatchers("/h2-console/**").permitAll() // H2-Console erlauben
                                // Admin-Only URLs
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                // Authority-basiert
//                        .requestMatchers("/api/accounts/delete/**").hasAuthority("DELETE_PRIVILEGES")
                                // Alle anderen brauchen nur Login
                                .anyRequest().authenticated()
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
//                .httpBasic(basic -> {
//                })

        return http.build();
    }
}
