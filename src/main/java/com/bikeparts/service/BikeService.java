
package com.bikeparts.service;


import com.bikeparts.annotation.Timed;
import com.bikeparts.entity.Account;
import com.bikeparts.entity.Bike;
import com.bikeparts.entity.Bikepart;
import com.bikeparts.repository.BikeRepository;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BikeService {
    
    private final BikeRepository bikeRepository;
    private final AccountService accountService;

    // Constructor Injection
    @Autowired
    public BikeService(BikeRepository bikeRepository, AccountService accountService) {
        this.bikeRepository = bikeRepository;
        this.accountService = accountService;
    }

    @Timed
    public List<Bikepart> getAllBikeparts(Long accountId, Long bikeId) {
        Account account = accountService.findById(accountId).orElseThrow(() -> new RuntimeException("Account nicht gefunden"));
        Bike bike = account.getBikes().stream().filter(b -> b.getId().equals(bikeId)).findFirst()
                .orElseThrow(() -> new RuntimeException("Bike " + bikeId + " nicht gefunden"));
        return bike.getBikeparts().stream().sorted(Comparator.comparing(Bikepart::getName)).toList();
    }

    public List<Bike> getAllBikesByAccountId(Long id) {
        return bikeRepository.findByAccountIdOrderByTypeAsc(id);
    }

    public Bike getBikeById(Long id) {
        return bikeRepository.findBikeById(id);
    }

    public Bikepart getBikepartById(Long bikepartId, Long accountId) {
        Bikepart bikepart = bikeRepository.findBikepartById(bikepartId);
        if (bikepart == null) {
            throw new RuntimeException("Bikepart nicht gefunden");
        }
        List<Bike> bikesOfAccount = bikeRepository.findByAccountId(accountId);
        if (bikesOfAccount.contains(bikepart.getBike())) {
            return bikeRepository.findBikepartById(bikepartId);
        } else {
            throw new RuntimeException("Zugriff verweigert");
        }
    }
}
