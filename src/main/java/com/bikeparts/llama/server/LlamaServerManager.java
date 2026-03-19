package com.bikeparts.llama.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
 * <p>Verwaltet den llama-server-Prozess: Pruefen ob er laeuft, starten falls nicht,
 * und warten bis er bereit ist.</p>
 *
 * <p>Die Klasse unterstuetzt zwei Verwendungsarten:
 * <ul>
 *   <li><b>Spring-Bean:</b> Wird automatisch per {@code @Autowired} mit Werten aus
 *       {@code application.properties} befuellt.</li>
 *   <li><b>Manuell via Builder:</b> Fuer den Einsatz ausserhalb des Spring-Kontexts,
 *       z.B. in {@code LlamaHttpClientMain}.</li>
 * </ul>
 * </p>
 *
 * <p><b>Ablauf:</b>
 * <pre>
 *   startIfNotRunning()
 *     -> isRunning()          (GET /health - laeuft er schon?)
 *     -> start()              (ProcessBuilder - neuen Prozess starten)
 *     -> waitUntilReady()     (GET /health pollen bis 200 OK)
 * </pre>
 * </p>
 *
 * <p><b>Verwendung via Builder:</b>
 * <pre>
 *   LlamaServerManager manager = new LlamaServerManager.Builder()
 *       .serverExe("C:/llama.cpp/llama-server.exe")
 *       .serverBaseUrl("http://localhost")
 *       .modelPath("C:/models/qwen2.5-1.5b-instruct-q4_k_m.gguf")
 *       .build();
 *
 *   manager.startIfNotRunning();
 * </pre>
 * </p>
 *
 * @see com.bikeparts.config.LlamaServerConfig
 */
@Component
@Slf4j
public class LlamaServerManager {

    /** Endpunkt fuer den Health-Check des llama-servers. */
    private static final String HEALTH_PFAD = "/health";

    /** Wartezeit zwischen zwei Health-Check-Versuchen in Millisekunden. */
    private static final int POLL_INTERVALL_MS = 500;

    /** Pfad zur llama-server.exe auf dem lokalen Dateisystem. */
    private final String serverExe;

    /** Basis-URL des Servers ohne Port und Pfad, z.B. {@code http://localhost}. */
    private final String serverBaseUrl;

    /** Vollstaendige URL des {@code /completion}-Endpunkts, z.B. {@code http://localhost:8085/completion}. */
    private final String serverUrl;

    /** Pfad zur GGUF-Modelldatei, die der llama-server laedt. */
    private final String modelPath;

    /** HTTP-Port, auf dem der llama-server lauscht. */
    private final int port;

    /** Kontextgroesse in Tokens fuer die Inferenz. */
    private final int contextGroesse;

    /** Anzahl der CPU-Kerne, die der llama-server fuer die Inferenz nutzt. */
    private final int threads;

    /** Maximale Wartezeit in Sekunden, bis der Server nach dem Start als bereit gilt. */
    private final int startTimeoutSekunden;

    /**
     * Steuert, ob der llama-server-Prozess beim Beenden der JVM automatisch gestoppt wird.
     * Konfigurierbar via {@code llama.server.lifecycle.auto-shutdown} in {@code application.properties}.
     */
    private final boolean autoShutdown;

    /**
     * Der gestartete llama-server-Prozess.
     * Ist {@code null}, wenn der Server nicht von dieser Instanz gestartet wurde
     * (z.B. weil er bereits lief).
     */
    private Process serverProzess;

