package com.bikeparts.controller;

import com.bikeparts.entity.*;
import com.bikeparts.price.service.ScrapingResult;
import com.bikeparts.service.BikeService;
import com.bikeparts.service.CartService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BikeViewController - Unit Tests")
class BikeViewControllerTest {

    @Mock
    private BikeService bikeService;

    @Mock
    private CartService cartService;

    @Mock
    private Account account;

    @InjectMocks
    private BikeViewController bikeViewController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");
        mockMvc = MockMvcBuilders.standaloneSetup(bikeViewController)
                .setViewResolvers(viewResolver)
                .build();
    }


    // =========================================================
    // GET /bikes
    // =========================================================

    @Nested
    @DisplayName("showBikes() - GET /bikes")
    class ShowBikes {

        @Test
        @DisplayName("returns bikes-list view with bikes in model")
        void showBikes_returnsBikesListView() throws Exception {
            Bike bike = new Bike();
            bike.setId(77L);
            bike.setBrand("Merida");
            when(bikeService.getAllBikes()).thenReturn(List.of(bike));

            mockMvc.perform(get("/bikes"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bikes-list"))
                    .andExpect(model().attributeExists("bikes"));

            verify(bikeService).getAllBikes();
        }

        @Test
        @DisplayName("empty bike list -> bikes-list view with empty attribute")
        void showBikes_emptyList_returnsBikesListView() throws Exception {
            when(bikeService.getAllBikes()).thenReturn(List.of());

            mockMvc.perform(get("/bikes"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bikes-list"))
                    .andExpect(model().attribute("bikes", List.of()));
        }
    }


    // =========================================================
    // GET /bikes/{bikeId}
    // =========================================================

    @Nested
    @DisplayName("showBike() - GET /bikes/{bikeId}")
    class ShowBike {

        @Test
        @DisplayName("bike found -> bike-details view with bike in model")
        void showBike_found_returnsBikeDetailsView() throws Exception {
            Bike bike = new Bike();
            bike.setId(77L);
            bike.setBrand("Merida");
            when(bikeService.getBikeById(77L)).thenReturn(bike);

            mockMvc.perform(get("/bikes/77"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bike-details"))
                    .andExpect(model().attribute("bike", bike));

            verify(bikeService).getBikeById(77L);
        }
    }


    // =========================================================
    // GET /bikes/{bikeId}/bikeparts
    // =========================================================

    @Nested
    @DisplayName("showBikeparts() - GET /bikes/{bikeId}/bikeparts")
    class ShowBikeparts {

        @Test
        @DisplayName("account matches bike owner -> bikeparts-list view")
        void showBikeparts_accountMatches_returnsBikepartsListView() throws Exception {
            Account bikeOwner = new Account();
            bikeOwner.setId(1L);
            Bike bike = new Bike();
            bike.setId(77L);
            bike.setAccount(bikeOwner);
            Bikepart bikepart = new Bikepart();
            when(bikeService.getBikeById(77L)).thenReturn(bike);
            when(account.getId()).thenReturn(1L);
            when(bikeService.getAllBikeparts(77L)).thenReturn(List.of(bikepart));

            mockMvc.perform(get("/bikes/77/bikeparts"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bikeparts-list"))
                    .andExpect(model().attributeExists("bikeparts"))
                    .andExpect(model().attribute("accountId", 1L));

            verify(bikeService).getAllBikeparts(77L);
        }

        @Test
        @DisplayName("account does not match bike owner -> throws RuntimeException")
        void showBikeparts_accountMismatch_throwsException() {
            Account bikeOwner = new Account();
            bikeOwner.setId(99L);
            Bike bike = new Bike();
            bike.setId(77L);
            bike.setAccount(bikeOwner);
            when(bikeService.getBikeById(77L)).thenReturn(bike);
            when(account.getId()).thenReturn(1L);

            assertThrows(Exception.class, () ->
                    mockMvc.perform(get("/bikes/77/bikeparts")));
        }
    }


    // =========================================================
    // GET /bikeparts/{id}
    // =========================================================

    @Nested
    @DisplayName("showBikepart() - GET /bikeparts/{id}")
    class ShowBikepart {

        @Test
        @DisplayName("bikepart found -> bikepart-details view with bikepart in model")
        void showBikepart_found_returnsBikePartDetailsView() throws Exception {
            Bikepart bikepart = new Bikepart();
            when(bikeService.getBikepartById(5L)).thenReturn(bikepart);

            mockMvc.perform(get("/bikeparts/5"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bikepart-details"))
                    .andExpect(model().attribute("bikepart", bikepart));

            verify(bikeService).getBikepartById(5L);
        }
    }


    // =========================================================
    // POST /bikeparts/{id}/addBikepartToCart
    // =========================================================

    @Nested
    @DisplayName("addBikepartToCart() - POST /bikeparts/{id}/addBikepartToCart")
    class AddBikepartToCart {

        @Test
        @DisplayName("bikepart added with default quantity -> bikeparts-list view")
        void addBikepartToCart_defaultQuantity_returnsBikepartsListView() throws Exception {
            Bike bike = new Bike();
            bike.setId(3L);
            Bikepart bikepart = new Bikepart();
            bikepart.setBike(bike);
            when(bikeService.getBikepartById(2L)).thenReturn(bikepart);
            when(bikeService.getAllBikeparts(3L)).thenReturn(List.of(bikepart));
            when(account.getId()).thenReturn(1L);

            mockMvc.perform(post("/bikeparts/2/addBikepartToCart"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bikeparts-list"))
                    .andExpect(model().attributeExists("bikeparts"))
                    .andExpect(model().attribute("accountId", 1L));

            verify(cartService).addBikepartToCart(2L, 1);
        }

        @Test
        @DisplayName("custom quantity param -> passed to cartService")
        void addBikepartToCart_withQuantity_passesQuantityToService() throws Exception {
            Bike bike = new Bike();
            bike.setId(3L);
            Bikepart bikepart = new Bikepart();
            bikepart.setBike(bike);
            when(bikeService.getBikepartById(2L)).thenReturn(bikepart);
            when(bikeService.getAllBikeparts(3L)).thenReturn(List.of(bikepart));
            when(account.getId()).thenReturn(1L);

            mockMvc.perform(post("/bikeparts/2/addBikepartToCart").param("quantity", "3"))
                    .andExpect(status().isOk());

            verify(cartService).addBikepartToCart(2L, 3);
        }
    }


    // =========================================================
    // GET /cart/
    // =========================================================

    @Nested
    @DisplayName("showCart() - GET /cart/")
    class ShowCart {

        @Test
        @DisplayName("returns cart-cartItems-list view with cart in model")
        void showCart_returnsCartView() throws Exception {
            Cart cart = new Cart();
            when(account.getCart()).thenReturn(cart);

            mockMvc.perform(get("/cart"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("cart"))
                    .andExpect(model().attribute("cart", cart));
        }
    }


    // =========================================================
    // GET /cart/cartItem/{id}/searchPriceBikeComponents
    // =========================================================

    @Nested
    @DisplayName("searchPriceBikeComponents() - GET /cart/cartItem/{id}/searchPrice")
    class SearchPrice {

        @Test
        @DisplayName("SUCCESS -> price-search-result view with productOffers in model")
        void search_success_returnsPriceSearchResultView() throws Exception {
            Bikepart bikepart = new Bikepart();
            bikepart.setName("Shimano XT Kette");
            CartItem cartItem = new CartItem();
            cartItem.setId(1L);
            cartItem.setBikepart(bikepart);
            String shopName = "bike-X";
            when(cartService.getCartItem(1L)).thenReturn(cartItem);
            when(cartService.searchPrice(bikepart))
                    .thenReturn(List.of(ScrapingResult.success(List.of(), shopName)));

            mockMvc.perform(get("/cart/cartItem/1/searchPrice"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("price-search-result"))
                    .andExpect(model().attributeExists("cartItem"))
                    .andExpect(model().attributeExists("bikepartName"))
                    .andExpect(model().attributeExists("scrapingResults"));
            verify(cartService).searchPrice(bikepart);
        }

        @Test
        @DisplayName("CartItem not found -> throws EntityNotFoundException")
        void search_cartItemNotFound_throwsException() {
            when(cartService.getCartItem(99L))
                    .thenThrow(new EntityNotFoundException("CartItem nicht gefunden: 99"));

            assertThrows(Exception.class, () ->
                    mockMvc.perform(get("/cart/cartItem/99/searchPrice")));
        }
    }
}
