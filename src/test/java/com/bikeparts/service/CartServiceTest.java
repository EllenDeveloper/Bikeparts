package com.bikeparts.service;

import com.bikeparts.entity.Account;
import com.bikeparts.entity.Bikepart;
import com.bikeparts.entity.Cart;
import com.bikeparts.entity.CartItem;
import com.bikeparts.repository.AccountRepository;
import com.bikeparts.repository.BikepartRepository;
import com.bikeparts.repository.CartItemRepository;
import com.bikeparts.repository.CartRepository;
import org.junit.jupiter.api.DisplayName;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService – Unit Tests")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private BikepartRepository bikepartRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private Account account;

    @InjectMocks
    private CartService cartService;


    // =========================================================
    // createCart
    // =========================================================

    @Nested
    @DisplayName("createCart()")
    class CreateCart {

        @Test
        @DisplayName("speichert Cart und gibt ihn zurück")
        void createCart_savesAndReturnsCart() {
            Cart cart = new Cart();
            cart.setName("Frühjahrs-Wartung 2026");
            when(cartRepository.save(cart)).thenReturn(cart);

            Cart result = cartService.createCart(cart);

            assertNotNull(result);
            assertEquals("Frühjahrs-Wartung 2026", result.getName());
            verify(cartRepository).save(cart);
        }

        @Test
        @DisplayName("gibt null zurück wenn Repository null liefert")
        void createCart_repositoryReturnsNull_returnsNull() {
            Cart cart = new Cart();
            when(cartRepository.save(cart)).thenReturn(null);

            Cart result = cartService.createCart(cart);

            assertNull(result);
            verify(cartRepository).save(cart);
        }

        @Test
        @DisplayName("Repository wirft Exception -> wird weitergeleitet")
        void createCart_repositoryThrows_propagatesException() {
            Cart cart = new Cart();
            when(cartRepository.save(cart)).thenThrow(new RuntimeException("DB Fehler"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> cartService.createCart(cart));

            assertEquals("DB Fehler", ex.getMessage());
        }
    }


    // =========================================================
    // deleteCart
    // =========================================================

    @Nested
    @DisplayName("deleteCart()")
    class DeleteCart {

        @Test
        @DisplayName("ruft deleteById mit korrekter ID auf")
        void deleteCart_callsDeleteById() {
            cartService.deleteCart(1L);

            verify(cartRepository).deleteById(1L);
        }

        @Test
        @DisplayName("ruft deleteById genau einmal auf")
        void deleteCart_callsDeleteByIdExactlyOnce() {
            cartService.deleteCart(42L);

            verify(cartRepository, times(1)).deleteById(42L);
            verifyNoMoreInteractions(cartRepository);
        }

        @Test
        @DisplayName("Repository wirft Exception -> wird weitergeleitet")
        void deleteCart_repositoryThrows_propagatesException() {
            doThrow(new RuntimeException("Cart nicht gefunden"))
                    .when(cartRepository).deleteById(99L);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> cartService.deleteCart(99L));

            assertEquals("Cart nicht gefunden", ex.getMessage());
        }
    }


    // =========================================================
    // createCartItem
    // =========================================================

    @Nested
    @DisplayName("createCartItem()")
    class CreateCartItem {

        @Test
        @DisplayName("speichert CartItem und gibt es zurück")
        void createCartItem_savesAndReturnsItem() {
            CartItem item = new CartItem();
            item.setQuantity(3);
            when(cartItemRepository.save(item)).thenReturn(item);

            CartItem result = cartService.createCartItem(item);

            assertNotNull(result);
            assertEquals(3, result.getQuantity());
            verify(cartItemRepository).save(item);
        }

        @Test
        @DisplayName("gibt null zurück wenn Repository null liefert")
        void createCartItem_repositoryReturnsNull_returnsNull() {
            CartItem item = new CartItem();
            when(cartItemRepository.save(item)).thenReturn(null);

            CartItem result = cartService.createCartItem(item);

            assertNull(result);
        }

        @Test
        @DisplayName("Repository wirft Exception -> wird weitergeleitet")
        void createCartItem_repositoryThrows_propagatesException() {
            CartItem item = new CartItem();
            when(cartItemRepository.save(item)).thenThrow(new RuntimeException("DB Fehler"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> cartService.createCartItem(item));

            assertEquals("DB Fehler", ex.getMessage());
        }
    }


    // =========================================================
    // deleteCartItem
    // =========================================================

    @Nested
    @DisplayName("deleteCartItem()")
    class DeleteCartItem {

        @Test
        @DisplayName("ruft deleteById mit korrekter ID auf")
        void deleteCartItem_callsDeleteById() {
            cartService.deleteCartItem(7L);

            verify(cartItemRepository).deleteById(7L);
        }

        @Test
        @DisplayName("ruft deleteById genau einmal auf")
        void deleteCartItem_callsDeleteByIdExactlyOnce() {
            cartService.deleteCartItem(7L);

            verify(cartItemRepository, times(1)).deleteById(7L);
            verifyNoMoreInteractions(cartItemRepository);
        }

        @Test
        @DisplayName("Repository wirft Exception -> wird weitergeleitet")
        void deleteCartItem_repositoryThrows_propagatesException() {
            doThrow(new RuntimeException("CartItem nicht gefunden"))
                    .when(cartItemRepository).deleteById(7L);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> cartService.deleteCartItem(7L));

            assertEquals("CartItem nicht gefunden", ex.getMessage());
        }
    }


    // =========================================================
    // addCartItemToCart
    // =========================================================

    @Nested
    @DisplayName("addCartItemToCart()")
    class AddCartItemToCart {

        @Test
        @DisplayName("null-Item -> keine Repository-Interaktion")
        void addCartItemToCart_nullItem_doesNothing() {
            cartService.addCartItemToCart(null, 1L);

            verifyNoInteractions(cartRepository);
            verifyNoInteractions(cartItemRepository);
        }

        @Test
        @DisplayName("gültiges Item wird gespeichert und dem Cart hinzugefügt")
        void addCartItemToCart_validItem_savesItemAndAddsToCart() {
            CartItem dto = new CartItem();
            dto.setQuantity(2);

            CartItem savedItem = new CartItem();
            savedItem.setId(10L);
            savedItem.setQuantity(2);

            Cart cart = new Cart();
            cart.setId(1L);

            when(cartItemRepository.save(dto)).thenReturn(savedItem);
            when(cartRepository.getCartById(1L)).thenReturn(List.of(cart));

            cartService.addCartItemToCart(dto, 1L);

            verify(cartItemRepository).save(dto);
            verify(cartRepository).getCartById(1L);
            assertEquals(1, cart.getCartItems().size());
        }

        @Test
        @DisplayName("hinzugefügtes Item hat korrekte Werte")
        void addCartItemToCart_addedItem_hasCorrectValues() {
            CartItem dto = new CartItem();
            dto.setQuantity(5);

            CartItem savedItem = new CartItem();
            savedItem.setId(20L);
            savedItem.setQuantity(5);

            Cart cart = new Cart();
            cart.setId(2L);

            when(cartItemRepository.save(dto)).thenReturn(savedItem);
            when(cartRepository.getCartById(2L)).thenReturn(List.of(cart));

            cartService.addCartItemToCart(dto, 2L);

            CartItem addedItem = cart.getCartItems().getFirst();
            assertEquals(5, addedItem.getQuantity());
            assertEquals(20L, addedItem.getId());
        }
    }


    // =========================================================
    // addBikepartToCart
    // =========================================================

    @Nested
    @DisplayName("addBikepartToCart()")
    class AddBikepartToCart {

        @Test
        @DisplayName("null-bikepartId -> keine Repository-Interaktion")
        void addBikepartToCart_nullId_doesNothing() {
            Cart cart = new Cart();
            cart.setId(1L);
            Account dbAccount = new Account();
            dbAccount.setCart(cart);

            when(account.getId()).thenReturn(1L);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(dbAccount));

            cartService.addBikepartToCart(null, 1);

            verifyNoInteractions(cartRepository);
            verifyNoInteractions(cartItemRepository);
            verifyNoInteractions(bikepartRepository);
        }

        @Test
        @DisplayName("Repository wirft Exception -> wird weitergeleitet")
        void addBikepartToCart_repositoryThrows_propagatesException() {
            Cart cart = new Cart();
            cart.setId(1L);
            Account dbAccount = new Account();
            dbAccount.setCart(cart);

            when(account.getId()).thenReturn(1L);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(dbAccount));
            when(bikepartRepository.findBikepartById(5L))
                    .thenThrow(new RuntimeException("Bikepart nicht gefunden"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> cartService.addBikepartToCart(5L, 1));

            assertEquals("Bikepart nicht gefunden", ex.getMessage());
        }

        @Test
        @DisplayName("gültige bikepartId -> Bikepart wird gesucht, CartItem gespeichert und zum Cart hinzugefügt")
        void addBikepartToCart_validId_addsItemToCart() {
            Bikepart bikepart = new Bikepart();
            bikepart.setId(5L);
            bikepart.setName("Shimano XT Kette");

            CartItem savedItem = new CartItem();
            savedItem.setId(99L);

            Cart cart = new Cart();
            cart.setId(1L);
            Account dbAccount = new Account();
            dbAccount.setCart(cart);

            when(account.getId()).thenReturn(1L);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(dbAccount));
            when(bikepartRepository.findBikepartById(5L)).thenReturn(bikepart);
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(savedItem);

            cartService.addBikepartToCart(5L, 1);

            verify(bikepartRepository).findBikepartById(5L);
            verify(cartItemRepository).save(any(CartItem.class));
            assertEquals(1, cart.getCartItems().size());
        }
    }
}
