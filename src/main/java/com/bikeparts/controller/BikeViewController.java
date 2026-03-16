package com.bikeparts.controller;

import com.bikeparts.entity.Account;
import com.bikeparts.entity.Bike;
import com.bikeparts.entity.Bikepart;
import com.bikeparts.entity.CartItem;
import com.bikeparts.price.service.ScrapingResult;
import com.bikeparts.service.AccountService;
import com.bikeparts.service.BikeService;
import com.bikeparts.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class BikeViewController {
    private final BikeService bikeService;
    private final CartService cartService;
    private final AccountService accountService;
    private final Account account;
    private Logger log = LoggerFactory.getLogger(BikeViewController.class);

    @Autowired
    public BikeViewController(BikeService bikeService, CartService cartService, AccountService accountService, Account account) {
        this.bikeService = bikeService;
        this.cartService = cartService;
        this.accountService = accountService;
        this.account = account;
    }

    @GetMapping("/bikes")
    public String showBikes(
            Model model) {
        List<Bike> bikes = bikeService.getAllBikes();

        model.addAttribute("bikes", bikes);
        return "bikes-list";
    }

    @GetMapping("/bikes/{bikeId}")
    public String showBike(
            @PathVariable Long bikeId,
            Model model) {
        Bike bike = bikeService.getBikeById(bikeId);

        model.addAttribute("bike", bike);
        return "bike-details";
    }

    @GetMapping("/bikes/{bikeId}/bikeparts")
    public String showBikeparts(
            @PathVariable Long bikeId,
            Model model) {
        Bike bike = bikeService.getBikeById(bikeId);
        if (!bike.getAccount().getId().equals(account.getId())) {
            throw new RuntimeException("Zugriff verweigert");
        }
        model.addAttribute("bikeparts", bikeService.getAllBikeparts(bikeId));
        model.addAttribute("accountId", account.getId());
        return "bikeparts-list";
    }

    @GetMapping("/bikeparts/{id}")
    public String showBikepart(
            @PathVariable Long id,
            Model model) {
        model.addAttribute("bikepart", bikeService.getBikepartById(id));
        return "bikepart-details";
    }

    @PostMapping("/bikeparts/{id}/addBikepartToCart")
    public String addBikepartToCart(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer quantity,
            Model model) {

        cartService.addBikepartToCart(id, quantity);
        Bikepart bikepart = bikeService.getBikepartById(id);
        model.addAttribute("bikeparts",  bikeService.getAllBikeparts(bikepart.getBike().getId()));
        model.addAttribute("accountId", account.getId());
        return "bikeparts-list";
    }

    @GetMapping("/cart/")
    public String showCart(
            Model model) {
          model.addAttribute("cart", account.getCart());
        return "cart-cartItems-list";
    }

    @GetMapping("/cart/cartItem/{id}/searchPriceBikeComponents")
    public String searchPriceBikeComponents(
            @PathVariable Long id,
            Model model) {

        // TODO: check
        CartItem cartItem = cartService.getCartItem(account.getCart(), id);
        ScrapingResult result = cartService.searchPriceBikeComponents(cartItem.getBikepart());
        model.addAttribute("productOffers", result.offers());
        model.addAttribute("scrapingStatus", result.status());
        model.addAttribute("scrapingError", result.errorMessage());
        return "price-search-result";
    }
}
