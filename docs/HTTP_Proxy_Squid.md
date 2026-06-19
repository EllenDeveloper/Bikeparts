# HTTP-Proxy mit Squid - Caching für Web-Scraping

## Zweck

Squid als lokaler Caching-Proxy für den Scraper (bike-components.de, bike-discount.de).
Gecachte Seiten werden lokal gespeichert - danach wird neu geladen.

---

## Ergebnis

**Squid kann HTTPS-Seiten nicht cachen.**

bike-components.de und bike-discount.de laufen beide über HTTPS.
Squid tunnelt HTTPS-Verbindungen per `CONNECT`-Methode durch (TCP_TUNNEL),
liest den Inhalt aber nicht und kann ihn daher nicht cachen.

Squid-Log-Beweis:
```
TCP_TUNNEL/200 ... CONNECT www.bike-discount.de:443 - HIER_DIRECT/104.20.25.196 -
TCP_TUNNEL/200 ... CONNECT www.bike-components.de:443 - HIER_DIRECT/...
```

Kein einziger `TCP_HIT` oder `TCP_MISS` für die Shop-Seiten - nur Tunnel-Einträge.

### Workaround: SSL Bumping (verworfen)

SSL Bumping würde Squid erlauben, HTTPS zu entschlüsseln und zu cachen.
Dafür wäre nötig:
- Eigenes CA-Zertifikat generieren
- Squid mit `ssl-bump` konfigurieren (spezielles Squid-Build mit OpenSSL)
- CA-Zertifikat in Firefox und Java-Truststore importieren
- Deutlich komplexeres Docker-Setup

Aufwand zu hoch für den Nutzen. Entscheidung: eigener Cache im Code.

---

## Entscheidung: Eigener DB-Cache

Caffeine (Spring `@Cacheable`) wurde zunächst in Betracht gezogen, scheidet aber aus:
- Caffeine ist ein **In-Memory-Cache** - bei jedem Neustart der Anwendung ist der Cache leer
- Während der Entwicklung wird die App häufig neu gestartet
- Jeder Neustart würde erneutes Scraping aller Produkte auslösen

**Lösung: eigener DB-Cache, selbst implementiert.**
Scraping-Ergebnisse werden in der Datenbank (H2) persistiert und überleben Neustarts.

**Strategie während der Entwicklungsphase:**
- Scraping-Ergebnisse werden in der DB gespeichert
- Time To Live (Zeit bis Daten neu gescraped werden): **14 Tage**
- Begründung: Preise ändern sich im Entwicklungsalltag selten genug,
  14 Tage sind ein guter Kompromiss zwischen Aktualität und Scraping-Aufwand

---

## Architektur (ursprünglich geplant, nicht umgesetzt)

```
Jsoup (Java) → Squid-Proxy (localhost:3128) → bike-components.de
                         ↑
               Cache (max 24h lokal gespeichert)
```

**Tatsächliche Architektur:**
```
Jsoup (Java) → bike-components.de / bike-discount.de
      ↑
DB-Cache (H2, selbst implementiert, Time To Live: 14 Tage bis Daten neu gescraped werden)
```

---

## Wann Squid trotzdem sinnvoll ist

- HTTP-Seiten cachen (kein SSL)
- IP-Adresse verschleiern (Schutz vor Blocking durch den Shop)
- Wenn SSL Bumping akzeptabel ist (eigenes CA-Zertifikat, kontrollierte Umgebung)

---

## Durchgeführtes Setup (Dokumentation des Versuchs)

### `docker-compose.yml`

```yaml
name: bikepartsSquidProxy

services:
  squid:
    image: ubuntu/squid:5.2-22.04_beta
    container_name: bikepartsSquidProxy
    ports:
      - "3128:3128"
    volumes:
      - ./squid.conf:/etc/squid/squid.conf
      - squid-cache:/var/spool/squid

volumes:
  squid-cache:
```

### `squid.conf`

```conf
http_port 3128

# Cache-Verzeichnis (1 GB)
cache_dir ufs /var/spool/squid 1000 16 256
maximum_object_size 10 MB
cache_mem 256 MB

# Format: refresh_pattern <regex> <min> <percent> <max>
# -------------------------------------------------------
# bike-components.de
# -------------------------------------------------------

# Suchergebnisse: 4 Stunden cachen
refresh_pattern -i bike-components\.de/de/s/\?keywords=   60  50%  240  override-expire

# Versandkosten: 24 Stunden cachen (ändern sich selten)
refresh_pattern -i bike-components\.de/de/service/versand/   60  50%  1440  override-expire

# -------------------------------------------------------
# bike-discount.de
# -------------------------------------------------------

# Suchergebnisse: 4 Stunden cachen
refresh_pattern -i bike-discount\.de/de/search\?search=   60  50%  240  override-expire

# Versandkosten: 24 Stunden cachen
refresh_pattern -i bike-discount\.de/de/shippingcosts   60  50%  1440  override-expire

# -------------------------------------------------------
# Alles andere: Standard
# -------------------------------------------------------
refresh_pattern .   0  20%  4320

# Zugriff erlauben
http_access allow all
```

### Hinweis: `container_name` vs. Docker Desktop Anzeige

Docker Desktop zeigt den **Projektnamen** (= Verzeichnisname oder `name:`-Feld),
nicht den `container_name`. Lösung: `name:` auf Compose-Ebene setzen.

---

## Java-Integration (ProxyConfig.java)

Der Proxy ist in der Anwendung konfigurierbar. Aktuell deaktiviert
(`scraping.proxy.enabled=false`), da Squid für HTTPS keinen Nutzen bringt.

**`application.properties`:**
```properties
scraping.proxy.enabled=false
scraping.proxy.host=localhost
scraping.proxy.port=3128
scraping.proxy.type=HTTP
```

**`ProxyConfig.java`:**
```java
@ConfigurationProperties(prefix = "scraping.proxy")
@Component
@Data
public class ProxyConfig {
    private boolean enabled = false;
    private String host;
    private int port = 3128;
    private String username;
    private String password;
    private ProxyType type = ProxyType.HTTP;

    public enum ProxyType { HTTP, SOCKS5 }

    public Proxy toProxy() {
        Proxy.Type javaType = (type == ProxyType.SOCKS5)
                ? Proxy.Type.SOCKS
                : Proxy.Type.HTTP;
        return new Proxy(javaType, new InetSocketAddress(host, port));
    }

    public boolean hasAuth() {
        return username != null && !username.isBlank();
    }
}
```

**`ScrapingUtils.java` (Proxy-Nutzung):**
```java
public Connection buildConnection(String url) {
    Connection conn = Jsoup.connect(url)
            .userAgent(ScrapingConstants.Common.USER_AGENT)
            .timeout(20_000);
    if (proxyConfig.isEnabled()) {
        conn.proxy(proxyConfig.getHost(), proxyConfig.getPort());
    }
    return conn;
}
```

---

## Vergleich: Squid vs. Caffeine

| | Caffeine (Java-intern) | DB-Cache (selbst impl.) | Squid (externer Proxy) |
|---|---|---|---|
| Aufwand | 1 Klasse + 1 Dependency | Entity + Service-Logik | Docker + squid.conf |
| Überlebt Neustart | nein | ja | ja |
| HTTPS-Caching | ja | ja | nur mit SSL Bumping |
| IP-Verschleierung | nein | nein | ja |
| Windows-tauglich | ja | ja | nur via Docker |

**Entscheidung:** DB-Cache (selbst implementiert) für Caching während der Entwicklung.
Squid nur relevant wenn IP-Blocking ein Problem wird.
