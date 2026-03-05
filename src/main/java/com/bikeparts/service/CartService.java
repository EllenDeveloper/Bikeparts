package com.bikeparts.service;

import com.bikeparts.entity.Bikepart;
import com.bikeparts.entity.Cart;
import com.bikeparts.entity.CartItem;
import com.bikeparts.repository.BikepartRepository;
import com.bikeparts.repository.CartItemRepository;
import com.bikeparts.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BikepartRepository bikepartRepository;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       BikepartRepository bikepartRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.bikepartRepository = bikepartRepository;
    }

    // --- Cart methods

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
    public void addBikepartToCart(Long bikepartId, Long cartId) {
        if (bikepartId != null) {
            Bikepart bikepart = bikepartRepository.getById(bikepartId);
            Cart cart = cartRepository.getCartById(cartId).getFirst();
            CartItem cartItem = new CartItem();
            cartItem.setBikepart(bikepart);
            cart.addCartItem(cartItem);
            cartItemRepository.save(cartItem);
        }
    }

    // --- CartItem methods

    public CartItem createCartItem(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    public void deleteCartItem(Long id) {
        cartItemRepository.deleteById(id);
    }
}
