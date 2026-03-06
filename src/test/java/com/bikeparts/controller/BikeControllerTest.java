package com.bikeparts.controller;

import com.bikeparts.entity.Account;
import com.bikeparts.entity.Bike;
import com.bikeparts.entity.Cart;
import com.bikeparts.repository.CartItemRepository;
import com.bikeparts.service.AccountService;
import com.bikeparts.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BikeController – Unit Tests")
class BikeControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private BikeController bikeController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bikeController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    // =========================================================
    // GET /api/accounts
    // =========================================================

    //TODO: echten account generieren

    @Nested
    @DisplayName("getAllAccounts() – GET /api/accounts")
    class GetAllAccounts {

        @Test
        @DisplayName("gibt alle Accounts zurück – 200 OK")
        void getAllAccounts_returnsListAnd200() throws Exception {
            Account a1 = new Account();
            a1.setEmail("ellen@bikeparts.de");
            Account a2 = new Account();
            a2.setEmail("max@bikeparts.de");
            when(accountService.findAll()).thenReturn(List.of(a1, a2));

            mockMvc.perform(get("/api/accounts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));

            verify(accountService).findAll();
        }

        @Test
        @DisplayName("leere Liste -> 200 OK mit leerem Array")
        void getAllAccounts_emptyList_returns200() throws Exception {
            when(accountService.findAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/accounts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Service wirft Exception -> wird weitergeleitet")
        void getAllAccounts_serviceThrows_propagatesException() {
            when(accountService.findAll()).thenThrow(new RuntimeException("DB nicht erreichbar"));

            assertThrows(Exception.class, () ->
                    mockMvc.perform(get("/api/accounts")));
        }
    }


    // =========================================================
    // GET /api/accounts/{id}
    // =========================================================

    @Nested
    @DisplayName("getAccountById() – GET /api/accounts/{id}")
    class GetAccountById {

        @Test
        @DisplayName("Account gefunden -> 200 OK mit Account")
        void getAccountById_found_returns200() throws Exception {
            Account account = new Account();
            account.setEmail("ellen@bikeparts.de");
            when(accountService.findById(1L)).thenReturn(Optional.of(account));

            mockMvc.perform(get("/api/accounts/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("ellen@bikeparts.de"));
        }

        @Test
        @DisplayName("Account nicht gefunden -> 404 Not Found")
        void getAccountById_notFound_returns404() throws Exception {
            when(accountService.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/accounts/99"))
                    .andExpect(status().isNotFound());
        }
    }


    // =========================================================
    // POST /api/accounts
    // =========================================================

    @Nested
    @DisplayName("createAccount() – POST /api/accounts")
    class CreateAccount {

        @Test
        @DisplayName("gültige Email -> 201 Created")
        void createAccount_validEmail_returns201() throws Exception {
            Account account = new Account();
            account.setEmail("ellen@bikeparts.de");
            when(accountService.createAccount(any(Account.class))).thenReturn(account);

            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(account)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("ellen@bikeparts.de"));
        }

        @Test
        @DisplayName("Email fehlt -> 400 Bad Request")
        void createAccount_missingEmail_returns400() throws Exception {
            Account account = new Account();
            account.setEmail(null);

            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(account)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Email darf nicht leer sein"));
        }

        @Test
        @DisplayName("Email leer -> 400 Bad Request")
        void createAccount_blankEmail_returns400() throws Exception {
            Account account = new Account();
            account.setEmail("  ");

            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(account)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Email darf nicht leer sein"));
        }

        @Test
        @DisplayName("ungültiges Email-Format -> 400 Bad Request")
        void createAccount_invalidEmailFormat_returns400() throws Exception {
            Account account = new Account();
            account.setEmail("kein-email");

            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(account)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Email hat ungültiges Format"));
        }
    }


    // =========================================================
    // PUT /api/accounts/{id}
    // =========================================================

    @Nested
    @DisplayName("updateAccount() – PUT /api/accounts/{id}")
    class UpdateAccount {

        @Test
        @DisplayName("erfolgreiches Update -> 200 OK")
        void updateAccount_success_returns200() throws Exception {
            Account account = new Account();
            account.setEmail("ellen@bikeparts.de");
            when(accountService.updateAccount(any(Account.class))).thenReturn(account);

            mockMvc.perform(put("/api/accounts/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(account)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("ellen@bikeparts.de"));
        }

        @Test
        @DisplayName("Account nicht gefunden -> 404 Not Found")
        void updateAccount_notFound_returns404() throws Exception {
            Account account = new Account();
            account.setEmail("ellen@bikeparts.de");
            when(accountService.updateAccount(any(Account.class)))
                    .thenThrow(new RuntimeException("Account nicht gefunden"));

            mockMvc.perform(put("/api/accounts/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(account)))
                    .andExpect(status().isNotFound());
        }
    }


    // =========================================================
    // DELETE /api/accounts/{id}
    // =========================================================

    @Nested
    @DisplayName("deleteAccount() – DELETE /api/accounts/{id}")
    class DeleteAccount {

        @Test
        @DisplayName("Account gelöscht -> 204 No Content")
        void deleteAccount_returns204() throws Exception {
            doNothing().when(accountService).deleteAccount(1L);

            mockMvc.perform(delete("/api/accounts/1"))
                    .andExpect(status().isNoContent());

            verify(accountService).deleteAccount(1L);
        }

        @Test
        @DisplayName("Service wirft Exception -> wird weitergeleitet")
        void deleteAccount_serviceThrows_propagatesException() {
            doThrow(new RuntimeException("Account nicht gefunden"))
                    .when(accountService).deleteAccount(99L);

            assertThrows(Exception.class, () ->
                    mockMvc.perform(delete("/api/accounts/99")));
        }
    }


    // =========================================================
    // POST /api/accounts/{id}/bikes
    // =========================================================

    @Nested
    @DisplayName("addBike() – POST /api/accounts/{id}/bikes")
    class AddBike {

        @Test
        @DisplayName("Account gefunden -> Bike hinzugefügt, 200 OK")
        void addBike_accountFound_returns200() throws Exception {
            Account account = new Account();
            account.setEmail("ellen@bikeparts.de");
            Bike bike = new Bike();
            when(accountService.findById(1L)).thenReturn(Optional.of(account));
            when(accountService.updateAccount(account)).thenReturn(account);

            mockMvc.perform(post("/api/accounts/1/bikes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bike)))
                    .andExpect(status().isOk());

            verify(accountService).updateAccount(account);
        }

        @Test
        @DisplayName("Account nicht gefunden -> 404 Not Found")
        void addBike_accountNotFound_returns404() throws Exception {
            when(accountService.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(post("/api/accounts/99/bikes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new Bike())))
                    .andExpect(status().isNotFound());
        }
    }


    // =========================================================
    // POST /api/accounts/{id}/cart
    // =========================================================

    @Nested
    @DisplayName("addCart() – POST /api/accounts/{id}/cart")
    class AddCart {

        @Test
        @DisplayName("Account gefunden -> Cart gesetzt, 200 OK")
        void addCart_accountFound_returns200() throws Exception {
            Account account = new Account();
            account.setEmail("ellen@bikeparts.de");
            Cart cart = new Cart();
            cart.setName("Frühjahrs-Wartung 2026");
            when(accountService.findById(1L)).thenReturn(Optional.of(account));
            when(accountService.updateAccount(account)).thenReturn(account);

            mockMvc.perform(post("/api/accounts/1/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cart)))
                    .andExpect(status().isOk());

            verify(accountService).updateAccount(account);
            assertNotNull(account.getCart());
        }


        @Test
        @DisplayName("Account nicht gefunden -> 404 Not Found")
        void addCart_accountNotFound_returns404() throws Exception {
            when(accountService.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(post("/api/accounts/99/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new Cart())))
                    .andExpect(status().isNotFound());
        }
    }


    // =========================================================
    // POST /api/accounts/{id}/cart/bikeparts/{bikepartId}
    // =========================================================

    @Nested
    @DisplayName("addBikePart() – POST /api/accounts/{id}/cart/bikeparts/{bikepartId}")
    class AddBikePart {

        @Test
        @DisplayName("Account und Cart vorhanden -> 204 No Content")
        void addBikePart_accountAndCartFound_returns204() throws Exception {
            Account account = new Account();
            Cart cart = new Cart();
            cart.setId(10L);
            account.setCart(cart);
            when(accountService.findById(1L)).thenReturn(Optional.of(account));

            mockMvc.perform(post("/api/accounts/1/cart/bikeparts/2"))
                    .andExpect(status().isNoContent());

            verify(cartService).addBikepartToCart(2L, 10L);
        }

        @Test
        @DisplayName("Account nicht gefunden -> 404 mit Fehlermeldung")
        void addBikePart_accountNotFound_returns404WithMessage() throws Exception {
            when(accountService.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(post("/api/accounts/99/cart/bikeparts/1"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Account 99 nicht gefunden"));
        }

        @Test
        @DisplayName("Account hat keinen Cart -> 404 mit Fehlermeldung")
        void addBikePart_noCart_returns404WithMessage() throws Exception {
            Account account = new Account();
            account.setCart(null);
            when(accountService.findById(1L)).thenReturn(Optional.of(account));

            mockMvc.perform(post("/api/accounts/1/cart/bikeparts/2"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Account 1 hat keinen Warenkorb"));
        }

        @Test
        @DisplayName("CartService wirft Exception -> wird weitergeleitet")
        void addBikePart_serviceThrows_propagatesException() {
            Account account = new Account();
            Cart cart = new Cart();
            cart.setId(10L);
            account.setCart(cart);
            when(accountService.findById(1L)).thenReturn(Optional.of(account));
            doThrow(new RuntimeException("Bikepart nicht gefunden"))
                    .when(cartService).addBikepartToCart(99L, 10L);

            assertThrows(Exception.class, () ->
                    mockMvc.perform(post("/api/accounts/1/cart/bikeparts/99")));
        }
    }
}
