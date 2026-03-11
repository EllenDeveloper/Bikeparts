package com.bikeparts.service;

import com.bikeparts.annotation.Timed;
import com.bikeparts.entity.Account;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import com.bikeparts.repository.AccountRepository;

@Service
@Transactional
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Account getAccountByEmail(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account nicht gefunden: " + email));
    }

    public Account createAccount(Account account) {
        if (accountRepository.existsByEmail(account.getEmail())) {
            throw new IllegalArgumentException("Account email bereits vergeben");
        }

        // Password verschlüsseln
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setIsActive(true);

        setAccountRelations(account);
        setBikePartRelations(account);
        return accountRepository.save(account);
    }

//    Bei bike und bikeparts auch
    private void setAccountRelations(Account account) {
        if (account.getBikes() != null) {
            account.getBikes().forEach(bike -> bike.setAccount(account));
        }
    }

    private void setBikePartRelations(Account account) {
        if (account.getBikes() != null) {
            account.getBikes().forEach(bike -> {
                if (bike.getBikeparts() != null) {
                    bike.getBikeparts().forEach(bikepart -> bikepart.setBike(bike));
                }
            });
        }
    }
    

    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    // Zeitmessung mit der @Timed-Annotation. Der PerformanceAspect wird aufgerufen
    @Timed
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Account updateAccount(Account account) {
        return accountRepository.save(account);
    }

    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }
}
