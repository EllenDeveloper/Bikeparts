# Datenmodell - Entities & Attribute

## 1. Account (Benutzer)

```
Account
├── id (PK, Long)
├── email (String, unique)
├── password (String, encrypted)
├── firstName (String)
├── lastName (String)
├── role (Enum: UserRole = USER, ADMIN)
├── isActive (Boolean)
├── createdAt (Timestamp)
└── updatedAt (Timestamp)
```

**Relationen:**
- `1:N` zu BIKE (Ein Account hat mehrere Fahrräder)
- `1:1` zu CART (Ein Account hat genau einen Warenkorb)
- `1:N` zu FAVORITE_SHOP (Ein Account hat mehrere Favoriten-Shops)
- `1:N` zu BLACKLISTED_SHOP (Ein Account hat mehrere blockierte Shops)
- `1:N` zu SEARCH_RESULT (Ein Account hat mehrere gespeicherte Suchergebnisse)

---

## 2. BIKE (Fahrrad)

```
BIKE
├── id (PK, Long)
├── accountId (FK, Long) → ACCOUNT.id
├── type (Enum: MTB, MTB_FULLY, TREKKING_BIKE, RACING_BIKE)
├── isEbike (Boolean)
├── name (String) - Benutzerdefinierter Name
├── brand (String) - Marke
├── modelName (String) - Modellname
├── wheelSize (Integer) - 27 Zoll, 29 Zoll
├── notes (Text)
├── createdAt (Timestamp)
└── updatedAt (Timestamp)
```

**Relationen:**
- `N:1` zu ACCOUNT (Mehrere Bikes gehören einem Account)
- `1:N` zu BIKEPART (Ein Bike hat mehrere Teile)

---

## 3. BIKEPART (Fahrradteil)

```
BIKEPART
├── id (PK, Long)
├── bikeId (FK, Long) → BIKE.id
├── type (Enum: WHEEL_29, WHEEL_27, CHAIN, CASSETTE, BRAKE_HYDRAULIC, BRAKE_V_BRAKE, TIRE_29, TIRE_27, GEAR_SHIFT)
├── name (String) - z.B. "Shimano XT CN-HG95"
├── brand (String) - z.B. "Shimano"
├── model (String)
├── quality (String) - z.B. "XT", "Deore", "105"
├── specific_details (String) - z.B. "10-fach"
├── alternativeQualities (String) - Komma-getrennt: "LX, SLX"
├── tireWidth (Integer) - Mantelbreite in mm (z.B. 2.25, 2.35)
├── notes (Text)
├── createdAt (Timestamp)
└── updatedAt (Timestamp)
```
GEAR_SHIFT = Schaltung


**Relationen:**
- `N:1` zu BIKE (Mehrere Parts gehören zu einem Bike)
- `N:M` zu CART via CART_ITEM (Parts können in Warenkorb gelegt werden)


## 4. CART (Warenkorb)

```
CART
├── id (PK, Long)
├── accountId (FK, Long, unique) → ACCOUNT.id
├── name (String) - z.B. "Frühjahrs-Wartung 2026"
├── createdAt (Timestamp)
└── updatedAt (Timestamp)
```

**Relationen:**
- `1:1` zu ACCOUNT (Jeder Account hat genau einen Warenkorb)
- `1:N` zu CART_ITEM (Ein Cart hat mehrere Items)

---

## 5. CART_ITEM (Warenkorb-Artikel)

```
CART_ITEM
├── id (PK, Long)
├── cartId (FK, Long) → CART.id
├── bikepartId (FK, Long) → BIKEPART.id (nullable)
├── productName (String) - Falls nicht von eigenem Bike
├── quantity (Integer)
├── notes (String)
├── createdAt (Timestamp)
└── updatedAt (Timestamp)
```

**Relationen:**
- `N:1` zu CART (Mehrere Items gehören zu einem Cart)
- `N:1` zu BIKEPART (Optional: Item kann referenzieren auf eigenes Bikepart)
- `1:N` zu PRODUCT_OFFER  direktional

---

