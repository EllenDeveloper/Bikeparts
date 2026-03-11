package com.bikeparts.security;

import com.bikeparts.config.AccountConfig;
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

        account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + email));

        // Spring Security’s User-Objekt erstellen (NICHT unsere Entity!)
        // Authentifizierung: use the original account data from JPA: found
        return org.springframework.security.core.userdetails.User.builder()
                .username(account.getEmail()) // email
                .password(account.getPassword())
//                .authorities(getAuthorities(user)) // authorities hinzufügen
//                .accountLocked(!account.isAccountNonLocked())
                .disabled(!account.getIsActive())
                .build();
    }
}