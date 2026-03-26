package com.bikeparts.service;

import com.bikeparts.entity.Account;
import com.bikeparts.entity.Bikepart;
import com.bikeparts.entity.Cart;
import com.bikeparts.entity.CartItem;
import com.bikeparts.llama.service.LlamaHttpClientService;
import com.bikeparts.price.ScrapingConstants;
import com.bikeparts.price.entity.ProductOffer;
import com.bikeparts.price.entity.ShopInfo;
import com.bikeparts.price.repository.ProductOfferRepository;
import com.bikeparts.price.repository.ShopInfoRepository;
import com.bikeparts.price.service.BikeComponentsScraperService;
import com.bikeparts.price.service.BikeDiscountScraperService;
import com.bikeparts.price.service.ScraperShopInterface;
import com.bikeparts.price.service.ScrapingResult;
import com.bikeparts.repository.AccountRepository;
import com.bikeparts.repository.BikepartRepository;
import com.bikeparts.repository.CartItemRepository;
import com.bikeparts.repository.CartRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartService {
    private final BikeDiscountScraperService bikeDiscountScraperService;
    private final AccountRepository accountRepository;
    private final ShopInfoRepository shopInfoRepository;
    /**
     * Logger fuer diese Klasse.
     */
    Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BikepartRepository bikepartRepository;
    private final Account account;
    private final BikeComponentsScraperService bikeComponentsScraperService;
    private final ProductOfferRepository productOfferRepository;
    private final LlamaHttpClientService llamaHttpClientService;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       BikepartRepository bikepartRepository, Account account,
                       BikeComponentsScraperService bikeComponentsScraperService,
                       ProductOfferRepository productOfferRepository,
                       LlamaHttpClientService llamaHttpClientService, BikeDiscountScraperService bikeDiscountScraperService, AccountRepository accountRepository, ShopInfoRepository shopInfoRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.bikepartRepository = bikepartRepository;
        this.account = account;
        this.bikeComponentsScraperService = bikeComponentsScraperService;
        this.productOfferRepository = productOfferRepository;
        this.llamaHttpClientService = llamaHttpClientService;
        this.bikeDiscountScraperService = bikeDiscountScraperService;
        this.accountRepository = accountRepository;
        this.shopInfoRepository = shopInfoRepository;
    }

    // --- Cart methods

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

        Cart cart = accountRepository.findById(account.getId())
                .map(Account::getCart)
                .orElse(null);
        if (cart == null) {
            cart = cartRepository.save(new Cart());
            account.setCart(cart);
            accountRepository.save(account);  // FK account.cart_id in DB persistieren
        }

        if (bikepartId != null) {
            Bikepart bikepart = bikepartRepository.findBikepartById(bikepartId);

            CartItem cartItem = new CartItem(cart, bikepart, quantity);
            createCartItem(cartItem);
        }
    }

    // --- CartItem methods

    /**
     * Laedt alle CartItems eines Warenkorbs direkt ueber die Cart-ID aus der DB.
     *
     * <p>Vermeidet {@code LazyInitializationException}: statt {@code cart.getCartItems()}
     * auf einem ggf. detached Proxy, wird direkt per {@code CartItemRepository.findByCartId}
     * abgefragt. Die Cart-ID ist auf einem Hibernate-Proxy immer sicher lesbar.</p>
     *
     * @param cartId ID des Warenkorbs, oder {@code null}
     * @return Liste der CartItems, oder leere Liste wenn cartId null ist
     */
    public List<CartItem> getCartItemsByCartId(Long cartId) {
        if (cartId == null) return List.of();
        return cartItemRepository.findByCartIdWithBikepart(cartId);
    }

    public CartItem createCartItem(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    public void deleteCartItem(Long id) {
        cartItemRepository.deleteById(id);
    }

    private String getSearchQuery(Bikepart bikepart) {
        String searchQuery = bikepart.getBrand() + " " + bikepart.getModel() + " " + bikepart.getSpecificDetails()
                + " " + bikepart.getType().getLabel();
//        String searchQuery = bikepart.getName();
        //        später: +bikepart.getAlternativeQualities() != null
        if (bikepart.getType().toString().contains("TIRE") || bikepart.getType().toString().contains("WHEEL")) {
            searchQuery += " " + bikepart.getTireWidth();
        }
        return searchQuery;
    }


    @Transactional
    public List<ScrapingResult> searchPrice(Bikepart bikepart) {
        // TODO: Daten richtig importieren
        //
        String searchQuery = getSearchQuery(bikepart);
        List<ScrapingResult> results = new ArrayList<>();

        //my extra für suche mit shopName. In produktiv muss das die shopId sein!
        String[] shopNames = {ScrapingConstants.BikeComponents.SHOP_NAME, ScrapingConstants.BikeDiscount.SHOP_NAME};
        ScraperShopInterface[] scraperShopInterfaces = {bikeComponentsScraperService, bikeDiscountScraperService};
        for (int i = 0; i < shopNames.length; i++) {

            ScrapingResult scrapingResult = scrapeShopOrGetDataFromDatabase(searchQuery, shopNames[i], scraperShopInterfaces[i]);
            results.add(scrapingResult);
        }

        return results;
    }

    private @NonNull ScrapingResult scrapeShopOrGetDataFromDatabase(
            String searchQuery, String shopName, ScraperShopInterface scraperShopInterface) {
        //my Für tests. Da der Server für die Entwicklung immer hoch und runterfährt, bringt der Cache mit
        //my Caffeine nichts. Es sollen die Daten in der DB als cache-Ersatz gespeichert werden.
        List<ProductOffer> cached = productOfferRepository.findBySearchQueryAndShopNameAndFetchedAtAfter(
                searchQuery, shopName,
                LocalDateTime.now().minusDays(ScrapingConstants.Common.CACHE_DAYS));
        if (!cached.isEmpty()) {
            log.debug("*** take productOffers from DB-cache! {} query = {}", shopName, searchQuery);
            return ScrapingResult.success(cached, shopName);
        }
        log.info("*** take productOffers from Website! {} query = {}", shopName, searchQuery);

        ScrapingResult scrapingResult = scraperShopInterface.search(searchQuery);

        if (scrapingResult.getStatus() == ScrapingResult.ScrapingStatus.SUCCESS) {
            ShopInfo shopInfo = shopInfoRepository.findByShopName(shopName).orElseThrow(() -> new IllegalArgumentException(
                    "Shop nicht gefunden: " + shopName));
            // set shopId to the offers
            scrapingResult.getOffers().forEach(o -> o.setShopId(shopInfo.getId()));
            productOfferRepository.saveAllAndFlush(scrapingResult.getOffers());
            List<ProductOffer> oldData = productOfferRepository.findBySearchQueryAndFetchedAtBefore(
                    searchQuery, LocalDateTime.now().minusDays(ScrapingConstants.Common.CACHE_DAYS));
            if (!oldData.isEmpty()) {
                productOfferRepository.deleteAll(oldData);
                log.debug("*** deleted {} outdated productOffers from DB-cache for query = {}", oldData.size(), searchQuery);
            }
            return scrapingResult;
        }

        // Fehler oder keine Treffer: auf veraltete DB-Daten zurückfallen
        List<ProductOffer> oldData = productOfferRepository.findBySearchQueryAndFetchedAtBefore(
                searchQuery, LocalDateTime.now().minusDays(ScrapingConstants.Common.CACHE_DAYS));
        if (!oldData.isEmpty()) {
            log.warn("Scraping {} verwende veraltete DB-Daten für query = {}", shopName, searchQuery);
            return ScrapingResult.success(oldData, shopName);
        }

        log.warn("Scraping {}: {} für query = {} - {}", shopName, scrapingResult.getStatus(), searchQuery, scrapingResult.getErrorMessage());
        return scrapingResult;
    }

    /**
     * Laedt ein CartItem mit seinem Bikepart direkt aus der DB (JOIN FETCH).
     *
     * <p>Laedt frisch per Repository statt ueber {@code cart.getCartItems()} (Proxy-Zugriff).
     * Bikepart und CartItem werden in einem SQL-Statement geladen - keine offene
     * Hibernate-Session danach erforderlich.</p>
     *
     * @param cartItemId ID des CartItems
     * @return CartItem mit vollstaendig geladenem Bikepart
     * @throws EntityNotFoundException wenn kein CartItem mit dieser ID existiert
     */
    public CartItem getCartItem(Long cartItemId) {
        return cartItemRepository.findByIdWithBikepart(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem nicht gefunden: " + cartItemId));
    }

    @Transactional
    public ProductOffer rateSearchResultsWithKI(Long bikepartId) {
        if (bikepartId != null) {
            Bikepart bikepart = bikepartRepository.findBikepartById(bikepartId);
            String searchQuery = getSearchQuery(bikepart);
            List<ProductOffer> productOfferBySearchQuery = productOfferRepository.findBySearchQuery(searchQuery);

            try {
                if (!productOfferBySearchQuery.isEmpty()) {
                    String result = llamaHttpClientService.rateSearchResult(searchQuery, productOfferBySearchQuery);
                    log.debug("**** " + result);
                    return productOfferRepository.findById(Long.parseLong(result)).orElse(null);
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

//            LlamaCompletionRequest llamaCompletionRequest = LlamaCompletionRequest.fromInventory(LlamaHttpClientMain.SYSTEM_PROMPT, stringForLlama);
        }
        // TODO error handling
        return null;
    }
}