    /**
     * Spring-Konstruktor - Werte werden automatisch aus {@code application.properties} geladen.
     *
     * @param serverExe            Pfad zur llama-server.exe ({@code llama.server.exe})
     * @param serverBaseUrl        Basis-URL des Servers ({@code llama.server.serverBaseUrl})
     * @param modelPath            Pfad zur GGUF-Modelldatei ({@code llama.model.path})
     * @param port                 HTTP-Port ({@code llama.server.port})
     * @param serverUrl            vollstaendige URL des /completion-Endpunkts
     *                             ({@code llama.server.serverBaseUrl}:{@code llama.server.port}/completion)
     * @param contextGroesse       Kontextgroesse in Tokens ({@code llama.server.context})
     * @param threads              Anzahl CPU-Kerne ({@code llama.server.threads})
     * @param startTimeoutSekunden maximale Wartezeit in Sekunden ({@code llama.server.timeout})
     * @param autoShutdown         {@code true} = Prozess wird beim JVM-Ende automatisch beendet
     *                             ({@code llama.server.lifecycle.auto-shutdown})
     */
    @Autowired
    public LlamaServerManager(
            @Value("${llama.server.exe}") String serverExe,
            @Value("${llama.server.serverBaseUrl}") String serverBaseUrl,
            @Value("${llama.model.path}") String modelPath,
            @Value("${llama.server.port}") int port,
            @Value("${llama.server.serverBaseUrl}:${llama.server.port}/completion") String serverUrl,
            @Value("${llama.server.context}") int contextGroesse,
            @Value("${llama.server.threads}") int threads,
            @Value("${llama.server.timeout}") int startTimeoutSekunden,
            @Value("${llama.server.lifecycle.auto-shutdown:false}") boolean autoShutdown) {
        this.serverExe = serverExe;
        this.serverBaseUrl = serverBaseUrl;
        this.modelPath = modelPath;
        this.port = port;
        this.serverUrl = serverUrl;
        this.contextGroesse = contextGroesse;
        this.threads = threads;
        this.startTimeoutSekunden = startTimeoutSekunden;
        this.autoShutdown = autoShutdown;
    }

    /**
     * Privater Builder-Konstruktor - wird ausschliesslich von {@link Builder#build()} aufgerufen.
     *
     * @param builder befuellter Builder mit allen Konfigurationswerten
     */
    private LlamaServerManager(Builder builder) {
        this.serverExe = builder.serverExe;
        this.serverBaseUrl = builder.serverBaseUrl;
        this.modelPath = builder.modelPath;
        this.port = builder.port;
        this.serverUrl = builder.serverUrl;
        this.contextGroesse = builder.contextGroesse;
        this.threads = builder.threads;
        this.startTimeoutSekunden = builder.startTimeoutSekunden;
        this.autoShutdown = builder.autoShutdown;
    }

    /**
     * Gibt die vollstaendige URL des {@code /completion}-Endpunkts zurueck.
     *
     * @return Server-URL, z.B. {@code http://localhost:8085/completion}
     */
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * Prueft ob der llama-server laeuft. Startet ihn falls nicht, und wartet bis
     * er bereit ist.
     *
     * @throws IOException          wenn der Prozess nicht gestartet werden kann
     * @throws InterruptedException wenn das Warten unterbrochen wird
     * @throws IllegalStateException wenn der Server nicht innerhalb des Timeouts bereit wird
     */
    public void startIfNotRunning() throws IOException, InterruptedException {
        if (isRunning()) {
            log.info("llamaServer: laeuft bereits auf Port " + port);
            return;
        }
        log.info("llamaServer: nicht erreichbar - starte Prozess...");
        start();
        waitUntilReady();
        log.info("llamaServer: bereit auf Port " + port);
    }

