# Code- und Dokumentationsrichtlinien

## DocBlocks

DocBlocks sollten dort verwendet werden, wo sie echten API-Nutzen stiften:

- Klassen
- Interfaces
- öffentliche Methoden
- komplexe Properties
- DTOs
- Repositories
- Services

Wichtige Tags:

- `@param`
- `@return`
- `@throws`

## Normale erklärende Kommentare

Normale Kommentare sind sinnvoll an Stellen, an denen Entscheidungen,
Sicherheitsüberlegungen oder Architektur-Trades erklärt werden, zum Beispiel:

- warum eine Sortierung per Whitelist erfolgt
- warum `?route=` statt Rewrite genutzt wird
- warum Audit-Fehler nicht automatisch die Hauptantwort blockieren
- warum Token nur gehasht gespeichert werden

## Nicht überkommentieren

Nicht jeder Setter oder triviale Getter braucht einen Zusatzkommentar. Kommentare
sollen beim Verstehen helfen, nicht die Datei optisch aufblasen.
