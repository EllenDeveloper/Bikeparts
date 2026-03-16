package com.bikeparts.service;

import com.bikeparts.entity.Account;
import com.bikeparts.entity.Bikepart;
import com.bikeparts.entity.Cart;
import com.bikeparts.entity.CartItem;
import com.bikeparts.enums.BikepartType;
import com.bikeparts.price.ScrapingConstants;
import com.bikeparts.price.entity.ProductOffer;
import com.bikeparts.price.repository.ProductOfferRepository;
import com.bikeparts.price.service.BikeComponentsScraperService;
import com.bikeparts.price.service.ScrapingResult;
import com.bikeparts.repository.BikepartRepository;
import com.bikeparts.repository.CartItemRepository;
import com.bikeparts.repository.CartRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.SessionScope;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class CartService {
    /** Logger fuer diese Klasse. */
    Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BikepartRepository bikepartRepository;
    private final AccountService accountService;
    private final Account account;
    private final BikeComponentsScraperService bikeComponentsScraperService;
    private final ProductOfferRepository productOfferRepository;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       BikepartRepository bikepartRepository, AccountService accountService, Account account, BikeComponentsScraperService bikeComponentsScraperService, ProductOfferRepository productOfferRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.bikepartRepository = bikepartRepository;
        this.accountService = accountService;
        this.account = account;
        this.bikeComponentsScraperService = bikeComponentsScraperService;
        this.productOfferRepository = productOfferRepository;
    }

    // --- Cart methods

    public Cart getCart(Cart cart) {
//        Optional<Cart> cartById = cartRepository.findById(cart.getId());
//        if (cartById == null) {
//            return createCart(cart);
//        }
//        else {
//            return cartById.orElse();
//        }
        return null;
    }

    public Cart createCart(Cart cart) {
        return cartRepository.save(cart);
    }

    public void deleteCart(Long id) {
        cartRepository.deleteById(id);
    }

    @Transactional
    public void addCartItemToCart(CartItem cartItemDTO, Long cartId) {
        if (cartItemDTO != null) {
            CartItem cartItem = createCartItem(cartItemDTO);
            Cart cart = cartRepository.getCartById(cartId).getFirst();
            cart.addCartItem(cartItem);
        }
    }

    @Transactional
    public void addBikepartToCart(Long bikepartId, Integer quantity) {

        Cart cart = account.getCart();
        if (cart == null) {
            cart = cartRepository.save(new Cart());
        }

        if (bikepartId != null) {
            Bikepart bikepart = bikepartRepository.findBikepartById(bikepartId);

            CartItem cartItem = new CartItem(cart, bikepart, quantity);
            createCartItem(cartItem);
        }
    }

    // --- CartItem methods

    public CartItem createCartItem(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    public void deleteCartItem(Long id) {
        cartItemRepository.deleteById(id);
    }


    @Transactional
    public ScrapingResult searchPriceBikeComponents(Bikepart bikepart) {
        // TODO: Daten richtig importieren
        //
         String searchQuery = bikepart.getBrand() + " " +bikepart.getModel() + " " +bikepart.getSpecificDetails()
         + " " +bikepart.getType().getLabel();
//        String searchQuery = bikepart.getName();
        if (bikepart.getType().toString().contains("TIRE") || bikepart.getType().toString().contains("WHEEL")) {
            searchQuery += " " + bikepart.getTireWidth();
        }
//        später: +bikepart.getAlternativeQualities() != null
        //my Für tests. Da der Server für die Entwicklung immer hoch und runterfährt, bringt der Cache mit
        //my Caffeine nichts. Es sollen die Daten in der DB als cache-Ersatz gespeichert werden.
        List<ProductOffer> cached = productOfferRepository.findBySearchQueryAndFetchedAtAfter(
                searchQuery,
                LocalDateTime.now().minusDays(ScrapingConstants.CACHE_DAYS));
        if (!cached.isEmpty()) {
            log.debug("*** take productOffers from DB-cache! query = {}", searchQuery);
            return ScrapingResult.success(cached);
        }
        log.info("*** take productOffers from Website! query = {}", searchQuery);

        ScrapingResult scrapingResult = bikeComponentsScraperService.search(searchQuery);

        if (scrapingResult.status() == ScrapingResult.ScrapingStatus.SUCCESS) {
            productOfferRepository.saveAllAndFlush(scrapingResult.offers());
            List<ProductOffer> oldData = productOfferRepository.findBySearchQueryAndFetchedAtBefore(
                    searchQuery, LocalDateTime.now().minusDays(ScrapingConstants.CACHE_DAYS));
            if (!oldData.isEmpty()) {
                productOfferRepository.deleteAll(oldData);
                log.debug("*** deleted {} outdated productOffers from DB-cache for query = {}", oldData.size(), searchQuery);
            }
            return scrapingResult;
        }

        // Fehler oder keine Treffer: auf veraltete DB-Daten zurückfallen
        List<ProductOffer> oldData = productOfferRepository.findBySearchQueryAndFetchedAtBefore(
                searchQuery, LocalDateTime.now().minusDays(ScrapingConstants.CACHE_DAYS));
        if (!oldData.isEmpty()) {
            log.warn("Scraping bike-components.de: verwende veraltete DB-Daten für query = {}", searchQuery);
            return ScrapingResult.success(oldData);
        }

        log.warn("Scraping bike-components.de: {} für query = {} - {}", scrapingResult.status(), searchQuery, scrapingResult.errorMessage());
        return scrapingResult;
    }

    public CartItem getCartItem(Cart cart, Long cartItemId) {
        return cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("CartItem nicht gefunden: " + cartItemId));
    }
}
