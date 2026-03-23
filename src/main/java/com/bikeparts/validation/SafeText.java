package com.bikeparts.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.*;

/**
 * Validierungsannotation zum Schutz gegen XSS und Query-Manipulation.
 *
 * <p>Erlaubt nur Buchstaben (inkl. Umlaute), Ziffern, Leerzeichen, Bindestrich,
 * Punkt und Schraegstrich. Sonderzeichen wie {@code < > " ' ; ( )} sind nicht
 * erlaubt, um das Einschleusen von JavaScript-Code (XSS) zu verhindern.</p>
 *
 * <p>Einsatz auf allen String-Feldern, die als Suchbegriff an externe Shops
 * weitergegeben werden oder im HTML gerendert werden koennen.</p>
 */
@Pattern(
        regexp = "^[\\w\\s\\-./äöüÄÖÜß]*$",
        message = "Ungültige Zeichen - kein HTML oder Sonderzeichen erlaubt"
)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
public @interface SafeText {

    String message() default "Ungültige Zeichen - kein HTML oder Sonderzeichen erlaubt";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}