package com.bikeparts.llama.client;

import com.bikeparts.llama.LlamaPromptUtils;
import com.bikeparts.llama.server.LlamaServerManager;
import com.bikeparts.price.entity.ProductOffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * ACHTUNG:
 * Diese Klasse funktioniert auch unter Java SE! Da die HTTP-klassen von java.net verwendet werden.
 * Es wurden absichtlich nicht die Klassen von SpringBoot (spring-ai-starter-model-ollama) verwendet.
 * <p>
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
//    Logger log = LoggerFactory.getLogger(LlamaHttpClientMain.class.toString());

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
        int port = Integer.parseInt(props.getProperty("llama.server.port", "8099"));
        int threads = Integer.parseInt(props.getProperty("llama.server.threads", "4"));
        int context = Integer.parseInt(props.getProperty("llama.server.context", "2048"));
        int timeout = Integer.parseInt(props.getProperty("llama.server.timeout", "100"));
        String serverUrl = "http://localhost:" + port + "/completion";

        // llama-server pruefen und ggf. starten
        LlamaServerManager serverManager = new LlamaServerManager.Builder()
                .serverExe(serverExe)
                .modelPath(modelPath)
                .serverBaseUrl(serverBaseUrl)
                .serverUrl(serverUrl)
                .port(port)
                .threads(threads)
                .contextGroesse(context)
                .startTimeoutSekunden(timeout)
                .build();
        serverManager.startIfNotRunning();

        // Test-ProductOffers aus QwenOnePrompt
        String productOffersString = "id=8, productName=Shimano 105 / SLX / CN-HG601-11 11-fach E-Bike Quick-Link Kette, price=24.99 \n"
                + "id=7, productName=Shimano SLX / 105 / E-Bike Quick-Link Kette CN-M7100 12-fach, price=20.990053 \n"
                + "id=6, productName=Shimano SLX Kassette CS-M7000-11 11-fach, price=49.989996000000005 \n"
                + "id=10, productName=Shimano XT / XTR / SLX CN-HG95 10-fach Kette , price=19.989977000000003 \n"
                + "id=5, productName=Shimano SLX Kassette CS-M7100-12 12-fach, price=77.989982 \n"
                + "id=4, productName=Shimano SLX Schaltgriff SL-M7100 mit Klemmschelle 12-fach, price=24.99 \n"
                + "id=9, productName=Shimano 105 / SLX / E-Bike Kette CN-HG601-11 11-fach, price=19.989977000000003 \n"
                + "id=3, productName=Shimano SLX Kassette CS-M7100-12 + Kette CN-M7100 12-fach Verschleißset, price=72.989959 \n";
        List<ProductOffer> productOffers = LlamaPromptUtils.getProductOffersFromString(productOffersString);

        String searchQuery = "Kette Shimano SLX 10-fach";


        LlamaHttpUtils.callLlama(searchQuery, productOffers);
    }

}
