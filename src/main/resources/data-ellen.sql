-- ============================================================
-- Testdaten: Account Ellen – Merida MTB 29 Hardtail
-- Erstellt: 2026-03-04
-- ============================================================
-- Voraussetzung: schema.sql wurde bereits ausgeführt (Tabellen existieren).
-- ============================================================


-- ============================================================
-- 1. ACCOUNT
-- ============================================================
INSERT INTO accounts (
    email,
    first_name,
    last_name,
    role,
    is_active,
    language,
    preferred_currency,
    notifications_enabled,
    creation_date,
    created_at,
    updated_at
) VALUES (
    'ellen@bikeparts.de',
    'Ellen',
    NULL,
    'USER',
    TRUE,
    'DE',
    'EUR',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- ============================================================
-- 2. FAHRRAD – Merida MTB 29 Hardtail
-- ============================================================
INSERT INTO bikes (
    account_id,
    type,
    is_ebike,
    name,
    brand,
    model_name,
    wheel_size,
    notes,
    created_at,
    updated_at
) VALUES (
    (SELECT id FROM accounts WHERE email = 'ellen@bikeparts.de'),
    'MTB',
    FALSE,
    'mtb1',
    'Merida',
    'Hardtail MTB 29',
    29,
    'Merida MTB 29 Hardtail',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- ============================================================
-- 3. FAHRRADTEILE – Merida MTB 29
-- ============================================================

-- 3.1  Fahrradschlauch – Continental MTB 29 | 62-622 | Ventil SV42
-- -----------------------------------------------------------------------
-- 62-622: Breite 62 mm, Felgendurchmesser 622 mm (= 29 Zoll)
-- SV42 = Sclaverand-Ventil (Französisches Ventil / Presta), Länge 42 mm
-- Passend für Reifenbreiten: 28/29 × 1,75 und 28/29 × 2,5
INSERT INTO bikeparts (
    bike_id,
    type,
    name,
    brand,
    model,
    specific_details,
    tire_width,
    notes,
    created_at,
    updated_at
) VALUES (
    (SELECT id FROM bikes WHERE name = 'mtb1'
        AND account_id = (SELECT id FROM accounts WHERE email = 'ellen@bikeparts.de')),
    'INNER_TUBE',
    'Fahrradschlauch MTB 29',
    'Continental',
    '62-622',
    'SV42 (Sclaverand / Französisches Ventil, 42 mm); passend für 28/29 × 1,75 bis 28/29 × 2,5',
    '1,75 - 2,5',
    'Fahrradschlauch 29 Zoll. 62-622. Ventil: SV42.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- 3.2  Kette – Shimano XT 10-fach
-- ---------------------------------
INSERT INTO bikeparts (
    bike_id,
    type,
    name,
    brand,
    model,
    quality,
    alternative_qualities,
    specific_details,
    notes,
    created_at,
    updated_at
) VALUES (
    (SELECT id FROM bikes WHERE name = 'mtb1'
        AND account_id = (SELECT id FROM accounts WHERE email = 'ellen@bikeparts.de')),
    'CHAIN',
    'Shimano XT Kette 10-fach',
    'Shimano',
    'CN-HG95',
    'XT',
    'SLX, Deore',
    '10-fach',
    'Shimano XT 10-fach Kette. Kompatibel mit 10-fach Kassetten.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- 3.3  Mantel – Continental X-King 2,4 (29 Zoll )
-- ------------------------------------------------------------
-- Reifenbreite 2,4 Zoll, Felgendurchmesser
INSERT INTO bikeparts (
    bike_id,
    type,
    name,
    brand,
    model,
    quality,
    specific_details,
    tire_width,
    notes,
    created_at,
    updated_at
) VALUES (
    (SELECT id FROM bikes WHERE name = 'mtb1'
        AND account_id = (SELECT id FROM accounts WHERE email = 'ellen@bikeparts.de')),
    'TIRE_29',
    'Continental X-King 2,4 29"',
    'Continental',
    'X-King 2.4',
    'Performance',
    '61-622; 29 × 2,4 Zoll',
    '2,4',
    'MTB-Mantel Continental X-King 2,4" für 29-Zoll-Felgen.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- 3.4  Bremsbeläge – Shimano Disc Brake Pads G03A (XT / Hydraulik)
-- -----------------------------------------------------------------
-- Passend für Shimano Deore XT, SLX, ALFINE (hydraulische Scheibenbremse)
INSERT INTO bikeparts (
    bike_id,
    type,
    name,
    brand,
    model,
    quality,
    specific_details,
    notes,
    created_at,
    updated_at
) VALUES (
    (SELECT id FROM bikes WHERE name = 'mtb1'
        AND account_id = (SELECT id FROM accounts WHERE email = 'ellen@bikeparts.de')),
    'BRAKE_HYDRAULIC',
    'Shimano Disc Brake Pads G03A',
    'Shimano',
    'G03A',
    'XT',
    'Hydraulische Scheibenbremse; Resin-Belag; passend für Deore XT, SLX, ALFINE',
    'Bremsbeläge G03A für Shimano XT-Bremsen (Deore XT, SLX, ALFINE).',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- 3.5  Kassette – Shimano XT CS-M771-10
-- ----------------------------------------
INSERT INTO bikeparts (
    bike_id,
    type,
    name,
    brand,
    model,
    quality,
    specific_details,
    notes,
    created_at,
    updated_at
) VALUES (
    (SELECT id FROM bikes WHERE name = 'mtb1'
        AND account_id = (SELECT id FROM accounts WHERE email = 'ellen@bikeparts.de')),
    'CASSETTE',
    'Shimano XT Kassette CS-M771-10',
    'Shimano',
    'CS-M771-10',
    'XT',
    '10-fach, 11-36T',
    'Shimano XT 10-fach Kassette, 11-36 Zähne.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- 3.6  Pedale – Shimano XT PD-M780
-- -----------------------------------
INSERT INTO bikeparts (
    bike_id,
    type,
    name,
    brand,
    model,
    quality,
    specific_details,
    notes,
    created_at,
    updated_at
) VALUES (
    (SELECT id FROM bikes WHERE name = 'mtb1'
        AND account_id = (SELECT id FROM accounts WHERE email = 'ellen@bikeparts.de')),
    'PEDAL',
    'Shimano XT Pedale PD-M780',
    'Shimano',
    'PD-M780',
    'XT',
    'Klickpedale (SPD), beidseitig einrastbar',
    'Shimano XT Klickpedale PD-M780, SPD-System.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- ============================================================
-- 4. WARENKORB – aktiver Warenkorb für Ellen
-- ============================================================
-- 1:1-Beziehung: FK cart_id liegt in accounts (Owning Side)
INSERT INTO carts (
    name,
    created_at,
    updated_at
) VALUES (
    'Frühjahrs-Wartung 2026',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- FK in accounts.cart_id setzen
UPDATE accounts
SET cart_id = (SELECT id FROM carts WHERE name = 'Frühjahrs-Wartung 2026')
WHERE email = 'ellen@bikeparts.de';


-- ============================================================
-- 5. WARENKORB-ARTIKEL – alle Bikeparts von mtb1
-- ============================================================
-- bikepart_id verknüpft den Artikel mit dem gespeicherten Bikepart.
-- product_name dient als Fallback-Anzeigename (getEffectiveProductName()).

-- 5.1  Fahrradschlauch Continental 62-622 SV42
INSERT INTO cart_items (
    cart_id,
    bikepart_id,
    product_name,
    quantity,
    notes,
    created_at,
    updated_at
) VALUES (
    (SELECT cart_id FROM accounts WHERE email = 'ellen@bikeparts.de'),
    (SELECT id FROM bikeparts WHERE model = '62-622'
        AND bike_id = (SELECT id FROM bikes WHERE name = 'mtb1'
            AND account_id = (SELECT id FROM accounts WHERE email = 'ellen@bikeparts.de'))),
    'Fahrradschlauch MTB 29',
    2,
    'Immer 2 Schläuche auf Vorrat',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 5.2  Shimano XT Kette 10-fach
INSERT INTO cart_items (
    cart_id,
    bikepart_id,
    product_name,
    quantity,
    notes,
    created_at,
    updated_at
) VALUES (
    (SELECT cart_id FROM accounts WHERE email = 'ellen@bikeparts.de'),
    (SELECT id FROM bikeparts WHERE model = 'CN-HG95'
        AND bike_id = (SELECT id FROM bikes WHERE name = 'mtb1'
            AND account_id = (SELECT id FROM accounts WHERE email = 'ellen@bikeparts.de'))),
    'Shimano XT Kette 10-fach',
    1,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 5.3  Continental X-King Mantel 2,4"
INSERT INTO cart_items (
    cart_id,
    bikepart_id,
    product_name,
    quantity,
    notes,
    created_at,
    updated_at
) VALUES (
    (SELECT cart_id FROM accounts WHERE email = 'ellen@bikeparts.de'),
    (SELECT id FROM bikeparts WHERE model = 'X-King 2.4'
        AND bike_id = (SELECT id FROM bikes WHERE name = 'mtb1'
            AND account_id = (SELECT id FROM accounts WHERE email = 'ellen@bikeparts.de'))),
    'Continental X-King 2,4 29"',
    2,
    'Vorder- und Hinterreifen ersetzen',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 5.4  Shimano Bremsbeläge G03A
INSERT INTO cart_items (
    cart_id,
    bikepart_id,
    product_name,
    quantity,
    notes,
    created_at,
    updated_at
) VALUES (
    (SELECT cart_id FROM accounts WHERE email = 'ellen@bikeparts.de'),
    (SELECT id FROM bikeparts WHERE model = 'G03A'
        AND bike_id = (SELECT id FROM bikes WHERE name = 'mtb1'
            AND account_id = (SELECT id FROM accounts WHERE email = 'ellen@bikeparts.de'))),
    'Shimano Disc Brake Pads G03A',
    2,
    'Vorder- und Hinterbremse',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 5.5  Shimano XT Kassette CS-M771-10
INSERT INTO cart_items (
    cart_id,
    bikepart_id,
    product_name,
    quantity,
    notes,
    created_at,
    updated_at
) VALUES (
    (SELECT cart_id FROM accounts WHERE email = 'ellen@bikeparts.de'),
    (SELECT id FROM bikeparts WHERE model = 'CS-M771-10'
        AND bike_id = (SELECT id FROM bikes WHERE name = 'mtb1'
            AND account_id = (SELECT id FROM accounts WHERE email = 'ellen@bikeparts.de'))),
    'Shimano XT Kassette CS-M771-10',
    1,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 5.6  Shimano XT Pedale PD-M780
INSERT INTO cart_items (
    cart_id,
    bikepart_id,
    product_name,
    quantity,
    notes,
    created_at,
    updated_at
) VALUES (
    (SELECT cart_id FROM accounts WHERE email = 'ellen@bikeparts.de'),
    (SELECT id FROM bikeparts WHERE model = 'PD-M780'
        AND bike_id = (SELECT id FROM bikes WHERE name = 'mtb1'
            AND account_id = (SELECT id FROM accounts WHERE email = 'ellen@bikeparts.de'))),
    'Shimano XT Pedale PD-M780',
    1,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- ============================================================
-- ENDE – Testdaten Ellen
-- ============================================================
