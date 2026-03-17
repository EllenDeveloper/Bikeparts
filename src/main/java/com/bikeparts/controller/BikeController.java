

package com.bikeparts.controller;

import com.bikeparts.entity.*;
import com.bikeparts.price.service.ScrapingResult;
import com.bikeparts.service.AccountService;
import com.bikeparts.service.BikeService;
import com.bikeparts.service.CartService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class BikeController {
    private final AccountService accountService;
    private final CartService cartService;
    private final BikeService bikeService;
    private final Account account;

    @Autowired
    public BikeController(AccountService accountService, CartService cartService,
                          BikeService bikeService, Account account) {
        this.accountService = accountService;
        this.cartService = cartService;
        this.bikeService = bikeService;
        this.account = account;
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
        // TODO: accountService.validateEmail return bool
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

        // TODO: remove account

        return accountService.findById(id)
                .map(account -> {
                    account.addBike(bike);  // Helper-Methode!
                    return ResponseEntity.ok(accountService.updateAccount(account));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // get all bikes
    @GetMapping("/bikes")
    public ResponseEntity<List<Bike>> getAllBikes() {
        return ResponseEntity.ok(bikeService.getAllBikes());
    }

    // Cart zum eingeloggten Account hinzufügen
    @PostMapping("/cart")
    public ResponseEntity<Account> addCart(@RequestBody Cart cart) {
        account.addCart(cart);
        return ResponseEntity.ok(accountService.updateAccount(account));
    }


    // Bikepart zu existierendem Cart eines Accounts hinzufügen
    // eher cart/addBikepartToCart
    @PostMapping("/cart/bikeparts/{bikepartId}")
    public ResponseEntity<?> addBikePartToCart(
            @PathVariable Long bikepartId,
            @RequestParam(defaultValue = "1") Integer quantity) {

        // TODO: Bikepart muss zu dem account gehören
        cartService.addBikepartToCart(bikepartId, quantity);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bike/{bikeId}")
    public ResponseEntity<List<Bikepart>> getAllBikeparts(
            @PathVariable Long bikeId)  {
        // TODO later: check: "gehört das Bike dem eingeloggten User?". dann muss Account nicht übergeben werden
        return ResponseEntity.ok(bikeService.getAllBikeparts(bikeId));
    }

    @GetMapping("/bike/{bikeId}/bikepart/{bikepartId}/searchPriceBikeComponents")
    public ResponseEntity<?> searchPriceBikeComponents(
            @PathVariable Long bikeId,
            @PathVariable Long bikepartId) {
        Bike bikeById = bikeService.getBikeById(bikeId);
        Bikepart bikepartById = bikeService.getBikepartById(bikepartId);
        if (!bikeById.getBikeparts().stream().toList().contains(bikepartById)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Bikepart "+bikepartId + "gehört nicht zum Bike "+bikeId));
        }

        ScrapingResult result = cartService.searchPriceBikeComponents(bikepartById);
        return switch (result.status()) {
            case SUCCESS -> ResponseEntity.ok(result.offers());
            case NO_RESULTS -> ResponseEntity.ok(Map.of("message", "Keine Angebote gefunden", "offers", List.of()));
            case ERROR -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Shop konnte nicht erreicht werden", "details", result.errorMessage()));
        };
    }

}
