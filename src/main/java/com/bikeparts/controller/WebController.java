/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.bikeparts.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class WebController {

//    @GetMapping("/login")
//    public String login() {
//        return "login";
//    }

    @GetMapping("/accounts")
    public String persons() {
        return "accounts";
    }

//    @GetMapping("/")
//    public String home() {
//        return "redirect:/accounts";
//    }
}
