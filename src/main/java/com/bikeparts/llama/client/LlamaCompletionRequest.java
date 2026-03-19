package com.bikeparts.llama.client;

import java.util.List;

/**
 * <p>Repraesentiert den JSON-Payload fuer eine POST-Anfrage an den llama-server /completion-Endpunkt.
 *
 * <p><b>Architektur - zwei Schichten:</b>
 * <ol>
 *   <li><b>LlamaCompletionRequest + Builder (diese Klasse):</b> Sammelt alle Parameter und baut daraus
 *       einen JSON-String (via {@link #toJson()}). Kein Netzwerk, kein HTTP - reines
 *       Java-Objekt.</li>
 *   <li><b>HTTP-Versand (z.B. LlamaHttpClientMain):</b> Nimmt den fertigen JSON-String und
 *       schickt ihn per {@code java.net.http.HttpClient} als HTTP-POST an den llama-server.</li>
 * </ol>
 *
 * <p><b>Builder-Entwurfsmuster:</b> Der innere {@link Builder} loest das Problem, dass ein
 * Objekt mit vielen optionalen Parametern ohne zahlreiche Konstruktor-Ueberladungen gebaut
 * werden kann. Jede Builder-Methode gibt {@code this} zurueck, sodass Aufrufe verkettet
 * werden koennen (Fluent API).
 *
 * <p><b>Ablauf:</b>
 * <pre>
 *   Builder              -&gt; LlamaCompletionRequest.toJson()  -&gt; HttpClient.send()
 *   (Parameter sammeln)     (JSON-String bauen)        (HTTP POST abschicken)
 * </pre>
 *
 * <p>Verwendung mit ProductOffers (Standardkonfiguration):
 * <pre>
  LlamaCompletionRequest request = LlamaCompletionRequest.fromProductOffers(systemPrompt, productOffersText);
  String json = request.toJson();
</pre>
 *
 * <p>Verwendung mit Builder und eigenen Parametern:
 * <pre>
  LlamaCompletionRequest request = new LlamaCompletionRequest.Builder()
      .productOffers(systemPrompt, productOffersText)
      .temperature(0.2f)
      .nPredict(20)
      .build();
</pre>
 */
public class LlamaCompletionRequest {

    /** Vollstaendiger Prompt im Qwen-ChatML-Format (System + User + Assistant-Prefix). */
    private final String prompt;

    /** Kreativitaet der Antwort: 0.0 = absolut deterministisch, 2.0 = sehr kreativ. */
    private final float temperature;

    /** Maximale Anzahl generierter Tokens in der Antwort. */
    private final int nPredict;

    /** Wiederholungsstrafe: 1.0 = keine Strafe, > 1.0 = Wiederholungen unterdruecken. */
    private final float repeatPenalty;

    /** Seed fuer reproduzierbare Ergebnisse. -1 = zufaelliger Seed. */
    private final int seed;

    /** Stop-Sequenzen: Generierung endet beim ersten Auftreten eines dieser Tokens. */
    private final List<String> stop;

    /**
     * Deaktiviert den Thinking-Modus (Chain-of-Thought) des Modells.
     * Unterstuetzt ab llama.cpp 2025 fuer Qwen3/QwQ-Modelle.
     * Fuer qwen2.5-instruct wird dieser Parameter zur Sicherheit mitgegeben.
     */
    private final boolean enableThinking;

    /**
     * Privater Konstruktor - wird ausschliesslich von {@link Builder#build()} aufgerufen.
     *
     * @param builder befuellter Builder mit allen Konfigurationswerten
     */
    private LlamaCompletionRequest(Builder builder) {
        this.prompt = builder.prompt;
        this.temperature = builder.temperature;
        this.nPredict = builder.nPredict;
        this.repeatPenalty = builder.repeatPenalty;
        this.seed = builder.seed;
        this.stop = builder.stop;
        this.enableThinking = builder.enableThinking;
    }

