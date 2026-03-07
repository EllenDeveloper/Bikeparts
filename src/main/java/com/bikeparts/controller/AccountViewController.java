package com.bikeparts.controller;

import com.bikeparts.entity.Account;
import com.bikeparts.entity.Bike;
import com.bikeparts.service.AccountService;
import com.bikeparts.service.BikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/accounts")
public class AccountViewController {
    private final BikeService bikeService;
    private final AccountService accountService;

    @Autowired
    public AccountViewController(BikeService bikeService, AccountService accountService) {
        this.bikeService = bikeService;
        this.accountService = accountService;
    }

    @GetMapping("/accounts")
    public String showAccounts(Model model) {
        // 1. Daten vom Service holen
        List<Account> accounts = accountService.findAll();

        // 2. Daten ins Model packen
        model.addAttribute("accounts", accounts);
        //                  ^^^^^^^^    ^^^^^^^
        //                  Name        Daten

        // 3. View-Namen zurückgeben
        return "accounts-list";
        //     ^^^^^^^^^^^^^^
        //     → src/main/resources/templates/bikes-list.html
    }
}
