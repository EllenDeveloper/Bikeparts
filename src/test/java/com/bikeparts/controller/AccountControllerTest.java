package com.bikeparts.controller;

import com.bikeparts.entity.Account;
import com.bikeparts.entity.Bike;
import com.bikeparts.enums.BikeType;
import com.bikeparts.enums.UserRole;
import com.bikeparts.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit Tests für AccountController
 *
 * Testet: - GET /api/accounts - Alle Accounts abrufen - GET /api/accounts/{id}
 * - Account by ID abrufen - POST /api/accounts - Account erstellen - PUT
 * /api/accounts/{id} - Account aktualisieren - DELETE /api/accounts/{id} -
 * Account löschen - POST /api/accounts/{id}/bikes - Bike zu Account hinzufügen
 *
 * Verwendet @MockitoBean (Spring Boot 3.4+) statt veraltetes @MockBean
 */
@WebMvcTest(AccountController.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountController Unit Tests")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    private Account testAccount1;
    private Account testAccount2;
    private Bike testBike;

    @BeforeEach
    void setUp() {
        // Test Account 1
        testAccount1 = new Account();
        testAccount1.setId(1L);
        testAccount1.setEmail("max@example.com");
        testAccount1.setFirstName("Max");
        testAccount1.setLastName("Mustermann");
        testAccount1.setRole(UserRole.USER);
        testAccount1.setIsActive(true);
        testAccount1.setCreatedAt(LocalDateTime.now());
        testAccount1.setUpdatedAt(LocalDateTime.now());

        // Test Account 2
        testAccount2 = new Account();
        testAccount2.setId(2L);
        testAccount2.setEmail("anna@example.com");
        testAccount2.setFirstName("Anna");
        testAccount2.setLastName("Admin");
        testAccount2.setRole(UserRole.ADMIN);
        testAccount2.setIsActive(true);
        testAccount2.setCreatedAt(LocalDateTime.now());
        testAccount2.setUpdatedAt(LocalDateTime.now());

        // Test Bike
        testBike = new Bike();
        testBike.setId(1L);
        testBike.setType(BikeType.MTB);
        testBike.setName("Mountain Bike Pro");
        testBike.setBrand("Scott");
        testBike.setIsEbike(false); 
    }

    // ==================== GET /api/accounts ====================
    @Test
    @DisplayName("GET /api/accounts - sollte alle Accounts zurückgeben")
    void getAllAccounts_ShouldReturnListOfAccounts() throws Exception {
        // Arrange
        List<Account> accounts = Arrays.asList(testAccount1, testAccount2);
        when(accountService.findAll()).thenReturn(accounts);

        // Act & Assert
        mockMvc.perform(get("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].email", is("max@example.com")))
                .andExpect(jsonPath("$[0].firstName", is("Max")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].email", is("anna@example.com")))
                .andExpect(jsonPath("$[1].role", is("ADMIN")));

        verify(accountService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/accounts - sollte leere Liste zurückgeben wenn keine Accounts vorhanden")
    void getAllAccounts_ShouldReturnEmptyList_WhenNoAccounts() throws Exception {
        // Arrange
        when(accountService.findAll()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(accountService, times(1)).findAll();
    }

    // ==================== GET /api/accounts/{id} ====================
    @Test
    @DisplayName("GET /api/accounts/{id} - sollte Account zurückgeben wenn vorhanden")
    void getAccountById_ShouldReturnAccount_WhenExists() throws Exception {
        // Arrange
        when(accountService.findById(1L)).thenReturn(Optional.of(testAccount1));

        // Act & Assert
        mockMvc.perform(get("/api/accounts/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("max@example.com")))
                .andExpect(jsonPath("$.firstName", is("Max")))
                .andExpect(jsonPath("$.lastName", is("Mustermann")))
                .andExpect(jsonPath("$.role", is("USER")))
                .andExpect(jsonPath("$.isActive", is(true)));

        verify(accountService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /api/accounts/{id} - sollte 404 zurückgeben wenn Account nicht existiert")
    void getAccountById_ShouldReturn404_WhenNotFound() throws Exception {
        // Arrange
        when(accountService.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/accounts/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).findById(999L);
    }

    // ==================== POST /api/accounts ====================
    @Test
    @DisplayName("POST /api/accounts - sollte neuen Account erstellen")
    void createAccount_ShouldReturnCreatedAccount() throws Exception {
        // Arrange
        Account newAccount = new Account();
        newAccount.setEmail("neu@example.com");
        newAccount.setFirstName("Neuer");
        newAccount.setLastName("User");

        Account savedAccount = new Account();
        savedAccount.setId(3L);
        savedAccount.setEmail("neu@example.com");
        savedAccount.setFirstName("Neuer");
        savedAccount.setLastName("User");
        savedAccount.setRole(UserRole.USER);
        savedAccount.setIsActive(true);
        savedAccount.setCreatedAt(LocalDateTime.now());

        when(accountService.createAccount(any(Account.class))).thenReturn(savedAccount);

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newAccount)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.email", is("neu@example.com")))
                .andExpect(jsonPath("$.role", is("USER")))
                .andExpect(jsonPath("$.isActive", is(true)));

        verify(accountService, times(1)).createAccount(any(Account.class));
    }

    @Test
    @DisplayName("POST /api/accounts - sollte 400 zurückgeben bei ungültigen Daten")
    void createAccount_ShouldReturn400_WhenInvalidData() throws Exception {
        // Arrange - Account ohne erforderliche Felder
        Account invalidAccount = new Account();
        // Keine email gesetzt

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidAccount)))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).createAccount(any(Account.class));
    }

    // ==================== PUT /api/accounts/{id} ====================
    @Test
    @DisplayName("PUT /api/accounts/{id} - sollte Account aktualisieren")
    void updateAccount_ShouldReturnUpdatedAccount() throws Exception {
        // Arrange
        Account updatedAccount = new Account();
        updatedAccount.setId(1L);
        updatedAccount.setEmail("max.neu@example.com"); // Geänderte Email
        updatedAccount.setFirstName("Maximilian"); // Geänderter Vorname
        updatedAccount.setLastName("Mustermann");
        updatedAccount.setRole(UserRole.USER);
        updatedAccount.setIsActive(true);

        when(accountService.updateAccount(any(Account.class))).thenReturn(updatedAccount);

        // Act & Assert
        mockMvc.perform(put("/api/accounts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedAccount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("max.neu@example.com")))
                .andExpect(jsonPath("$.firstName", is("Maximilian")));

        verify(accountService, times(1)).updateAccount(any(Account.class));
    }

    @Test
    @DisplayName("PUT /api/accounts/{id} - sollte 404 zurückgeben wenn Account nicht existiert")
    void updateAccount_ShouldReturn404_WhenNotFound() throws Exception {
        // Arrange
        Account accountDetails = new Account();
        accountDetails.setId(999L);
        accountDetails.setEmail("nichtvorhanden@test.de");

        when(accountService.updateAccount(any(Account.class)))
                .thenThrow(new RuntimeException("Account not found"));

        // Act & Assert
        mockMvc.perform(put("/api/accounts/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountDetails)))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).updateAccount(any(Account.class));
    }

    // ==================== DELETE /api/accounts/{id} ====================
    @Test
    @DisplayName("DELETE /api/accounts/{id} - sollte Account löschen")
    void deleteAccount_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(accountService).deleteAccount(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/accounts/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(accountService, times(1)).deleteAccount(1L);
    }

    @Test
    @DisplayName("DELETE /api/accounts/{id} - sollte auch bei nicht existierendem Account 204 zurückgeben")
    void deleteAccount_ShouldReturnNoContent_EvenWhenNotFound() throws Exception {
        // Arrange
        doNothing().when(accountService).deleteAccount(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/accounts/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(accountService, times(1)).deleteAccount(999L);
    }

    // ==================== POST /api/accounts/{id}/bikes ====================
    @Test
    @DisplayName("POST /api/accounts/{id}/bikes - sollte Bike zu Account hinzufügen")
    void addBike_ShouldAddBikeToAccount() throws Exception {
        // Arrange
        when(accountService.findById(1L)).thenReturn(Optional.of(testAccount1));

        Account updatedAccount = new Account();
        updatedAccount.setId(1L);
        updatedAccount.setEmail("max@example.com");
        updatedAccount.addBike(testBike);

        when(accountService.updateAccount(any(Account.class))).thenReturn(updatedAccount);

        // Act & Assert
        mockMvc.perform(post("/api/accounts/1/bikes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBike)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("max@example.com")));

        verify(accountService, times(1)).findById(1L);
        verify(accountService, times(1)).updateAccount(any(Account.class));
    }

    @Test
    @DisplayName("POST /api/accounts/{id}/bikes - sollte 404 zurückgeben wenn Account nicht existiert")
    void addBike_ShouldReturn404_WhenAccountNotFound() throws Exception {
        // Arrange
        when(accountService.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/accounts/999/bikes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBike)))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).findById(999L);
        verify(accountService, never()).updateAccount(any(Account.class));
    }

    // ==================== Edge Cases & Error Handling ====================
    @Test
    @DisplayName("GET /api/accounts/{id} - sollte mit negativer ID umgehen können")
    void getAccountById_ShouldHandle_NegativeId() throws Exception {
        // Arrange
        when(accountService.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/accounts/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).findById(-1L);
    }

    @Test
    @DisplayName("POST /api/accounts - sollte JSON Parse Error abfangen")
    void createAccount_ShouldHandle_InvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).createAccount(any(Account.class));
    }

    @Test
    @DisplayName("PUT /api/accounts/{id} - sollte Validation Error abfangen")
    void updateAccount_ShouldHandle_ValidationError() throws Exception {
        // Arrange - Account mit ungültiger Email
        Account invalidAccount = new Account();
        invalidAccount.setId(1L);
        invalidAccount.setEmail("ungueltige-email"); // Keine gültige Email

        // Act & Assert
        mockMvc.perform(put("/api/accounts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidAccount)))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).updateAccount(any(Account.class));
    }

    @Test
    @DisplayName("GET /api/accounts/{id} - sollte alle Account-Felder korrekt zurückgeben")
    void getAccountById_ShouldReturnAllFields() throws Exception {
        // Arrange
        Account fullAccount = new Account();
        fullAccount.setId(5L);
        fullAccount.setEmail("full@example.com");
        fullAccount.setFirstName("Full");
        fullAccount.setLastName("Account");
        fullAccount.setRole(UserRole.USER);
        fullAccount.setIsActive(true);
        fullAccount.setCreatedAt(LocalDateTime.now());
        fullAccount.setUpdatedAt(LocalDateTime.now());

        when(accountService.findById(5L)).thenReturn(Optional.of(fullAccount));

        // Act & Assert
        mockMvc.perform(get("/api/accounts/5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.email", is("full@example.com")))
                .andExpect(jsonPath("$.firstName", is("Full")))
                .andExpect(jsonPath("$.lastName", is("Account")))
                .andExpect(jsonPath("$.role", is("USER")))
                .andExpect(jsonPath("$.isActive", is(true)))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        verify(accountService, times(1)).findById(5L);
    }
}