    /**
     * Erstellt eine vorkonfigurierte Anfrage fuer die ProductOffers-Filterung.
     *
     * @param systemPrompt  Systembeschreibung fuer das Modell
     * @param productOffers ProductOffers-Liste im Format "id=X, productName=..., price=..., inStock=..."
     * @return LlamaCompletionRequest mit Standardkonfiguration
     */
    public static LlamaCompletionRequest fromProductOffers(String systemPrompt, String productOffers) {
        return new Builder()
                .productOffers(systemPrompt, productOffers)
                .build();
    }

    /**
     * Serialisiert den Request als JSON-String fuer den llama-server /completion-Endpunkt.
     *
     * @return JSON-Payload als String
     */
    public String toJson() {
        StringBuilder stopArray = new StringBuilder("[");
        for (int i = 0; i < stop.size(); i++) {
            stopArray.append("\"").append(escapeJson(stop.get(i))).append("\"");
            if (i < stop.size() - 1) {
                stopArray.append(",");
            }
        }
        stopArray.append("]");

        return "{"
                + "\"prompt\":\"" + escapeJson(prompt) + "\","
                + "\"temperature\":" + temperature + ","
                + "\"n_predict\":" + nPredict + ","
                + "\"repeat_penalty\":" + repeatPenalty + ","
                + "\"seed\":" + seed + ","
                + "\"stop\":" + stopArray + ","
                + "\"enable_thinking\":" + enableThinking
                + "}";
    }

    /**
     * Maskiert Sonderzeichen fuer die JSON-Serialisierung.
     *
     * @param text zu maskierender Text
     * @return maskierter Text
     */
    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Gibt den vollstaendigen Prompt im Qwen-ChatML-Format zurueck.
     *
     * @return Prompt-String (System + User + Assistant-Prefix)
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * Gibt die Temperatur fuer die Textgenerierung zurueck.
     *
     * @return Temperaturwert (0.0 = deterministisch, 2.0 = sehr kreativ)
     */
    public float getTemperature() {
        return temperature;
    }

    /**
     * Gibt die maximale Anzahl generierter Tokens zurueck.
     *
     * @return maximale Token-Anzahl
     */
    public int getNPredict() {
        return nPredict;
    }

    /**
     * Gibt die Wiederholungsstrafe zurueck.
     *
     * @return Wiederholungsstrafe (1.0 = keine Strafe, &gt; 1.0 = Wiederholungen unterdruecken)
     */
    public float getRepeatPenalty() {
        return repeatPenalty;
    }

    /**
     * Gibt den Seed fuer die Textgenerierung zurueck.
     *
     * @return Seed-Wert (-1 = zufaelliger Seed)
     */
    public int getSeed() {
        return seed;
    }

    /**
     * Gibt die Liste der Stop-Sequenzen zurueck.
     * Die Generierung endet beim ersten Auftreten einer dieser Sequenzen.
     *
     * @return unveraenderliche Liste der Stop-Tokens
     */
    public List<String> getStop() {
        return stop;
    }

    /**
     * Gibt zurueck, ob der Thinking-Modus (Chain-of-Thought) aktiviert ist.
     *
     * @return {@code true} wenn Thinking aktiv, {@code false} wenn deaktiviert
     */
    public boolean isEnableThinking() {
        return enableThinking;
    }

    /**
     * Builder fuer {@link LlamaCompletionRequest}.
     *
     * <p>Standardwerte:
     * <ul>
     *   <li>temperature: 0.0 (deterministisch)</li>
     *   <li>nPredict: 10</li>
     *   <li>repeatPenalty: 1.0 (keine Strafe)</li>
     *   <li>seed: -1 (zufaellig)</li>
     *   <li>stop: ["&lt;|im_end|&gt;", "\n"]</li>
     *   <li>enableThinking: false</li>
     * </ul>
     */
    public static class Builder {

        /** Vollstaendiger Prompt im Qwen-ChatML-Format. Wird durch {@link #productOffers} oder {@link #prompt} gesetzt. */
        private String prompt;

        /** Kreativitaet der Antwort: 0.0 = absolut deterministisch, 2.0 = sehr kreativ. */
        private float temperature = 0.0f;

        /** Maximale Anzahl generierter Tokens in der Antwort. */
        private int nPredict = 10;

