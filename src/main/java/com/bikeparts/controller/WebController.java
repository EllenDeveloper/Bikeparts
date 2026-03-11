/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.bikeparts.controller;

import com.bikeparts.entity.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class WebController {
    private static final Logger log = LoggerFactory.getLogger(WebController.class);

    @GetMapping("/login")
    public String login() {
        return "login";
    }


//    @GetMapping("/me/full")
//    public String currentAccountFull(@CurrentAccount Account account) {
//        log.debug("currentUser = " + account);
//        return "account";
//    }

    @GetMapping("/")
    public String home() {
        return "redirect:/bikes-list";
    }
}
