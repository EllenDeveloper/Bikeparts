package com.bikeparts.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Annottation für custom annotation
@Target(ElementType.METHOD)  // Nur auf Methoden
@Retention(RetentionPolicy.RUNTIME)  // Zur Laufzeit verfügbar
public @interface Timed {
    // Keine Parameter nötig
}