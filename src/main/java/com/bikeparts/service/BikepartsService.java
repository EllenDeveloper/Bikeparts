
package com.bikeparts.service;


import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BikepartsService {
    
//    private final BikepartsRepository bikepartsRepository;
//
//    // Constructor Injection
//    @Autowired
//    public BikepartsService(BikepartsRepository bikepartsRepository) {
//        this.bikepartsRepository = bikepartsRepository;
//    }
//    
//    // Alle Accounten abrufen
//    public List<Account> getAllAccounts() {
//        return bikepartsRepository.findAll();
//    }
//
//    // Account by ID abrufen
//    public Optional<Account> getAccountById(Long id) {
//        return bikepartsRepository.findById(id);
//    }
//
//    // Account erstellen
//    public Account createAccount(Account account) {
//        // Hier könnte Business-Logik stehen
//        // z.B. Email-Duplikat-Check, Validierung, etc.
//        return bikepartsRepository.save(account);
//    }
//    
//    public Account updateAccount(Long id, Account accountDetails) {
//        Account account = bikepartsRepository.findById(id).orElseThrow(() -> new RuntimeException("Account not found with id " + id));
//        
//        account.setNickname(accountDetails.getNickname());
//        account.setEmail(accountDetails.getEmail());
//        return bikepartsRepository.save(account);
//    }
//    
//    public void deleteAccount(Long id) {
//        bikepartsRepository.deleteById(id);
//    }
}