        /** Wiederholungsstrafe: 1.0 = keine Strafe, &gt; 1.0 = Wiederholungen unterdruecken. */
        private float repeatPenalty = 1.0f;

        /** Seed fuer reproduzierbare Ergebnisse. -1 = zufaelliger Seed. */
        private int seed = -1;

        /** Stop-Sequenzen: Generierung endet beim ersten Auftreten eines dieser Tokens. */
        private List<String> stop = List.of("<|im_end|>", "\n");

        /** Steuert den Thinking-Modus (Chain-of-Thought). false = deaktiviert (Standard). */
        private boolean enableThinking = false;

        /**
         * Setzt System-Prompt und ProductOffers und baut daraus den vollstaendigen Qwen-ChatML-Prompt.
         * Ueberschreibt einen zuvor gesetzten Prompt.
         *
         * @param systemPrompt  Systembeschreibung fuer das Modell
         * @param productOffers ProductOffers-Liste als String
         * @return this Builder
         */
        public Builder productOffers(String systemPrompt, String productOffers) {
            this.prompt = buildPrompt(systemPrompt, productOffers);
            return this;
        }

        /**
         * Setzt einen vollstaendigen, selbst erstellten Prompt.
         * Ueberschreibt zuvor gesetzte ProductOffers.
         *
         * @param prompt vollstaendiger Prompt-String
         * @return this Builder
         */
        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        /**
         * @param temperature 0.0 = deterministisch, 2.0 = sehr kreativ
         * @return this Builder
         */
        public Builder temperature(float temperature) {
            this.temperature = temperature;
            return this;
        }

        /**
         * @param nPredict maximale Anzahl generierter Tokens
         * @return this Builder
         */
        public Builder nPredict(int nPredict) {
            this.nPredict = nPredict;
            return this;
        }

        /**
         * @param repeatPenalty Wiederholungsstrafe (1.0 = keine)
         * @return this Builder
         */
        public Builder repeatPenalty(float repeatPenalty) {
            this.repeatPenalty = repeatPenalty;
            return this;
        }

        /**
         * @param seed Seed fuer Reproduzierbarkeit (-1 = zufaellig)
         * @return this Builder
         */
        public Builder seed(int seed) {
            this.seed = seed;
            return this;
        }

        /**
         * @param stop Liste von Stop-Sequenzen
         * @return this Builder
         */
        public Builder stop(List<String> stop) {
            this.stop = stop;
            return this;
        }

        /**
         * Steuert den Thinking-Modus (Chain-of-Thought).
         * Standard: false (deaktiviert).
         *
         * @param enableThinking true = Thinking aktiv, false = deaktiviert
         * @return this Builder
         */
        public Builder enableThinking(boolean enableThinking) {
            this.enableThinking = enableThinking;
            return this;
        }

        /**
         * Erstellt den {@link LlamaCompletionRequest}.
         *
         * @return fertiger LlamaCompletionRequest
         * @throws IllegalStateException wenn weder ProductOffers noch Prompt gesetzt wurde
         */
        public LlamaCompletionRequest build() {
            if (prompt == null || prompt.isBlank()) {
                throw new IllegalStateException("Prompt oder ProductOffers muss gesetzt sein.");
            }
            return new LlamaCompletionRequest(this);
        }

        /**
         * Baut den vollstaendigen Prompt im Qwen-ChatML-Format.
         * Struktur identisch zu QwenOnePrompt: System -> User (ProductOffers) -> Assistant-Prefix "ID=".
         *
         * @param systemPrompt  Systembeschreibung fuer das Modell
         * @param productOffers ProductOffers-Liste als String
         * @return fertiger ChatML-Prompt
         */
        private String buildPrompt(String systemPrompt, String productOffers) {
            return "<|im_start|>system\n"
                    + systemPrompt + "<|im_end|>\n"
                    + "<|im_start|>user\n"
                    + productOffers
//my                    + " /no_think"
                    + "<|im_end|>\n"
                    + "<|im_start|>assistant\n"
                    + "ID=";
        }
    }
}
