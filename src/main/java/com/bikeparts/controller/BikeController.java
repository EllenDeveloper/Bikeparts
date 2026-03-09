

package com.bikeparts.controller;

import com.bikeparts.entity.*;
import com.bikeparts.service.AccountService;
import com.bikeparts.service.BikeService;
import com.bikeparts.service.CartService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class BikeController {
    private final AccountService accountService;
    private final CartService cartService;
    private final BikeService bikeService;

    @Autowired
    public BikeController(AccountService accountService, CartService cartService, BikeService bikeService) {
        this.accountService = accountService;
        this.cartService = cartService;
        this.bikeService = bikeService;
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

    // get all bikes
    @GetMapping("{id}/bikes")
    public ResponseEntity<List<Bike>> getAllBikes(@PathVariable Long id) {
        return ResponseEntity.ok(bikeService.getAllBikesByAccountId(id));
    }

    // Cart zu existierendem Account hinzufügen
    @PostMapping("/{id}/cart")
    public ResponseEntity<Account> addCart(
            @PathVariable Long id,
            @RequestBody Cart cart) {

        return accountService.findById(id)
                .map(account -> {
                    account.addCart(cart);  // Helper-Methode!
                    return ResponseEntity.ok(accountService.updateAccount(account));
                })
                .orElse(ResponseEntity.notFound().build());
    }


    // Bikepart zu existierendem Cart eines Accounts hinzufügen
    // eher cart/addBikepartToCart
    @PostMapping("/{id}/cart/bikeparts/{bikepartId}")
    public ResponseEntity<?> addBikePartToCart(
            @PathVariable Long accountId,
            @PathVariable Long bikepartId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        Account account = accountService.findById(accountId).orElse(null);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Account " + accountId + " nicht gefunden"));
        }
//        if (account.getCart() == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("error", "Account " + id + " hat keinen Warenkorb"));
//        }
        // TODO: Bikepart muss zu dem account gehören
        cartService.addBikepartToCart(bikepartId, accountId, quantity);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{accountId}/bike/{bikeId}")
    public ResponseEntity<List<Bikepart>> getAllBikeparts(
            @PathVariable Long accountId,
            @PathVariable Long bikeId)  {
        // TODO later: check: "gehört das Bike dem eingeloggten User?". dann muss Account nicht übergeben werden
        return ResponseEntity.ok(bikeService.getAllBikeparts(accountId, bikeId));
    }


}
