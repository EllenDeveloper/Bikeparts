package com.bikeparts.controller;

import com.bikeparts.entity.Bike;
import com.bikeparts.service.BikeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class BikeViewController {
    private final BikeService bikeService;

    @Autowired
    public BikeViewController(BikeService bikeService) {
        this.bikeService = bikeService;
    }

    @GetMapping("/accounts/{id}/bikes")
    public String showBikes(
            @PathVariable Long id,
            HttpSession session,
            Model model) {
        session.setAttribute("accountId", id);
        List<Bike> bikes = bikeService.getAllBikesByAccountId(id);

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
            HttpSession session,
            Model model) {
        Long accountId = (Long) session.getAttribute("accountId");
        Bike bike = bikeService.getBikeById(bikeId);
        if (!bike.getAccount().getId().equals(accountId)) {
            throw new RuntimeException("Zugriff verweigert");
        }
        model.addAttribute("bikeparts", bikeService.getAllBikeparts(accountId, bikeId));
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
}