## 6. PRODUCT_OFFER (Produkt-Angebot von Shop)

```
PRODUCT_OFFER
├── id (PK, Long)
├── searchResultId (FK, Long) → SEARCH_RESULT.id   -> ersteinmal nicht
├── productName (String)
├── shopName (String)
├── shopId (Long)
├── productUrl (String)
├── price (BigDecimal)
├── inStock (Boolean)
├── price (BigDecimal) - Preis + Versand
├── source (Enum: AMAZON, EBAY, IDEALO, WEB_SCRAPING)
├── fetchedAt (Timestamp)
```

**Relationen:**
- `N:1` zu SEARCH_RESULT  -> ersteinmal nicht
- `N:1` zu CART_ITEM
- `1:N` zu SHOP_INFO  direktional

---
## 6.1. SHOP_INFO

```
SHOP_INFO
├── id (PK, Long)
├── shopName (String)
├── shppingCostUrl (String) -> ändern in shippingCost
├── shppingCost (BigDecimal) -> ändern in shippingCost
├── freeShippingOnOrdersOver (BigDecimal)
├── source (Enum: WEB_SCRAPING)
├── fetchedAt (Timestamp)
```

**Relationen:**
- `N:1` zu SEARCH_RESULT   -> ersteinmal nicht
- `N:1` zu CART_ITEM

---
# Ab hier wird alles später implementiert:
---
## 7. SEARCH_RESULT (Gespeicherte Suchergebnisse)

```
SEARCH_RESULT
├── id (PK, Long)
├── accountId (FK, Long) → ACCOUNT.id
├── cartId (FK, Long) → CART.id
├── searchName (String) - Benutzerdefinierter Name
├── searchDate (Timestamp)
├── totalResults (Integer)
├── bestPrice (BigDecimal)
├── bestShop (String)
├── createdAt (Timestamp)
└── updatedAt (Timestamp)
```

**Relationen:**
- `N:1` zu ACCOUNT
- `N:1` zu CART
- `1:N` zu PRODUCT_OFFER (Ein Search Result hat mehrere Angebote)

---


## 8. FAVORITE_SHOP (Favoriten-Shops)

```
FAVORITE_SHOP
├── id (PK, Long)
├── accountId (FK, Long) → ACCOUNT.id
├── shopName (String)
├── shopUrl (String)
├── notes (Text)
├── preferInSearch (Boolean)
├── createdAt (Timestamp)
└── updatedAt (Timestamp)
```

**Relationen:**
- `N:1` zu ACCOUNT

---

## 9. BLACKLISTED_SHOP (Blockierte Shops)

```
BLACKLISTED_SHOP
├── id (PK, Long)
├── accountId (FK, Long) → ACCOUNT.id
├── shopName (String)
├── shopUrl (String)
├── reason (Text)
├── createdAt (Timestamp)
└── updatedAt (Timestamp)
```

**Relationen:**
- `N:1` zu ACCOUNT

---

## 10. ADMIN_PARTNER_SHOP (Verifizierte Partner-Shops - Admin-verwaltet)

```
ADMIN_PARTNER_SHOP
├── id (PK, Long)
├── shopName (String)
├── shopUrl (String)
├── apiType (Enum: AMAZON, EBAY, IDEALO, CUSTOM_API, NONE)
├── apiKey (String, encrypted)
├── apiEndpoint (String)
├── isActive (Boolean)
├── priority (Integer) - Bevorzugung bei Suche
├── commission (BigDecimal) - Affiliate-Provision
├── createdBy (FK, Long) → ACCOUNT.id (Admin)
├── createdAt (Timestamp)
└── updatedAt (Timestamp)
```

**Relationen:**
- `N:1` zu ACCOUNT (Admin, der den Shop angelegt hat)

---

## 11. SEARCH_PRIORITY (Account-Suchprioritäten)

```
SEARCH_PRIORITY
├── id (PK, Long)
├── accountId (FK, Long) → ACCOUNT.id
├── priorityType (Enum: LOWEST_PRICE, SHIPPING_COST, DELIVERY_TIME, PAYMENT_METHOD, SHOP_RATING, FAVORITE_SHOPS)
├── weight (Integer) - 1-10, Gewichtung
├── isActive (Boolean)
├── createdAt (Timestamp)
└── updatedAt (Timestamp)
```

