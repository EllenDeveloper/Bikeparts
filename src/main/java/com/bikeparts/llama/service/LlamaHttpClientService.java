package com.bikeparts.llama.service;

import com.bikeparts.llama.client.LlamaCompletionRequest;
import com.bikeparts.llama.client.LlamaHttpUtils;
import com.bikeparts.llama.server.LlamaServerManager;
import com.bikeparts.price.entity.ProductOffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * ACHTUNG:
 * Diese Klasse funktioniert auch unter Java SE! Da die HTTP-klassen von java.net verwendet werden.
 * Es wurden absichtlich nicht die Klassen von SpringBoot (spring-ai-starter-model-ollama) verwendet.
 *
 * <p>Spring-Service fuer die Kommunikation mit dem externen llama-server.
 * Baut einen {@link LlamaCompletionRequest}, sendet ihn per HTTP POST an den
 * {@code /completion}-Endpunkt und extrahiert die KI-Antwort aus der JSON-Response.</p>
 *
 * <p><b>Ablauf:</b>
 * <pre>
 *   accessTry(productOffers)
 *     -> LlamaCompletionRequest.fromProductOffers()   (Prompt zusammenbauen)
 *     -> sendeAnfrage()                               (HTTP POST via java.net.http)
 *     -> extrahiereContent()                          (content-Feld aus JSON lesen)
 * </pre>
 * </p>
 *
 * @see LlamaCompletionRequest
 * @see LlamaServerManager
 */
@Slf4j
@Service
public class LlamaHttpClientService {

    private final LlamaServerManager llamaServerManager;

    /**
     * Erstellt den Service mit dem {@link LlamaServerManager} zur Server-URL-Abfrage.
     *
     * @param llamaServerManager verwaltet den llama-server-Prozess und liefert die Server-URL
     */
    @Autowired
    LlamaHttpClientService(LlamaServerManager llamaServerManager) {
        this.llamaServerManager = llamaServerManager;
    }

    /**
     * Sendet die ProductOffers-Liste an den llama-server und gibt die gefundene Produkt-ID zurueck.
     *
     * <p>Stellt sicher, dass der llama-server laeuft, baut den Completion-Request,
     * schickt ihn per HTTP POST und extrahiert die ID aus der Antwort.</p>
     *
     * @param searchQuery searchQuery
     * @param productOfferBySearchQuery Produktliste
     * @return extrahierte Produkt-ID als String
     * @throws IOException          bei Netzwerkfehlern oder HTTP-Fehlerstatuscodes
     * @throws InterruptedException wenn der HTTP-Aufruf oder der Server-Start unterbrochen wird
     */
    public String getKiSuggestions(String searchQuery, List<ProductOffer> productOfferBySearchQuery) throws IOException, InterruptedException {

        llamaServerManager.startIfNotRunning();
        return LlamaHttpUtils.callLlama(searchQuery, productOfferBySearchQuery);
    }
}
