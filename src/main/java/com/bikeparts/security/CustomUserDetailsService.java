package com.bikeparts.security;

import com.bikeparts.entity.Account;
import com.bikeparts.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Autowired
    private Account account;

    @Override
    @Transactional(readOnly = true)
    // Spring Security ruft diese Methode beim Login auf
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Loading user: {}", email);

        // Account mit Cart und CartItems per JOIN FETCH aus der DB laden
        // kein Lazy-Proxy im Session-Account
        Account found = accountRepository.findByEmailWithCart(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + email));

        // Felder in den Session-Proxy kopieren
        account.setId(found.getId());
        account.setEmail(found.getEmail());
        account.setFirstName(found.getFirstName());
        account.setLastName(found.getLastName());
        account.setRole(found.getRole());
        account.setIsActive(found.getIsActive());
        account.setCart(found.getCart());

        // Spring Security’s User-Objekt erstellen (NICHT unsere Entity!)
        return org.springframework.security.core.userdetails.User.builder()
                .username(found.getEmail())
                .password(found.getPassword())
//                .authorities(getAuthorities(user)) // authorities hinzufügen
//                .accountLocked(!account.isAccountNonLocked())
                .disabled(!account.getIsActive())
                .build();
    }
}