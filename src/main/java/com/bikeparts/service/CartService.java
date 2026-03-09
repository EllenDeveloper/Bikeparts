package com.bikeparts.service;

import com.bikeparts.entity.Account;
import com.bikeparts.entity.Bikepart;
import com.bikeparts.entity.Cart;
import com.bikeparts.entity.CartItem;
import com.bikeparts.repository.BikepartRepository;
import com.bikeparts.repository.CartItemRepository;
import com.bikeparts.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.SessionScope;

import java.util.Optional;

@Component
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BikepartRepository bikepartRepository;
    private final AccountService accountService;
    private final ActiveCart activeCart;

    @Autowired
    public CartService(ActiveCart activeCart, CartRepository cartRepository, CartItemRepository cartItemRepository,
                       BikepartRepository bikepartRepository, AccountService accountService) {
        this.activeCart = activeCart;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.bikepartRepository = bikepartRepository;
        this.accountService = accountService;
    }

    // --- Cart methods

    public Cart getCart(Cart cart) {
//        Optional<Cart> cartById = cartRepository.findById(cart.getId());
//        if (cartById == null) {
//            return createCart(cart);
//        }
//        else {
//            return cartById.orElse();
//        }
        return null;
    }

    public Cart createCart(Cart cart) {
        return cartRepository.save(cart);
    }

    public void deleteCart(Long id) {
        cartRepository.deleteById(id);
    }

    @Transactional
    public void addCartItemToCart(CartItem cartItemDTO, Long cartId) {
        if (cartItemDTO != null) {
            CartItem cartItem = createCartItem(cartItemDTO);
            Cart cart = cartRepository.getCartById(cartId).getFirst();
            cart.addCartItem(cartItem);
        }
    }

    @Transactional
    public Cart addBikepartToCart(Long bikepartId, Long accountId, Integer quantity) {
        Account account = accountService.findById(accountId).orElseThrow(() -> new RuntimeException("Account nicht gefunden"));

        Cart cart = account.getCart();
        if (cart == null) {
            cart = cartRepository.save(new Cart());
        }
        // move this to login-Handler / at a position when the user logs in
        if (!activeCart.isLoaded()) {
            activeCart.load(cart);
        }

        if (bikepartId != null) {
            Bikepart bikepart = bikepartRepository.findBikepartById(bikepartId);

            CartItem cartItem = new CartItem(cart, bikepart, quantity);
            createCartItem(cartItem);
            return cart;
        }
        return null;
    }

    // --- CartItem methods

    public CartItem createCartItem(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    public void deleteCartItem(Long id) {
        cartItemRepository.deleteById(id);
    }
}
