# Architekturüberblick

## Zielbild

Die Middleware schützt die Logging-Datenbank vor direktem Internetzugriff und
stellt eine stabile HTTP/JSON-Schnittstelle bereit.

### Kernprinzipien

- Least Privilege
- Defense in Depth
- Kapselung des internen Schemas
- nachvollziehbare Lese- und Schreibpfade
- saubere Trennung von HTTP, Sicherheit, Fachlogik und Datenzugriff
- Erweiterbarkeit ohne harte Kopplung an Basistabellen

## Routing ohne .htaccess

Die Hauptlösung verwendet einen Front Controller im Root-Verzeichnis:

`/index.php?route=/api/v1/...`

Das ist unter Shared Hosting robuster als ein Rewrite-abhängiges Routing.
Optional kann später zusätzlich `public/index.php` eingesetzt werden.

## Datenbankstrategie

- **Lesezugriffe**: auf freigegebene Views
- **Schreibzugriffe**: über Repository-/Service-Pfade mit vorbereiteten Statements
- **Schema-Stabilität**: versionierte Views wie `api_v1_log_events`
- **Integrität**: Event-Hash, Audit-Trail, Append-only-Fachprinzip

## Sicherheit

- HTTPS vorausgesetzt
- Bearer-Token mit Hashspeicherung
- Scope-basierte Autorisierung
- IP-Allowlisting je Quelle
- Request-Validierung und JSON-Normalisierung
- Auditierung sicherheitsrelevanter Zugriffe
- generische Fehlermeldungen nach außen

## Skalierungsstrategie

MySQL-InnoDB und Foreign Keys sind in Kombination mit user-defined Partitioning
eingeschränkt. Deshalb ist der **Startmodus bewusst FK-stark und unpartitioniert**.
Wenn das Volumen stark steigt, sollte für Archiv- oder Long-Term-Daten auf
separate Archivtabellen oder ein externes Archivziel umgestellt werden.
