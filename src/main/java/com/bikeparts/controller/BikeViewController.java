package com.bikeparts.controller;

import com.bikeparts.entity.Account;
import com.bikeparts.entity.Bike;
import com.bikeparts.entity.Bikepart;
import com.bikeparts.entity.Cart;
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
import java.util.Optional;

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

    @GetMapping("/accounts/{id}/bikes")
    public String showBikes(
            @PathVariable Long id,
            HttpSession session,
            Model model) {
        session.setAttribute("accountId", id);
        log.info("****"+ account);
        List<Bike> bikes = bikeService.getAllBikesByAccountId(id);

        model.addAttribute("bikes", bikes);
        model.addAttribute("accountId", id);
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
            HttpSession session,
            Model model) {
        Long accountId = (Long) session.getAttribute("accountId");
        Bike bike = bikeService.getBikeById(bikeId);
        if (!bike.getAccount().getId().equals(accountId)) {
            throw new RuntimeException("Zugriff verweigert");
        }
        model.addAttribute("bikeparts", bikeService.getAllBikeparts(accountId, bikeId));
        model.addAttribute("accountId", accountId);
        return "bikeparts-list";
    }

    @GetMapping("/bikeparts/{id}")
    public String showBikepart(
            @PathVariable Long id,
            HttpSession session,
            Model model) {
        Long accountId = (Long) session.getAttribute("accountId");
        model.addAttribute("bikepart", bikeService.getBikepartById(id, accountId));
        return "bikepart-details";
    }

    @PostMapping("/bikeparts/{id}/addBikepartToCart")
    public String addBikepartToCart(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer quantity,
            HttpSession session,
            Model model) {
        // TODO verschieben nach Login und in die Session packen
        Long accountId = (Long) session.getAttribute("accountId");
        Bikepart bikepartById = bikeService.getBikepartById(id, accountId);
        Cart cart = cartService.addBikepartToCart(id, accountId, quantity);
        model.addAttribute("cart", cart);
        return "bikeparts-list";
    }

    @GetMapping("/cart/{id}")
    public String showCart(
            @PathVariable Long id,
            HttpSession session,
            Model model) {
        Long accountId = (Long) session.getAttribute("accountId");
        Account account = accountService.findById(accountId).orElseThrow(() -> new RuntimeException("Account nicht gefunden"));
        model.addAttribute("cart", account.getCart());
        return "cart-cartItems-list";
    }
}
