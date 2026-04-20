# MustelaLog Desktop Client V1

Ein professionell strukturierter **WPF-Desktop-Client** für die Anzeige,
Filterung, Auswertung und Diagnose von Logdaten aus der **MustelaLogAPI**.

## Ziel

Der Client ist die Präsentationsschicht einer Three-Tier-Architektur:

1. **MySQL**
2. **MustelaLogAPI / PHP-Middleware**
3. **C#-Desktop-Client**

V1 fokussiert auf:

- Event-Tabelle mit Paging, Sortierung und Filtersicht
- Detailansicht
- Related-Events-Ansicht
- clientseitige Aggregationen
- Test-Log-Versand
- CSV-/JSON-Export
- lokales Diagnose-Logging
- Vorbereitung auf spätere Benutzeranmeldung

## Technologie

- **.NET 8 WPF** als Hauptlösung
- schlankes **MVVM** ohne externe MVVM-Bibliothek
- `HttpClient` für API-Zugriffe
- `System.Text.Json` für JSON
- lokale Konfiguration über `clientsettings.json`

## Projekte in der Solution

- `src/MustelaLog.Client.Core`
- `src/MustelaLog.Client.Wpf`
- `tests/MustelaLog.Client.Core.Tests`

## Build-Hinweis

Die Lösung ist für **Windows** und **WPF** ausgelegt. In dieser Container-
Umgebung konnte ich sie nicht kompilieren, weil kein .NET SDK installiert ist.
Die Dateien sind deshalb als direkt nutzbarer Projektstand vorbereitet, sollten
aber auf einem Windows-Rechner mit .NET-SDK einmal gebaut und geprüft werden.