    /**
     * Prueft ob der llama-server erreichbar ist, indem GET /health aufgerufen wird.
     *
     * @return true wenn der Server antwortet (HTTP 200), sonst false
     */
    public boolean isRunning() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(2))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverBaseUrl + ":" + port + HEALTH_PFAD))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Startet den llama-server als externen Prozess via {@link ProcessBuilder}.
     * Stdout und Stderr des Prozesses werden verworfen, um die Konsole sauber zu halten.
     *
     * <p>Falls {@code llama.server.lifecycle.auto-shutdown=true} konfiguriert ist,
     * wird ein JVM-Shutdown-Hook registriert, der den Prozess beim Beenden der JVM
     * automatisch stoppt.</p>
     *
     * @throws IOException wenn die ausfuehrbare Datei nicht gefunden wird oder
     *                     der Prozess nicht gestartet werden kann
     */
    private void start() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                serverExe,
                "-m", modelPath,
                "--port", String.valueOf(port),
                "-c", String.valueOf(contextGroesse),
                "--threads", String.valueOf(threads)
        );

        // Serverausgabe in Null-Stream umleiten (kein Konsolenmüll)
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);

        this.serverProzess = pb.start();

        if (autoShutdown) {
            // Prozess beim JVM-Ende automatisch beenden
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (serverProzess != null && serverProzess.isAlive()) {
                    log.info("llamaServer: beende Prozess...");
                    serverProzess.destroy();
                }
            }));
        }
    }

    /**
     * Wartet per Polling auf dem Health-Endpunkt, bis der llama-server bereit ist.
     *
     * @throws InterruptedException  wenn das Warten unterbrochen wird
     * @throws IllegalStateException wenn der Server nicht innerhalb von
     *                               {@code startTimeoutSekunden} bereit wird
     */
    private void waitUntilReady() throws InterruptedException {
        long deadline = System.currentTimeMillis() + (startTimeoutSekunden * 1000L);
        while (System.currentTimeMillis() < deadline) {
            if (isRunning()) {
                return;
            }
            Thread.sleep(POLL_INTERVALL_MS);
            System.out.print(".");
        }
        throw new IllegalStateException(
                "llama-server nicht bereit nach " + startTimeoutSekunden + " Sekunden.");
    }

    /**
     * Builder fuer {@link LlamaServerManager}.
     *
     * <p>Standardwerte:
     * <ul>
     *   <li>port: 8080</li>
     *   <li>contextGroesse: 2048</li>
     *   <li>threads: 4</li>
     *   <li>startTimeoutSekunden: 60</li>
     *   <li>autoShutdown: false</li>
     * </ul>
     */
    public static class Builder {

        private String serverExe;
        private String serverBaseUrl;
        private String serverUrl;
        private String modelPath;
        private int port = 8080;
        private int contextGroesse = 2048;
        private int threads = 4;
        private int startTimeoutSekunden = 60;
        private boolean autoShutdown = false;

        /**
         * @param serverExe Pfad zur llama-server.exe
         * @return this Builder
         */
        public Builder serverExe(String serverExe) {
            this.serverExe = serverExe;
            return this;
        }

        /**
         * @param serverBaseUrl Server url z.B. http://localhost
         * @return this Builder
         */
        public Builder serverBaseUrl(String serverBaseUrl) {
            this.serverBaseUrl = serverBaseUrl;
            return this;
        }

        /**
         * @param serverUrl Server url z.B. http://localhost:8080/completion
         * @return this Builder
         */
        public Builder serverUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        /**
         * @param modelPath Pfad zur GGUF-Modelldatei
         * @return this Builder
         */
        public Builder modelPath(String modelPath) {
            this.modelPath = modelPath;
            return this;
        }

        /**
         * @param port HTTP-Port des llama-servers (Standard: 8080)
         * @return this Builder
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * @param contextGroesse Kontextgroesse in Tokens (Standard: 2048)
         * @return this Builder
         */
        public Builder contextGroesse(int contextGroesse) {
            this.contextGroesse = contextGroesse;
            return this;
        }

        /**
         * @param threads Anzahl CPU-Kerne fuer die Inferenz (Standard: 4)
         * @return this Builder
         */
        public Builder threads(int threads) {
            this.threads = threads;
            return this;
        }

        /**
         * @param startTimeoutSekunden maximale Wartezeit bis der Server bereit ist (Standard: 60)
         * @return this Builder
         */
        public Builder startTimeoutSekunden(int startTimeoutSekunden) {
            this.startTimeoutSekunden = startTimeoutSekunden;
            return this;
        }

        /**
         * Steuert, ob der llama-server-Prozess beim Beenden der JVM automatisch gestoppt wird.
         * Standard: {@code false}.
         *
         * @param autoShutdown {@code true} = Shutdown-Hook wird registriert
         * @return this Builder
         */
        public Builder autoShutdown(boolean autoShutdown) {
            this.autoShutdown = autoShutdown;
            return this;
        }

        /**
         * Erstellt den {@link LlamaServerManager}.
         *
         * @return fertiger LlamaServerManager
         * @throws IllegalStateException wenn serverExe oder modelPath nicht gesetzt
         */
        public LlamaServerManager build() {
            if (serverExe == null || serverExe.isBlank()) {
                throw new IllegalStateException("serverExe muss gesetzt sein.");
            }
            if (serverBaseUrl == null || serverBaseUrl.isBlank()) {
                throw new IllegalStateException("serverBaseUrl muss gesetzt sein.");
            }
            if (serverUrl == null || serverUrl.isBlank()) {
                throw new IllegalStateException("serverUrl muss gesetzt sein.");
            }
            if (modelPath == null || modelPath.isBlank()) {
                throw new IllegalStateException("modelPath muss gesetzt sein.");
            }
            return new LlamaServerManager(this);
        }
    }
}
