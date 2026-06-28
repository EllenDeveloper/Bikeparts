package com.bikeparts.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider – Unit Tests")
class JwtTokenProviderTest {

    // mind. 32 Zeichen fuer HMAC-SHA256
    private static final String TEST_SECRET = "TestSecretKeyFuerJWTUnitTests1234567890AbcDef!!";

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(TEST_SECRET);
        ReflectionTestUtils.setField(provider, "jwtExpirationMs", 86400000L); // 24h
    }

    private Authentication auth(String email) {
        return new UsernamePasswordAuthenticationToken(
                email, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }


    // =========================================================
    // generateToken()
    // =========================================================

    @Nested
    @DisplayName("generateToken()")
    class GenerateToken {

        @Test
        @DisplayName("gueltige Authentication -> gibt non-null Token zurueck")
        void generateToken_validAuth_returnsToken() {
            String token = provider.generateToken(auth("test@bikeparts.de"));

            assertNotNull(token);
            assertFalse(token.isBlank());
        }

        @Test
        @DisplayName("Token besteht aus drei JWT-Segmenten (Header.Payload.Signature)")
        void generateToken_hasThreeJwtSegments() {
            String token = provider.generateToken(auth("test@bikeparts.de"));

            assertEquals(3, token.split("\\.").length);
        }

        @Test
        @DisplayName("zwei Tokens fuer gleichen User sind unterschiedlich (Zeitstempel)")
        void generateToken_twoTokensForSameUser_areDifferent() throws InterruptedException {
            String token1 = provider.generateToken(auth("demo@bikeparts.de"));
            Thread.sleep(1);
            String token2 = provider.generateToken(auth("test@bikeparts.de"));

            assertNotEquals(token1, token2);
        }
    }


    // =========================================================
    // getUsernameFromToken()
    // =========================================================

    @Nested
    @DisplayName("getUsernameFromToken()")
    class GetUsernameFromToken {

        @Test
        @DisplayName("gueltiger Token -> gibt korrekte Email zurueck")
        void getUsernameFromToken_validToken_returnsEmail() {
            String token = provider.generateToken(auth("demo@bikeparts.de"));

            assertEquals("demo@bikeparts.de", provider.getUsernameFromToken(token));
        }

        @Test
        @DisplayName("test@bikeparts.de -> korrekt aus Token gelesen")
        void getUsernameFromToken_adminEmail_returnsAdminEmail() {
            String token = provider.generateToken(auth("test@bikeparts.de"));

            assertEquals("test@bikeparts.de", provider.getUsernameFromToken(token));
        }
    }


    // =========================================================
    // validateToken()
    // =========================================================

    @Nested
    @DisplayName("validateToken()")
    class ValidateToken {

        @Test
        @DisplayName("gueltiger Token -> true")
        void validateToken_validToken_returnsTrue() {
            String token = provider.generateToken(auth("test@bikeparts.de"));

            assertTrue(provider.validateToken(token));
        }

        @Test
        @DisplayName("manipulierter Token (Signatur veraendert) -> false")
        void validateToken_tamperedSignature_returnsFalse() {
            String token = provider.generateToken(auth("test@bikeparts.de"));
            String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "UNGUELTIGE_SIGNATUR";

            assertFalse(provider.validateToken(tampered));
        }

        @Test
        @DisplayName("Token mit anderem Secret signiert -> false")
        void validateToken_differentSecret_returnsFalse() {
            JwtTokenProvider otherProvider = new JwtTokenProvider("AndererSecretKeyFuerTest1234567890AbcDef!!");
            ReflectionTestUtils.setField(otherProvider, "jwtExpirationMs", 86400000L);
            String tokenFromOtherProvider = otherProvider.generateToken(auth("test@bikeparts.de"));

            assertFalse(provider.validateToken(tokenFromOtherProvider));
        }

        @Test
        @DisplayName("abgelaufener Token -> false")
        void validateToken_expiredToken_returnsFalse() {
            // setzt den parameter test@bikeparts.de von provider per reflection auf -1000L
            ReflectionTestUtils.setField(provider, "jwtExpirationMs", -1000L); // bereits abgelaufen
            String expiredToken = provider.generateToken(auth("test@bikeparts.de"));

            assertFalse(provider.validateToken(expiredToken));
        }

        @Test
        @DisplayName("leerer String -> false")
        void validateToken_emptyString_returnsFalse() {
            assertFalse(provider.validateToken(""));
        }

        @Test
        @DisplayName("zufaelliger String (kein JWT) -> false")
        void validateToken_randomString_returnsFalse() {
            assertFalse(provider.validateToken("das-ist-kein-jwt-token"));
        }
    }
}
