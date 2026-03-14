package com.bikeparts.enums;

/**
 * Bikepart type enum
 */
public enum BikepartType {
    WHEEL_29("Laufrad 29 Zoll"),      // Rad 29 Zoll / Wheel 29 inch
    WHEEL_27("Laufrad 27 Zoll"),      // Rad 27 Zoll / Wheel 27 inch
    CHAIN("Kette"),               // Kette / Chain
    CASSETTE("Kassette"),         // Kassette / Cassette
    DISC_BRAKE("Scheibenbremse"), // Scheibenbremse / Disc brake
    BRAKE_V_BRAKE("V-Bremse"),    // V-Bremse / V-brake
    TIRE_29("Mantel 29 Zoll"),    // Reifen 29 Zoll / Tire 29 inch
    TIRE_27("Mantel 27 Zoll"),    // Reifen 27 Zoll / Tire 27 inch
    GEAR_SHIFT("Schaltung"),      // Schaltung / Gear shift
    PEDAL("Pedale"),              // Pedale / Pedal
    INNER_TUBE("Schlauch");       // Schlauch / Inner tube

    private final String label;

    BikepartType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
