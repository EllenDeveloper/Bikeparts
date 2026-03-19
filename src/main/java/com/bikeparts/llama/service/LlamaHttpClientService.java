package com.bikeparts.llama.service;

import com.bikeparts.llama.client.LlamaCompletionRequest;
import com.bikeparts.llama.server.LlamaServerManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * ACHTUNG:
 * Diese Klasse funktioniert auch unter Java SE! Da die HTTP-klassen von java.net verwendet werden.
 * Es wurden absichtlich nicht die Klassen von SpringBoot (spring-ai-starter-model-ollama) verwendet.
 *
 * <p>Spring-Service fuer die Kommunikation mit dem externen llama-server.
 * Baut einen {@link LlamaCompletionRequest}, sendet ihn per HTTP POST an den
 * {@code /completion}-Endpunkt und extrahiert die KI-Antwort aus dem JSON-Response.</p>
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

    /** Timeout fuer die HTTP-Anfrage in Sekunden. */
    private static final int TIMEOUT_SEKUNDEN = 30;

    /**
     * System-Prompt fuer den llama-server.
     * Konfiguriert die KI als deterministischen Filter-Algorithmus (identisch zu QwenOnePrompt).
     */
    public static final String SYSTEM_PROMPT =
            "Du bist ein Filter-Algorithmus. Gib NUR die numerische ID aus. "
                    + "Bedingungen: Kette, SLX, 10-fach, inStock=true. Günstigster Preis gewinnt.";

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
     * @param productOffers Produktliste im Format "id=X, productName=..., price=..., inStock=..."
     * @return extrahierte Produkt-ID als String
     * @throws IOException          bei Netzwerkfehlern oder HTTP-Fehlerstatuscodes
     * @throws InterruptedException wenn der HTTP-Aufruf oder der Server-Start unterbrochen wird
     */
    public String accessTry(String productOffers) throws IOException, InterruptedException {

        llamaServerManager.startIfNotRunning();

        // LlamaCompletionRequest mit Standardkonfiguration (Thinking deaktiviert)
        LlamaCompletionRequest request = LlamaCompletionRequest.fromProductOffers(SYSTEM_PROMPT, productOffers);

        System.out.println("=== LlamaRequest Test ===");
//        System.out.println("serverUrl: " + request.getServerUrl());
        System.out.println("temperature: " + request.getTemperature());
        System.out.println("nPredict: " + request.getNPredict());
        System.out.println("enableThinking: " + request.isEnableThinking());
        System.out.println("--- JSON-Payload ---");
        System.out.println(request.toJson());
        System.out.println("--- Sende Anfrage ---");

        String antwort = sendeAnfrage(request, llamaServerManager.getServerUrl());

        System.out.println("--- Rohantwort vom Server ---");
        System.out.println(antwort);

        // Ergebnis-ID aus der JSON-Antwort extrahieren
        String gefundeneId = extrahiereContent(antwort);
        System.out.println("--- Gefundene ID ---");
        System.out.println("gefundeneId: " + gefundeneId);
        return gefundeneId;
    }

    /**
     * Sendet den {@link LlamaCompletionRequest} per HTTP POST an den llama-server.
     *
     * @param request   der vorbereitete LlamaCompletionRequest
     * @param serverUrl vollstaendige URL des /completion-Endpunkts
     * @return Rohantwort des Servers als JSON-String
     * @throws IOException          bei Netzwerkfehlern
     * @throws InterruptedException wenn der Thread unterbrochen wird
     */
    private static String sendeAnfrage(LlamaCompletionRequest request, String serverUrl)
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
    private static String extrahiereContent(String jsonAntwort) {
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
