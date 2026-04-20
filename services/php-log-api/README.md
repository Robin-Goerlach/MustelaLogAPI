# SASD Logging Middleware

Dieses Projekt ist eine **shared-hosting-taugliche PHP-Middleware** für eine
Three-Tier-Architektur:

1. **Client / Presentation Layer**
2. **PHP-Middleware / Middle Tier**
3. **MySQL / Data Tier**

Die Lösung ist ausdrücklich auf **IONOS Shared Webhosting** und ähnliche
Umgebungen zugeschnitten:

- kein `.htaccess`
- keine Rewrite-Regeln erforderlich
- kein Root-Zugriff
- kein Docker
- keine Daemons
- Root-`index.php` als primärer Einstiegspunkt

## Hauptidee

Die Middleware stellt eine **HTTP/JSON-API** bereit und schützt die Datenbank
vor direktem Internetzugriff. Eingehende Logevents werden validiert,
normalisiert, autorisiert und auditiert, bevor sie in MySQL gespeichert werden.
Lesezugriffe erfolgen bevorzugt über **freigegebene Views**, damit das interne
Tabellenschema später geändert werden kann, ohne dass der Client-Vertrag sofort
bricht.

## Beispiel-Endpunkte

Die Hauptlösung nutzt einen Front Controller ohne Rewrite-Abhängigkeit:

- `GET /index.php?route=/api/v1/health`
- `POST /index.php?route=/api/v1/ingest/events`
- `GET /index.php?route=/api/v1/events`
- `GET /index.php?route=/api/v1/events/{eventId}`
- `GET /index.php?route=/api/v1/sources`

## Authentisierung im Beispiel

Die Beispielimplementierung unterstützt pragmatisch:

- **Bearer-Token** für Quellen (`source`-Prinzipale)
- **Bearer-Token** für API-Clients (`client`-Prinzipale)

Die Token werden **nicht im Klartext** gespeichert, sondern als SHA-256-Hash.
Für besonders hohe Anforderungen kann das Schema später auf Public-Key-basierte
Signaturen oder vorgeschaltete mTLS-Terminierung erweitert werden, ohne die
Grundarchitektur neu zu bauen.

## Projektüberblick

- `index.php`  
  Primärer Einstiegspunkt im Projekt-Root.
- `public/index.php`  
  Optionale alternative Eintrittsstelle für spätere Hosting-Varianten.
- `bootstrap.php`  
  Initialisierung und Autoloading.
- `app/`  
  Anwendungscode in klaren Schichten.
- `database/mysql/`  
  MySQL-Schema, Views, Beispiel-Grants und Seed-Daten.
- `docs/`  
  Architektur- und Betriebsdokumentation.
- `bin/`  
  Optionale Wartungsskripte für CLI/Cron.

## Deployment-Grundidee

1. Dateien hochladen
2. `.env.example` nach `.env` kopieren und Werte anpassen
3. SQL-Skripte in der Reihenfolge `001`, `002`, `003`, `004` ausführen
4. Ein erstes Bearer-Token erzeugen und dessen SHA-256 in die DB eintragen
5. `index.php?route=/api/v1/health` testen

## Wichtige Hinweise

- Diese Middleware ist die **kontrollierte Zugriffsschicht**, nicht bloß eine
  technische Weiterleitung.
- Die Anwendung trennt bewusst zwischen:
  - externem API-Modell
  - interner Fachlogik
  - internem Datenbankschema
- Lesepfade und Schreibpfade sind getrennt organisiert.
- Die Views unter `database/mysql/002_views.sql` bilden die stabile
  Leseschnittstelle.

Weitere Details stehen in `docs/ARCHITECTURE.md`.
