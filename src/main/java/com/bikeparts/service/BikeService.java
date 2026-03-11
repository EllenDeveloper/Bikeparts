
package com.bikeparts.service;


import com.bikeparts.annotation.Timed;
import com.bikeparts.entity.Account;
import com.bikeparts.entity.Bike;
import com.bikeparts.entity.Bikepart;
import com.bikeparts.repository.BikeRepository;

import java.util.Comparator;
import java.util.List;

import com.bikeparts.repository.BikepartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BikeService {
    
    private final BikeRepository bikeRepository;
    private final AccountService accountService;
    private final BikepartRepository bikepartRepository;
    private final Account account;

    // Constructor Injection
    @Autowired
    public BikeService(BikeRepository bikeRepository, AccountService accountService, BikepartRepository bikepartRepository, Account account) {
        this.bikeRepository = bikeRepository;
        this.accountService = accountService;
        this.bikepartRepository = bikepartRepository;
        this.account = account;
    }

    @Timed
    public List<Bikepart> getAllBikeparts(Long bikeId) {
        Account accountJPA = accountService.findById(account.getId()).orElseThrow(() -> new RuntimeException("Account nicht gefunden"));
        Bike bike = accountJPA.getBikes().stream().filter(b -> b.getId().equals(bikeId)).findFirst()
                .orElseThrow(() -> new RuntimeException("Bike " + bikeId + " nicht gefunden"));
        return bike.getBikeparts().stream().sorted(Comparator.comparing(Bikepart::getName)).toList();
    }

    public List<Bike> getAllBikes() {
        return bikeRepository.findByAccountIdOrderByTypeAsc(account.getId());
    }

    public Bike getBikeById(Long id) {
        return bikeRepository.findBikeById(id);
    }

    public Bikepart getBikepartById(Long bikepartId) {
        Bikepart bikepart = bikepartRepository.findBikepartById(bikepartId);
        if (bikepart == null) {
            throw new RuntimeException("Bikepart nicht gefunden");
        }
        List<Bike> bikesOfAccount = bikeRepository.findByAccountId(account.getId());
        if (bikesOfAccount.stream().toList().contains(bikepart.getBike())) {
            return bikepartRepository.findBikepartById(bikepartId);
        } else {
            throw new RuntimeException("Zugriff verweigert");
        }
    }
}
