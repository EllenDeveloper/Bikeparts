package com.bikeparts.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cart – Entity Tests")
class CartTest {

    private Cart cart;

    @BeforeEach
    void setUp() {
        cart = new Cart();
    }


    @Nested
    @DisplayName("addBikepart()")
    class AddBikepart {

        @Test
        @DisplayName("CartItem wird zur Liste hinzugefügt")
        void addBikepart_addsCartItemToList() {
            Bikepart bikepart = new Bikepart();
            bikepart.setName("Shimano XT Kette");
            bikepart.setBrand("Shimano");

            cart.addBikepart(bikepart);

            assertEquals(1, cart.getCartItems().size());
        }

        @Test
        @DisplayName("CartItem hat korrekten Bikepart")
        void addBikepart_cartItemHasCorrectBikepart() {
            Bikepart bikepart = new Bikepart();
            bikepart.setName("Shimano XT Kette");
            bikepart.setBrand("Shimano");

            cart.addBikepart(bikepart);

            assertSame(bikepart, cart.getCartItems().getFirst().getBikepart());
        }

        @Test
        @DisplayName("Notes = name + brand")
        void addBikepart_notesContainsNameAndBrand() {
            Bikepart bikepart = new Bikepart();
            bikepart.setName("Shimano XT Kette");
            bikepart.setBrand("Shimano");

            cart.addBikepart(bikepart);

            assertEquals("Shimano XT KetteShimano", cart.getCartItems().getFirst().getNotes());
        }

        @Test
        @DisplayName("Mehrere Bikeparts werden alle hinzugefügt")
        void addBikepart_multipleItems_allAdded() {
            Bikepart bikepart1 = new Bikepart();
            bikepart1.setName("Kette");
            bikepart1.setBrand("Shimano");

            Bikepart bikepart2 = new Bikepart();
            bikepart2.setName("Kassette");
            bikepart2.setBrand("SRAM");

            cart.addBikepart(bikepart1);
            cart.addBikepart(bikepart2);

            assertEquals(2, cart.getCartItems().size());
        }

        @Test
        @DisplayName("null-Bikepart wirft NullPointerException")
        void addBikepart_nullBikepart_throwsNullPointerException() {
            assertThrows(NullPointerException.class, () -> cart.addBikepart(null));
        }
    }
}
