package com.bikeparts.llama.client;

import com.bikeparts.llama.server.LlamaServerManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;

/**
 * ACHTUNG:
 * Diese Klasse funktioniert auch unter Java SE! Da die HTTP-klassen von java.net verwendet werden.
 * Es wurden absichtlich nicht die Klassen von SpringBoot verwendet.
 *
 * Testprogramm fuer die REST-Kommunikation mit dem llama-server.
 *
 * <p><b>Architektur - zwei Schichten:</b>
 * <ol>
 *   <li><b>Schicht 1 - JSON-Payload bauen ({@link LlamaCompletionRequest}):</b>
 *       Der Builder sammelt alle Inferenz-Parameter (System-Prompt, ProductOffers, temperature usw.)
 *       und serialisiert sie via {@code toJson()} in einen JSON-String. Kein Netzwerk.</li>
 *   <li><b>Schicht 2 - HTTP-Versand ({@link #sendeAnfrage}):</b>
 *       Der fertige JSON-String wird per {@code java.net.http.HttpClient} als HTTP-POST
 *       an den llama-server geschickt. Der Server gibt eine JSON-Antwort zurueck,
 *       aus der das {@code content}-Feld (die KI-Antwort) extrahiert wird.</li>
 * </ol>
 *
 * <p><b>Ablauf:</b>
 * <pre>
 *   Builder              -&gt; LlamaCompletionRequest.toJson()  -&gt; HttpClient.send()  -&gt; extrahiereContent()
 *
 *   (Parameter sammeln)     (JSON-String bauen)        (HTTP POST)           (ID auslesen)
 * </pre>
 *
 * <p><b>Voraussetzung:</b> llama-server laeuft auf localhost:8080.
 * Starten mit:
 * <pre>
 *   ./llama-server.exe -m "./models/qwen2.5-1.5b-instruct-q4_k_m.gguf"
 *                      --port 8080 -c 2048 --threads 4
 * </pre>
 */
public class LlamaHttpClientMain {
    
    /** Timeout fuer die HTTP-Anfrage in Sekunden. */
    private static final int TIMEOUT_SEKUNDEN = 30;

    /** System-Prompt: KI als deterministischer Filter-Algorithmus (identisch zu QwenOnePrompt). */
    public static final String SYSTEM_PROMPT =
            "Du bist ein Filter-Algorithmus. Gib NUR die numerische ID aus. "
            + "Bedingungen: Kette, SLX, 10-fach, inStock=true. Günstigster Preis gewinnt.";

    /**
     * Einstiegspunkt des Testprogramms.
     *
     * <p>Laedt die Konfiguration aus {@code application.properties}, stellt sicher dass
     * der llama-server laeuft, baut einen {@link LlamaCompletionRequest} mit einer
     * festen Test-ProductOffers-Liste und gibt die vom Modell gefundene Produkt-ID aus.</p>
     *
     * @param args werden nicht ausgewertet
     * @throws IOException          bei Netzwerkfehlern oder wenn {@code application.properties}
     *                              nicht geladen werden kann
     * @throws InterruptedException wenn der HTTP-Aufruf oder der Server-Start unterbrochen wird
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        // Konfiguration aus application.properties laden
        Properties props = new Properties();
        try (InputStream eingabe = LlamaHttpClientMain.class
                .getResourceAsStream("/application.properties")) {
            props.load(eingabe);
        }

        String serverExe = props.getProperty("llama.server.exe");
        String serverBaseUrl = props.getProperty("llama.server.serverBaseUrl");
        String modelPath = props.getProperty("llama.model.path");
        int port         = Integer.parseInt(props.getProperty("llama.server.port", "8080"));
        int threads      = Integer.parseInt(props.getProperty("llama.server.threads", "4"));
        int context      = Integer.parseInt(props.getProperty("llama.server.context", "2048"));
        int timeout      = Integer.parseInt(props.getProperty("llama.server.timeout", "100"));
        String serverUrl = "http://localhost:" + port + "/completion";

        // llama-server pruefen und ggf. starten
        LlamaServerManager serverManager = new LlamaServerManager.Builder()
                .serverExe(serverExe)
                .modelPath(modelPath)
                .serverBaseUrl(serverBaseUrl)
                .port(port)
                .threads(threads)
                .contextGroesse(context)
                .startTimeoutSekunden(timeout)
                .build();
        serverManager.startIfNotRunning();

        // Test-ProductOffers aus QwenOnePrompt
        String productOffers = "id=8, productName=Shimano 105 / SLX / CN-HG601-11 11-fach E-Bike Quick-Link Kette, shopName=bike-components.de, price=24.99, inStock=true \n"
                + "id=7, productName=Shimano SLX / 105 / E-Bike Quick-Link Kette CN-M7100 12-fach, shopName=bike-components.de, price=20.990053, inStock=true \n"
                + "id=6, productName=Shimano SLX Kassette CS-M7000-11 11-fach, shopName=bike-components.de, price=49.989996000000005, inStock=true \n"
                + "id=10, productName=Shimano XT / XTR / SLX CN-HG95 10-fach Kette , shopName=bike-components.de, price=19.989977000000003, inStock=true \n"
                + "id=5, productName=Shimano SLX Kassette CS-M7100-12 12-fach, shopName=bike-components.de, price=77.989982, inStock=true \n"
                + "id=4, productName=Shimano SLX Schaltgriff SL-M7100 mit Klemmschelle 12-fach, shopName=bike-components.de, price=24.99, inStock=true \n"
                + "id=9, productName=Shimano 105 / SLX / E-Bike Kette CN-HG601-11 11-fach, shopName=bike-components.de, price=19.989977000000003, inStock=false \n"
                + "id=3, productName=Shimano SLX Kassette CS-M7100-12 + Kette CN-M7100 12-fach Verschleißset, shopName=bike-components.de, price=72.989959, inStock=true \n";

        // LlamaCompletionRequest mit Standardkonfiguration (Thinking deaktiviert)
        LlamaCompletionRequest request = LlamaCompletionRequest.fromProductOffers(SYSTEM_PROMPT, productOffers);

        System.out.println("=== LlamaRequest Test ===");
        System.out.println("serverUrl: " + serverUrl);
        System.out.println("temperature: " + request.getTemperature());
        System.out.println("nPredict: " + request.getNPredict());
        System.out.println("enableThinking: " + request.isEnableThinking());
        System.out.println("--- JSON-Payload ---");
        System.out.println(request.toJson());
        System.out.println("--- Sende Anfrage ---");

        String antwort = sendeAnfrage(request, serverUrl);

        System.out.println("--- Rohantwort vom Server ---");
        System.out.println(antwort);

        // Ergebnis-ID aus der JSON-Antwort extrahieren
        String gefundeneId = extrahiereContent(antwort);
        System.out.println("--- Gefundene ID ---");
        System.out.println("gefundeneId: " + gefundeneId);
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
