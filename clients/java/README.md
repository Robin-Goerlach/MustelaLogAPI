# MustelaLog Swing Client V1

Ein bewusst einfach gehaltener, aber professionell strukturierter Java-17-Swing-Client für die MustelaLogAPI.

## Ziel von V1

Der Client deckt den fachlichen Kern ab:

- Log-Tabelle mit Sortierung und Paging
- Filterbereich mit aktiven Filtern
- Detailansicht des selektierten Events
- Related Events / Korrelationssicht
- einfache clientseitige Aggregationen
- Test-Log über die API senden
- CSV- und JSON-Export der aktuell angezeigten Datensätze
- lokales Diagnose-Logging mit Dateilog und Live-Fenster
- vorbereitete Credential-Schicht für spätere Benutzeranmeldung

## Technische Entscheidungen

- **Java 17**
- **Swing** für die Desktop-Oberfläche
- **java.net.http.HttpClient** für die API-Kommunikation
- **keine externen Laufzeitbibliotheken** für JSON; stattdessen eine kleine interne JSON-Hilfe
- **Maven** als Build-Werkzeug

## Start

1. `client-settings.example.json` nach `client-settings.json` kopieren
2. Base URL und Token eintragen
3. bauen und starten:

```bash
mvn test
mvn exec:java
```

## Wichtige Konfigurationswerte

- `api.baseUrl`: z. B. `https://example.org/index.php`
- `api.routeParameterName`: standardmäßig `route`
- `api.apiVersionPath`: z. B. `/api/v1`
- `api.technicalAccessToken`: technischer Token für V1
- `diagnostics.logFilePath`: lokaler Pfad für das Client-Diagnoselog

## Hinweise zu Filterung und API-Grenzen

Die MustelaLogAPI unterstützt in der hier bekannten Fassung nur einen Teil der Filter serverseitig. Der Client kennzeichnet deshalb offen, wenn zusätzliche Filter nur lokal auf der aktuell geladenen Seite angewendet werden.

## Tests

Im Projekt sind einfache Unit-Tests für gut isolierbare Kernlogik enthalten:

- Zeitdarstellung
- Saved Views
- Aggregationen
- Export
- Log-Sanitizing

UI-spezifische Funktionen wie Tabellen-Rendering, Dialog-Interaktion oder Fensterverhalten sind in V1 vor allem manuell zu prüfen.
