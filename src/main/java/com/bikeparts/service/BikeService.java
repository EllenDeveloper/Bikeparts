
package com.bikeparts.service;


import com.bikeparts.entity.Bike;
import com.bikeparts.entity.Bikepart;
import com.bikeparts.repository.BikeRepository;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BikeService {
    
    private final BikeRepository bikeRepository;

    // Constructor Injection
    @Autowired
    public BikeService(BikeRepository bikeRepository) {
        this.bikeRepository = bikeRepository;
    }
    
    // Alle Bikes abrufen
    public List<Bike> getAllBikes() {
        return bikeRepository.findAll();
    }

    public List<Bikepart> getAllBikeparts(Long bikeId) {
        Bike bike = bikeRepository.getBikeById(bikeId);
        return bike.getBikeparts();
    }
}
