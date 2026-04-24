# Mustralla LogAPI Java Client

Dieser Java-Client wurde neu auf das sichtbare MustelaLogAPI-Server-Interface ausgerichtet.

## Ziel

Der Client erfüllt insbesondere diese Server-Vertragsregeln:

- getrennte Bearer-Tokens für lesende und schreibende Zugriffe
- Routing über `index.php?route=/api/v1/...`
- korrektes Lesen des `data`-Envelopes in Serverantworten
- getrennte Behandlung von `GET /events`, `GET /events/{eventId}`, `GET /sources` und `POST /ingest/events`
- Ingest-Nutzdaten mit `occurredAt`, `observedAt`, `severityNumber`, `severityText` und `message`
- saubere Fehlerauswertung von HTTP-Status, `error.code`, `error.message` und `requestId`

## Projektaufbau

- `src/main/java` - Anwendungscode
- `src/test/java` - kleine, dependency-freie Selbsttests
- `client-settings.example.json` - Beispielkonfiguration
- `scripts/build.sh` - Kompilierung mit `javac`
- `scripts/run.sh` - Start der Swing-Anwendung
- `scripts/test.sh` - Ausführung der Selbsttests

## Voraussetzungen

- Java 17 oder neuer
- Zugriff auf den MustelaLogAPI-Server
- ein Reader-Token für `GET /api/v1/events` und `GET /api/v1/sources`
- ein Source-Token für `POST /api/v1/ingest/events`

## Konfiguration

1. `client-settings.example.json` nach `client-settings.json` kopieren
2. diese Werte anpassen:
   - `api.baseUrl`
   - `api.routeParameterName`
   - `api.apiVersionPath`
   - `api.readBearerToken`
   - `api.ingestBearerToken`
   - `diagnostics.logFilePath`
3. Anwendung starten

## Bauen und Starten

### Mit den mitgelieferten Shell-Skripten

```bash
chmod +x scripts/build.sh scripts/run.sh scripts/test.sh
./scripts/build.sh
./scripts/test.sh
./scripts/run.sh
```

### Direkt mit javac

```bash
mkdir -p out/main
find src/main/java -name "*.java" | sort | xargs javac -encoding UTF-8 -d out/main
java -cp out/main de.sasd.mustelalog.client.app.MustelaLogClientApplication client-settings.json
```

## Hinweise zur Bedienung

- **Health** prüft `GET /api/v1/health`
- **Events laden** ruft `GET /api/v1/events` mit den serverseitig unterstützten Filtern auf
- **Quellen laden** ruft `GET /api/v1/sources` auf
- **Test-Log senden** ruft `POST /api/v1/ingest/events` mit einem gültigen Source-Token auf
- **CSV/JSON exportieren** schreibt die aktuell geladene Seite lokal weg
- **Diagnosefenster** zeigt das lokale Client-Logging live an

## Wichtige Architekturentscheidung

Der Client versucht **nicht**, den Serververtrag umzudefinieren. Stattdessen ist er an den sichtbaren Vertrag des Servers angepasst:

- Read-Antworten werden aus `data` gelesen
- `events.read` und `sources.read` verwenden das Reader-Token
- `events.ingest` verwendet das Ingest-Token
- Test-Logs enthalten die vom Servervalidator verlangten Pflichtfelder

## Selbsttests

Die Selbsttests decken keine GUI-Interaktion ab. Sie prüfen aber zentrale, isolierbare Logik:

- JSON Parser / Writer
- Zeitnormalisierung für Serverabfragen
- CSV-Export
- Konfigurationsladen
