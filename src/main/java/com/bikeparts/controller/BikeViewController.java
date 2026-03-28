package com.bikeparts.controller;

import com.bikeparts.entity.*;
import com.bikeparts.price.entity.ProductOffer;
import com.bikeparts.price.service.ScrapingResult;
import com.bikeparts.service.AccountService;
import com.bikeparts.service.BikeService;
import com.bikeparts.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MVC-Controller für die View-Layer des BikePartsFinder.
 *
 * <p>GraalVM Native Image: {@code @RegisterReflectionForBinding} registriert
 * {@link ScrapingResult} und das innere Enum {@link ScrapingResult.ScrapingStatus}
 * für Reflection, damit Thymeleaf/SpEL im Native Image auf Properties wie
 * {@code shopName}, {@code status} und {@code offers} zugreifen kann.</p>
 */
@RegisterReflectionForBinding({ScrapingResult.class, ScrapingResult.ScrapingStatus.class})
@Controller
public class BikeViewController {
    private final BikeService bikeService;
    private final CartService cartService;
    private final Account account;
    private final Logger log = LoggerFactory.getLogger(BikeViewController.class);
    private final AccountService accountService;

    @Autowired
    public BikeViewController(BikeService bikeService, CartService cartService,
                              Account account, AccountService accountService) {
        this.bikeService = bikeService;
        this.cartService = cartService;
        this.account = account;
        this.accountService = accountService;
    }

    @GetMapping("/bikes")
    public String showBikes(
            Model model) {
        List<Bike> bikes = bikeService.getAllBikes();

        model.addAttribute("bikes", bikes);
        return "bikes-list";
    }

    @GetMapping("/bikes/{bikeId}")
    public String showBike(
            @PathVariable Long bikeId,
            Model model) {
        Bike bike = bikeService.getBikeById(bikeId);

        model.addAttribute("bike", bike);
        return "bike-details";
    }

    @GetMapping("/bikes/{bikeId}/bikeparts")
    public String showBikeparts(
            @PathVariable Long bikeId,
            Model model) {
        Bike bike = bikeService.getBikeById(bikeId);
        if (!bike.getAccount().getId().equals(account.getId())) {
            throw new RuntimeException("Zugriff verweigert");
        }
//        model.addAttribute("bikeparts", bikeService.getAllBikeparts(bikeId));
//        model.addAttribute("accountId", account.getId());
//        Bikepart bikepart = bikeService.getBikepartById(bikeId);
        List<Bikepart> allBikeparts = bikeService.getAllBikeparts(bikeId);
        model.addAttribute("bikeparts", allBikeparts);
        model.addAttribute("accountId", account.getId());
        model.addAttribute("bikeId", bikeId);
        model.addAttribute("bikepartsSize", allBikeparts.size());
        return "bikeparts-list";
    }

    @GetMapping("/bikeparts/{id}")
    public String showBikepart(
            @PathVariable Long id,
            Model model) {
        model.addAttribute("bikepart", bikeService.getBikepartById(id));
        return "bikepart-details";
    }

    @PostMapping("/bikeparts/{id}/addBikepartToCart")
    public String addBikepartToCart(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer quantity,
            Model model) {

        cartService.addBikepartToCart(id, quantity);
        Bikepart bikepart = bikeService.getBikepartById(id);
        Long bikeId = bikepart.getBike().getId();
        List<Bikepart> allBikeparts = bikeService.getAllBikeparts(bikeId);
        model.addAttribute("bikeparts", allBikeparts);
        model.addAttribute("accountId", account.getId());
        model.addAttribute("bikeId", bikeId);
        model.addAttribute("bikepartsSize", allBikeparts.size());
        return showBikeparts(bikeId, model);
//        return "bikeparts-list";
    }

    @GetMapping("/cart")
    public String showCart(
            Model model) {
        Cart cart = account.getCart();
        // cart.getId() ist auf einem Hibernate-Proxy immer sicher lesbar.
        // cart.getCartItems() wuerde auf einem detached Proxy eine LazyInitializationException werfen.
        Long cartId = cart != null ? cart.getId() : null;
        model.addAttribute("cart", cart);
        model.addAttribute("cartItems", cartService.getCartItemsByCartId(cartId));
        return "cart";
    }

    @GetMapping("/cart/cartItem/{id}/searchPrice")
    public String searchPrice(
            @PathVariable Long id,
            Model model) {
        log.debug("searchPrice");

        CartItem cartItem = cartService.getCartItem(id);
        List<ScrapingResult> scrapingResults = cartService.searchPrice(cartItem.getBikepart());
        model.addAttribute("cartItem", cartItem);
        model.addAttribute("bikepartName", cartItem.getBikepart().getName());
        model.addAttribute("scrapingResults", scrapingResults);
        return "price-search-result";
    }

    /**
     * Zeigt alle Preisergebnisse fuer alle Artikel im aktiven Warenkorb.
     *
     * <p>Fuer jeden {@link CartItem} werden die Scraping-Ergebnisse aller Shops
     * sowie der KI-Vorschlag ermittelt und per ID als Maps ans Template uebergeben.</p>
     *
     * @param model Thymeleaf-Model
     * @return Template-Name {@code all-prices}
     */
    @GetMapping("/cart/allPrices")
    public String allPrices(Model model) {
        log.debug("allPrices");

        Cart cart = account.getCart();
        Long cartId = cart != null ? cart.getId() : null;
        List<CartItem> cartItems = cartService.getCartItemsByCartId(cartId);

        Map<Long, List<ScrapingResult>> scrapingResultsByCartItemId = new HashMap<>();
        Map<Long, ProductOffer> kiSuggestionsByCartItemId = new HashMap<>();

        for (CartItem cartItem : cartItems) {
            List<ScrapingResult> results = cartService.searchPrice(cartItem.getBikepart());
            ProductOffer kiSuggestion = cartService.kiSuggestionsForBikepart(cartItem.getBikepart().getId());
            scrapingResultsByCartItemId.put(cartItem.getId(), results);
            kiSuggestionsByCartItemId.put(cartItem.getId(), kiSuggestion);
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("scrapingResultsByCartItemId", scrapingResultsByCartItemId);
        model.addAttribute("kiSuggestionsByCartItemId", kiSuggestionsByCartItemId);
        return "all-prices";
    }
}
