package com.bikeparts.service;

import com.bikeparts.entity.Account;
import com.bikeparts.entity.Bike;
import com.bikeparts.entity.Bikepart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BikeService - Unit Tests")
class BikeServiceTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private BikeService bikeService;

    private Account account;
    private Bike bike;
    private Bikepart bikepart1;
    private Bikepart bikepart2;

    @BeforeEach
    void setUp() {
        bikepart1 = new Bikepart();
        bikepart1.setId(10L);
        bikepart1.setName("Shimano XT Kette");

        bikepart2 = new Bikepart();
        bikepart2.setId(11L);
        bikepart2.setName("Schwalbe Marathon Reifen");

        bike = new Bike();
        bike.setId(5L);
        bike.setBikeparts(List.of(bikepart1, bikepart2));

        account = new Account();
        account.setId(1L);
        account.setBikes(List.of(bike));
    }


    // =========================================================
    // getAllBikeparts
    // =========================================================

    @Nested
    @DisplayName("getAllBikeparts()")
    class GetAllBikeparts {

        @Test
        @DisplayName("returns bikeparts of the requested bike")
        void getAllBikeparts_validIds_returnsBikeparts() {
            when(accountService.findById(1L)).thenReturn(Optional.of(account));

            List<Bikepart> result = bikeService.getAllBikeparts(bike.getId());

            assertEquals(2, result.size());
            assertTrue(result.contains(bikepart1));
            assertTrue(result.contains(bikepart2));
        }

        @Test
        @DisplayName("Account nicht gefunden -> RuntimeException")
        void getAllBikeparts_accountNotFound_throwsException() {
            when(accountService.findById(99L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> bikeService.getAllBikeparts(bike.getId()));

            assertEquals("Account nicht gefunden", ex.getMessage());
        }

        @Test
        @DisplayName("Bike gehört nicht zum Account -> RuntimeException")
        void getAllBikeparts_bikeNotInAccount_throwsException() {
            when(accountService.findById(1L)).thenReturn(Optional.of(account));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> bikeService.getAllBikeparts(bike.getId()));

            assertEquals("Bike 99 nicht gefunden", ex.getMessage());
        }

        @Test
        @DisplayName("bike has no bikeparts -> returns empty list")
        void getAllBikeparts_bikeHasNoBikeparts_returnsEmptyList() {
            bike.setBikeparts(List.of());
            when(accountService.findById(1L)).thenReturn(Optional.of(account));

            List<Bikepart> result = bikeService.getAllBikeparts(bike.getId());

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}