**Relationen:**
- `N:1` zu ACCOUNT

---

## Zusätzliche Hilfstabellen (Optional)

### 12. SEARCH_LOG (Für Admin-Statistiken)

```
SEARCH_LOG
├── id (PK, Long)
├── accountId (FK, Long) → ACCOUNT.id
├── searchQuery (String)
├── resultsCount (Integer)
├── selectedOffer (FK, Long) → PRODUCT_OFFER.id (nullable)
├── timestamp (Timestamp)
└── durationMs (Long)
```

### 13. EMAIL_NOTIFICATION (Email-Versand-Historie)

```
EMAIL_NOTIFICATION
├── id (PK, Long)
├── accountId (FK, Long) → ACCOUNT.id
├── type (Enum: CART_SUMMARY, SEARCH_RESULT, PRICE_ALERT)
├── recipient (String)
├── subject (String)
├── sentAt (Timestamp)
└── status (Enum: SENT, FAILED, PENDING)
```

---

## Enums Übersicht

### BikeType
- MTB
- MTB_FULLY
- TREKKING_BIKE
- RACING_BIKE

### BikepartType
- WHEEL_29
- WHEEL_27
- CHAIN
- CASSETTE
- BRAKE_HYDRAULIC
- BRAKE_V_BRAKE
- TIRE_29
- TIRE_27
- GEAR_SHIFT
- PEDAL
- SADDLE
- HANDLEBAR

### UserRole
- USER
- ADMIN

### PaymentMethod
- KLARNA
- PAYPAL
- CREDIT_CARD
- BANK_TRANSFER
- SOFORT

### Availability
- IN_STOCK
- LOW_STOCK
- OUT_OF_STOCK
- PREORDER

### DataSource
- AMAZON
- EBAY
- IDEALO
- WEB_SCRAPING
- CUSTOM_API

### SearchPriorityType
- LOWEST_PRICE
- SHIPPING_COST
- DELIVERY_TIME
- PAYMENT_METHOD
- SHOP_RATING
- FAVORITE_SHOPS

---

## Entity-Relationship-Diagramm (Textform)

```
ACCOUNT (1) ──────< (N) BIKE
BIKE    (1) ──────< (N) BIKEPART
ACCOUNT (1) ──────── (1) CART
CART    (1) ──────< (N) CART_ITEM
BIKEPART (N) ──< (N) CART_ITEM (über bikepartId)
ACCOUNT (1) ──────< (N) SEARCH_RESULT
CART    (1) ──────< (N) SEARCH_RESULT
SEARCH_RESULT (1) ──< (N) PRODUCT_OFFER
CART_ITEM (1) ────< (N) PRODUCT_OFFER
ACCOUNT (1) ──────< (N) FAVORITE_SHOP
ACCOUNT (1) ──────< (N) BLACKLISTED_SHOP
ACCOUNT (1) ──────< (N) SEARCH_PRIORITY
ACCOUNT (Admin) (1) ──< (N) ADMIN_PARTNER_SHOP
```

---

## Wichtige Hinweise zur Implementierung

1. **Cascade-Operationen:** 
   - ACCOUNT → BIKE → BIKEPART (Cascade DELETE)
   - CART → CART_ITEM (Cascade DELETE)
   - SEARCH_RESULT → PRODUCT_OFFER (Cascade DELETE)

2. **Indizes:**
   - ACCOUNT.email (unique)
   - BIKE.accountId, BIKEPART.bikeId
   - CART.accountId (unique), CART_ITEM.cartId
   - PRODUCT_OFFER.searchResultId

3. **Soft Delete:** 
   - Optional für ACCOUNT, BIKE (isActive/isDeleted Flag)

4. **Timestamps:**
   - Alle Entities mit createdAt
   - Updates: updatedAt (automatisch via JPA @PreUpdate)

---

**Stand:** 18. Februar 2026
