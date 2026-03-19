package com.bikeparts.config;

import com.bikeparts.llama.server.LlamaServerManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.io.IOException;

/**
 * Spring-Konfiguration fuer den Start des externen llama-servers beim Hochfahren der Anwendung.
 *
 * <p>Sobald der Spring-Kontext vollstaendig gestartet ist ({@link ApplicationReadyEvent}),
 * wird - sofern {@code llama.server.lifecycle.auto-start=true} konfiguriert ist - geprueft,
 * ob der llama-server bereits laeuft. Falls nicht, wird er automatisch als externer Prozess
 * gestartet und auf Betriebsbereitschaft gewartet.</p>
 *
 * <p><b>Fehlerbehandlung (Degraded Mode):</b> Kann der Server nicht gestartet werden,
 * bricht die Anwendung nicht ab. Der Fehler wird geloggt und die Anwendung laeuft
 * ohne KI-Funktionen weiter.</p>
 *
 * <p><b>Voraussetzung:</b> llama.cpp muss unter dem in {@code application.properties}
 * konfigurierten Pfad ({@code llama.server.exe}) vorhanden sein.
 * Manueller Start z.B. mit:
 * <pre>
 *   llama-server.exe -m "DIRECTORY_KI_MODELS\qwen2.5-1.5b-instruct-q4_k_m.gguf"
 *                    --port 8085 -c 2048 --threads 4
 * </pre>
 * Zuvor muss fuer Windows llama.cpp heruntergeladen werden.</p>
 *
 * @see LlamaServerManager
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class LlamaServerConfig {

    private final LlamaServerManager llamaServerManager;

    /**
     * Steuert, ob der llama-server beim Hochfahren der Anwendung automatisch gestartet wird.
     * Konfigurierbar via {@code llama.server.lifecycle.auto-start} in {@code application.properties}.
     */
    @Value("${llama.server.lifecycle.auto-start:false}")
    private boolean autoStart;

    /**
     * Einstiegspunkt nach vollstaendigem Hochfahren der Anwendung.
     *
     * <p>Wird durch das {@link ApplicationReadyEvent} ausgeloest, d.h. nachdem
     * der gesamte Spring-Kontext inklusive aller Beans initialisiert wurde.
     * Der llama-server wird nur gestartet, wenn {@code llama.server.lifecycle.auto-start=true}.</p>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (autoStart) {
            initLlamaServerManager();
        } else {
            log.debug("llama-server Auto-Start deaktiviert (llama.server.lifecycle.auto-start=false).");
        }
    }

    /**
     * Startet den llama-server, falls er nicht bereits laeuft.
     *
     * <p>Fehler werden abgefangen und geloggt (Degraded Mode):
     * <ul>
     *   <li>{@link IOException}: Server-Executable nicht gefunden oder Prozess-Start fehlgeschlagen.</li>
     *   <li>{@link InterruptedException}: Warten auf Server-Bereitschaft wurde unterbrochen.
     *       Das Interrupt-Flag wird via {@link Thread#interrupt()} wiederhergestellt.</li>
     * </ul>
     * </p>
     */
    public void initLlamaServerManager() {
        try {
            llamaServerManager.startIfNotRunning();
        } catch (IOException e) {
            log.error("llama-server konnte nicht gestartet werden - KI-Funktionen deaktiviert.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Start des llama-servers unterbrochen - KI-Funktionen deaktiviert.", e);
        }
    }
}
