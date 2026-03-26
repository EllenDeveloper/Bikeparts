package com.bikeparts.llama.client;

import com.bikeparts.llama.LlamaPromptUtils;
import com.bikeparts.price.entity.ProductOffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

/**
 * Gemeinsame HTTP-Hilfsmethoden fuer die Kommunikation mit dem llama-server.
 *
 * <p>Wird von {@link LlamaHttpClientMain} (Java-SE-Testprogramm) und
 * {@code LlamaHttpClientService} (Spring-Service) gemeinsam genutzt,
 * um Codeduplizierung zu vermeiden.</p>
 */
public class LlamaHttpUtils {

    private static final Logger log = LoggerFactory.getLogger(LlamaHttpUtils.class);

    /**
     * Timeout fuer HTTP-Anfragen in Sekunden.
     */
    public static final int TIMEOUT_SEKUNDEN = 60;

    private LlamaHttpUtils() {
    }


    /**
     * Liest die Server-URL aus {@code application.properties}.
     *
     * <p>Kombiniert {@code llama.server.serverBaseUrl} und {@code llama.server.port}
     * zum vollstaendigen /completion-Endpunkt.</p>
     *
     * @return vollstaendige URL des /completion-Endpunkts
     * @throws IOException wenn {@code application.properties} nicht geladen werden kann
     */
    private static String ladeServerUrl() throws IOException {
        Properties props = new Properties();
        try (InputStream in = LlamaHttpUtils.class.getResourceAsStream("/application.properties")) {
            props.load(in);
        }
        String serverBaseUrl = props.getProperty("llama.server.serverBaseUrl", "http://localhost");
        int port = Integer.parseInt(props.getProperty("llama.server.port", "8080"));
        return serverBaseUrl + ":" + port + "/completion";
    }

    /**
     * Filtert, sortiert und bewertet eine ProductOffers-Liste mit dem llama-server.
     *
     * <p>Liest die Server-URL aus {@code application.properties}, baut den Completion-Request,
     * sendet ihn per HTTP POST und gibt die gefundene Produkt-ID zurueck.</p>
     *
     * @param searchQuery   die Suchanfrage
     * @param productOffers die zu bewertende Produktliste
     * @return gefundene Produkt-ID als String
     * @throws IOException          bei Netzwerkfehlern oder wenn {@code application.properties}
     *                              nicht geladen werden kann
     * @throws InterruptedException wenn der HTTP-Aufruf unterbrochen wird
     */
    public static String callLlama(String searchQuery, List<ProductOffer> productOffers) throws IOException, InterruptedException {
        String serverUrl = ladeServerUrl();
        //List<ProductOffer> filteredOffers = preFilter(productOffers, searchQuery);
        List<ProductOffer> filteredOffers = LlamaPromptUtils.preFilterAndSort(searchQuery, productOffers);

        log.debug("filteredOffers: {}", filteredOffers);
        String systemPrompt = LlamaPromptUtils.getSystemPrompt(searchQuery, filteredOffers);

        // LlamaCompletionRequest mit Standardkonfiguration (Thinking deaktiviert)
        LlamaCompletionRequest request = LlamaCompletionRequest.buildRequest(systemPrompt, LlamaPromptUtils.chatMLToken_UserPart);

        log.debug("=== LlamaRequest Test ===");
        log.debug("serverUrl: {}, temperature: {}, nPredict: {}, cachePrompt: {}, enableThinking: {}",
                serverUrl, request.getTemperature(), request.getNPredict(),
                request.isCachePrompt(), request.isEnableThinking());
        log.debug("jsonPayload: {}", request.toJson());

        String antwort = LlamaHttpUtils.sendeAnfrage(request, serverUrl);

        log.debug("--- Rohantwort vom Server ---");
        log.debug(antwort);

        // Ergebnis-ID aus der JSON-Antwort extrahieren
        String foundValue = LlamaHttpUtils.extrahiereContent(antwort);
        log.debug("--- foundValue ---");
        log.debug("foundValue \"" + foundValue + "\"  ");
        // ID aus der Antwort extrahieren (Format: id=[3], ...) und gegen Liste prüfen
        String idFromResponse = foundValue.replaceAll(".*id=\\[?(\\d+)\\]?.*", "$1");
        boolean idFound = filteredOffers.stream()
                .anyMatch(offer -> idFromResponse.equals(String.valueOf(offer.getId())));
        if (!idFound) {
            log.warn("Warnung: KI hat eine ID erfunden, die nicht im Filter war!");
        }
        // TODO logging über logger
        log.debug("Zurückgegebener Wert: " + idFromResponse);
        return idFromResponse;
    }

    /**
     * Sendet den {@link LlamaCompletionRequest} per HTTP POST an den llama-server.
     *
     * @param request   der vorbereitete LlamaCompletionRequest
     * @param serverUrl vollstaendige URL des /completion-Endpunkts
     * @return Rohantwort des Servers als JSON-String
     * @throws IOException          bei Netzwerkfehlern oder unerwartetem HTTP-Statuscode
     * @throws InterruptedException wenn der Thread unterbrochen wird
     */
    public static String sendeAnfrage(LlamaCompletionRequest request, String serverUrl)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SEKUNDEN))
                .build();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl))
                .timeout(Duration.ofSeconds(TIMEOUT_SEKUNDEN))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(request.toJson()))
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Unerwarteter HTTP-Statuscode: " + response.statusCode()
                    + " - Body: " + response.body());
        }

        return response.body();
    }

    /**
     * Extrahiert den "content"-Wert aus der JSON-Antwort des llama-servers.
     * Beispiel: {"content":"10","...} -> "10"
     *
     * <p>Einfaches String-Parsing ohne externe JSON-Bibliothek.
     *
     * @param jsonAntwort rohe JSON-Antwort des Servers
     * @return extrahierter content-Wert oder Fehlermeldung
     */
    public static String extrahiereContent(String jsonAntwort) {
        String schluessel = "\"content\":\"";
        int start = jsonAntwort.indexOf(schluessel);
        if (start == -1) {
            return "(content-Feld nicht gefunden in Antwort)";
        }
        start += schluessel.length();
        int ende = jsonAntwort.indexOf("\"", start);
        if (ende == -1) {
            return "(Ende des content-Felds nicht gefunden)";
        }
        return jsonAntwort.substring(start, ende).trim();
    }


}
