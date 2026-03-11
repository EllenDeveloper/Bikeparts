package com.bikeparts.controller;

import com.bikeparts.entity.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class AccountController {
    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

//    @GetMapping("/full")
//    public Account currentAccountFull(@CurrentAccount Account account) {
//        log.debug("currentUser = " + account);
//        return account;
//    }

}
