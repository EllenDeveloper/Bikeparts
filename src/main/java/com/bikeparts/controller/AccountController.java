
package com.bikeparts.controller;

import com.bikeparts.entity.Account;
import com.bikeparts.entity.Bike;
import com.bikeparts.repository.AccountRepository;
import com.bikeparts.service.BikepartsService;
import com.bikeparts.service.AccountService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<Account> getAllAccounts() {
        return accountService.findAll();
    }
    
    // GET /api/accounts/{id} - Account by ID abrufen
    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        return accountService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/accounts - Account erstellen
//    @PostMapping
//    public ResponseEntity<Account> createAccount(@Valid @RequestBody Account account) {
//        Account created = accountService.createAccount(account);
//        return ResponseEntity.status(HttpStatus.CREATED).body(created);
//    }
    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody Account account) {  // ohne @Valid: (@Valid @RequestBody Account account)
        // Email validieren
        if (account.getEmail() == null || account.getEmail().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Email darf nicht leer sein"));
        }

        // Email Format prüfen
        if (!account.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Email hat ungültiges Format"));
        }

        Account created = accountService.createAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    
    // PUT /api/accounts/{id} - Account aktualisieren
    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody Account accountDetails
    ) {
        try {
            Account updated = accountService.updateAccount(accountDetails);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/accounts/{id} - Account löschen
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
    
    // Bike zu existierendem Account hinzufügen
    @PostMapping("/{id}/bikes")
    public ResponseEntity<Account> addBike(
            @PathVariable Long id,
            @RequestBody Bike bike) {

        return accountService.findById(id)
                .map(account -> {
                    account.addBike(bike);  // Helper-Methode!
                    return ResponseEntity.ok(accountService.updateAccount(account));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Adresse zu existierender Person hinzufügen POST
     * /api/persons/{id}/addresses Body: { "street": "...", "city": "...", ... }
     */
//    @PostMapping("/{id}/addresses")
//    public ResponseEntity<Person> addAddress(
//            @PathVariable Long id,
//            @RequestBody Address address) {
//
//        return personRepository.findById(id)
//                .map(person -> {
//                    person.addAddress(address);  // Helper-Methode synchronisiert!
//                    return ResponseEntity.ok(personRepository.save(person));
//                })
//                .orElse(ResponseEntity.notFound().build());
//    }
}
