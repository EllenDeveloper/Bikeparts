package com.bikeparts.controller;

import com.bikeparts.entity.Account;
import com.bikeparts.entity.Bike;
import com.bikeparts.entity.Bikepart;
import com.bikeparts.entity.Cart;
import com.bikeparts.price.entity.ProductOffer;
import com.bikeparts.price.enums.FetchMethod;
import com.bikeparts.price.service.ScrapingResult;
import com.bikeparts.repository.CartItemRepository;
import com.bikeparts.service.AccountService;
import com.bikeparts.service.BikeService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private CartService cartService;

    @Mock
    private BikeService bikeService;

    @Mock
    private Account account;

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
    // POST /api/accounts/cart
    // =========================================================

    @Nested
    @DisplayName("addCart() - POST /api/accounts/cart")
    class AddCart {

        @Test
        @DisplayName("Cart set on session account -> 200 OK")
        void addCart_returns200() throws Exception {
            Account updatedAccount = new Account();
            updatedAccount.setEmail("ellen@bikeparts.de");
            Cart cart = new Cart();
            cart.setName("Frühjahrs-Wartung 2026");
            when(accountService.updateAccount(account)).thenReturn(updatedAccount);

            mockMvc.perform(post("/api/accounts/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cart)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("ellen@bikeparts.de"));

            verify(account).addCart(any(Cart.class));
            verify(accountService).updateAccount(account);
        }

        @Test
        @DisplayName("AccountService wirft Exception -> wird weitergeleitet")
        void addCart_serviceThrows_propagatesException() {
            when(accountService.updateAccount(account))
                    .thenThrow(new RuntimeException("DB nicht erreichbar"));

            assertThrows(Exception.class, () ->
                    mockMvc.perform(post("/api/accounts/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new Cart()))));
        }
    }


    // =========================================================
    // POST /api/accounts/cart/bikeparts/{bikepartId}
    // =========================================================

    @Nested
    @DisplayName("addBikePartToCart() - POST /api/accounts/cart/bikeparts/{bikepartId}")
    class AddBikePart {

        @Test
        @DisplayName("valid bikepartId -> 204 No Content")
        void addBikePartToCart_validRequest_returns204() throws Exception {
            mockMvc.perform(post("/api/accounts/cart/bikeparts/2"))
                    .andExpect(status().isNoContent());

            verify(cartService).addBikepartToCart(2L, 1);
        }

        @Test
        @DisplayName("custom quantity param -> passed to cartService")
        void addBikePartToCart_withQuantity_passesQuantityToService() throws Exception {
            mockMvc.perform(post("/api/accounts/cart/bikeparts/2")
                            .param("quantity", "3"))
                    .andExpect(status().isNoContent());

            verify(cartService).addBikepartToCart(2L, 3);
        }

        @Test
        @DisplayName("CartService wirft Exception -> wird weitergeleitet")
        void addBikePartToCart_serviceThrows_propagatesException() {
            doThrow(new RuntimeException("Bikepart nicht gefunden"))
                    .when(cartService).addBikepartToCart(99L, 1);

            assertThrows(Exception.class, () ->
                    mockMvc.perform(post("/api/accounts/cart/bikeparts/99")));
        }
    }


    // =========================================================
    // GET /api/accounts/bike/{bikeId}/bikepart/{bikepartId}/searchPriceBikeComponents
    // =========================================================

    @Nested
    @DisplayName("searchPriceBikeComponents() – GET /api/accounts/bike/{bikeId}/bikepart/{bikepartId}/searchPriceBikeComponents")
    class SearchPriceBikeComponents {

        private Bike bike;
        private Bikepart bikepart;

        @BeforeEach
        void setUpBikeAndBikepart() {
            bikepart = new Bikepart();
            bikepart.setId(10L);
            bike = new Bike();
            bike.addBikepart(bikepart);
            when(bikeService.getBikeById(1L)).thenReturn(bike);
        }

        @Test
        @DisplayName("SUCCESS -> 200 OK with offers list")
        void search_success_returns200WithOffers() throws Exception {
            when(bikeService.getBikepartById(10L)).thenReturn(bikepart);
            ProductOffer offer = ProductOffer.builder()
                    .productName("Shimano XT Kette")
                    .price(new BigDecimal("39.99"))
                    .productUrl("https://www.bike-components.de/de/shimano-xt-kette")
                    .inStock(true)
                    .shopName("bike-components.de")
                    .shopId(1L)
                    .source(FetchMethod.WEB_SCRAPING)
                    .fetchedAt(LocalDateTime.now())
                    .searchQuery("Shimano XT Kette")
                    .build();
            when(cartService.searchPriceBikeComponents(bikepart))
                    .thenReturn(ScrapingResult.success(List.of(offer)));

            mockMvc.perform(get("/api/accounts/bike/1/bikepart/10/searchPriceBikeComponents"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].productName").value("Shimano XT Kette"));
        }

        @Test
        @DisplayName("NO_RESULTS -> 200 OK with message")
        void search_noResults_returns200WithMessage() throws Exception {
            when(bikeService.getBikepartById(10L)).thenReturn(bikepart);
            when(cartService.searchPriceBikeComponents(bikepart))
                    .thenReturn(ScrapingResult.noResults());

            mockMvc.perform(get("/api/accounts/bike/1/bikepart/10/searchPriceBikeComponents"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Keine Angebote gefunden"));
        }

        @Test
        @DisplayName("ERROR -> 503 Service Unavailable with error details")
        void search_error_returns503() throws Exception {
            when(bikeService.getBikepartById(10L)).thenReturn(bikepart);
            when(cartService.searchPriceBikeComponents(bikepart))
                    .thenReturn(ScrapingResult.error("Connection refused"));

            mockMvc.perform(get("/api/accounts/bike/1/bikepart/10/searchPriceBikeComponents"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error").value("Shop konnte nicht erreicht werden"))
                    .andExpect(jsonPath("$.details").value("Connection refused"));
        }

        @Test
        @DisplayName("Bikepart gehört nicht zum Bike -> 400 Bad Request")
        // my checked
        void search_bikepartNotInBike_returns400() throws Exception {
            Bikepart otherBikepart = new Bikepart();
            otherBikepart.setId(99L);
            when(bikeService.getBikepartById(99L)).thenReturn(otherBikepart);

            mockMvc.perform(get("/api/accounts/bike/1/bikepart/99/searchPriceBikeComponents"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }
    }
}
