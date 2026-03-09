package com.bikeparts.service;

import com.bikeparts.entity.Cart;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class ActiveCart {

    private Cart cart;

    public void load(Cart cart) {
        this.cart = cart;
    }

    public Cart get() {
        return cart;
    }

    public boolean isLoaded() {
        return cart != null;
    }
}