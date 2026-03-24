package com.bikeparts.llama.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Gemeinsame HTTP-Hilfsmethoden fuer die Kommunikation mit dem llama-server.
 *
 * <p>Wird von {@link LlamaHttpClientMain} (Java-SE-Testprogramm) und
 * {@code LlamaHttpClientService} (Spring-Service) gemeinsam genutzt,
 * um Codeduplizierung zu vermeiden.</p>
 */
public class LlamaHttpUtils {

    /** Timeout fuer HTTP-Anfragen in Sekunden. */
    public static final int TIMEOUT_SEKUNDEN = 60;

    private LlamaHttpUtils() {
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
