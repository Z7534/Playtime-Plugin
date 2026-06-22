# ⏱️ Playtime

Ein leichtgewichtiges und performantes Spielzeit-Tracking Plugin für Paper/Spigot Minecraft Server.

![Minecraft Version](https://img.shields.io/badge/Minecraft-26.2-brightgreen)
![Java Version](https://img.shields.io/badge/Java-26.0.1-orange)

---

## 📋 Übersicht

**Playtime** ist ein effizientes Plugin zur Erfassung und Anzeige der Spielzeit deiner Spieler. Es speichert die Daten sicher in einer MariaDB-Datenbank und bietet eine vollständig anpassbare Konfiguration mit Unterstützung für Hex-Farben und klassische Farbcodes.

### Hauptmerkmale

- **Echtzeit-Tracking**: Die aktuelle Session wird live zur Gesamtspielzeit hinzugerechnet
- **Asynchrone Datenbankoperationen**: Keine Serverlag durch Datenbankabfragen
- **HikariCP Connection Pool**: Optimierte und zuverlässige Datenbankverbindungen
- **Vollständig konfigurierbar**: Alle Nachrichten können angepasst werden
- **Hex-Farben Support**: Moderne `&#RRGGBB` Farbcodes werden unterstützt
- **Offline-Spieler Abfrage**: Spielzeit auch von Offline-Spielern abrufbar
- **Tab-Completion**: Intelligente Autovervollständigung für Spielernamen

---

## 📥 Installation

1. Lade die neueste Version der Plugin-JAR herunter
2. Platziere die JAR-Datei im `plugins/` Ordner deines Servers
3. Starte den Server einmal, um die Konfigurationsdatei zu generieren
4. Stoppe den Server und konfiguriere die Datenbankverbindung in `plugins/Playtime/config.yml`
5. Starte den Server erneut

### Voraussetzungen

- **Paper** 26.2
- **Java 26**
- **MariaDB** Datenbank

---

## ⚙️ Konfiguration

Die Konfigurationsdatei befindet sich unter `plugins/Playtime/config.yml`.

### Datenbank-Einstellungen

```yaml
database:
  host: "localhost"
  port: 3306
  database: "minecraft"
  username: "root"
  password: "password"

  # Connection Pool Einstellungen (optional)
  pool:
    maximum-pool-size: 10
    minimum-idle: 2
    connection-timeout: 30000
    idle-timeout: 600000
    max-lifetime: 1800000
```

### Nachrichten anpassen

Alle Nachrichten können individuell angepasst werden. Es stehen folgende Platzhalter zur Verfügung:

| Platzhalter | Beschreibung |
|-------------|--------------|
| `%player%` | Name des Spielers |
| `%days%` | Tage gespielt |
| `%hours%` | Stunden (0-23) |
| `%minutes%` | Minuten (0-59) |
| `%seconds%` | Sekunden (0-59) |
| `%total_hours%` | Gesamte Stunden (inkl. Tage) |
| `%total_minutes%` | Gesamte Minuten |

#### Farbcodes

Das Plugin unterstützt sowohl klassische Minecraft-Farbcodes als auch moderne Hex-Farben:

- **Klassisch**: `&0` - `&9`, `&a` - `&f`, `&k`, `&l`, `&m`, `&n`, `&o`, `&r`
- **Hex-Farben**: `&#RRGGBB` (z.B. `&#FF5555` für Rot)

#### Beispiel-Nachrichten

```yaml
messages:
  prefix: "&8[&6Playtime&8] "
  own-playtime: "&7Deine Spielzeit: &a%days% &7Tage, &a%hours% &7Stunden, &a%minutes% &7Minuten"
  other-playtime: "&7Spielzeit von &e%player%&7: &a%days% &7Tage, &a%hours% &7Stunden, &a%minutes% &7Minuten"
  player-not-found: "&cSpieler &e%player% &cwurde nicht gefunden!"
  no-permission: "&cDu hast keine Berechtigung für diesen Befehl!"
  no-permission-other: "&cDu hast keine Berechtigung, die Spielzeit anderer Spieler anzuzeigen!"
  player-only: "&cDieser Befehl kann nur von Spielern ausgeführt werden!"
  database-error: "&cEin Datenbankfehler ist aufgetreten!"
  no-playtime: "&7%player% hat noch keine Spielzeit!"
```

---

## 🎮 Befehle

| Befehl | Beschreibung | Berechtigung |
|--------|--------------|--------------|
| `/playtime` | Zeigt deine eigene Spielzeit an | `playtime.use` |
| `/playtime <Spieler>` | Zeigt die Spielzeit eines anderen Spielers an | `playtime.other` |

---

## 🔐 Berechtigungen

| Berechtigung | Beschreibung | Standard |
|--------------|--------------|----------|
| `playtime.use` | Erlaubt das Abfragen der eigenen Spielzeit | `true` (alle Spieler) |
| `playtime.other` | Erlaubt das Abfragen der Spielzeit anderer Spieler | `op` (nur Operatoren) |

---

## 🗄️ Datenbank-Schema

Das Plugin erstellt automatisch die benötigte Tabelle:

```sql
CREATE TABLE IF NOT EXISTS playtime (
    uuid VARCHAR(36) PRIMARY KEY,
    player_name VARCHAR(16) NOT NULL,
    playtime_ms BIGINT NOT NULL DEFAULT 0,
    first_join TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)
```

| Spalte | Typ | Beschreibung |
|--------|-----|--------------|
| `uuid` | VARCHAR(36) | Eindeutige Spieler-UUID (Primärschlüssel) |
| `player_name` | VARCHAR(16) | Aktueller Spielername |
| `playtime_ms` | BIGINT | Gesamte Spielzeit in Millisekunden |
| `first_join` | TIMESTAMP | Zeitpunkt des ersten Beitritts |
| `last_seen` | TIMESTAMP | Zeitpunkt des letzten Logins |

---

## 🔧 Technische Details

### Abhängigkeiten

- **Paper API** 1.21+
- **MariaDB JDBC Connector** 3.4.1
- **HikariCP** 5.1.0 (Connection Pooling)

### Build

Das Plugin verwendet Gradle als Build-System:

```bash
# Plugin bauen
./gradlew build

# Plugin bauen und Testserver starten
./gradlew runServer
```

Die fertige JAR-Datei befindet sich nach dem Build unter `build/libs/`.

### Architektur

```
de.Z7534.playtime/
├── Playtime.java           # Hauptklasse des Plugins
├── commands/
│   └── PlaytimeCommand.java    # Befehlsverarbeitung
├── database/
│   └── DatabaseManager.java    # Datenbankoperationen
├── listeners/
│   └── PlayerListener.java     # Event-Handler (Join/Quit)
└── utils/
    ├── MessageUtil.java        # Farbcode-Verarbeitung
    └── TimeUtil.java           # Zeitformatierung
```

---

## 📊 Performance

- **Asynchrone Datenbankabfragen**: Alle Datenbankoperationen laufen in separaten Threads
- **Connection Pooling**: HikariCP sorgt für effiziente Verbindungsverwaltung
- **Minimaler Speicherverbrauch**: Nur aktive Sessions werden im RAM gehalten
- **Automatische Speicherung**: Spielzeit wird beim Verlassen des Servers gespeichert
- **Graceful Shutdown**: Bei Server-Stopp werden alle offenen Sessions korrekt gespeichert

---

## 🐛 Fehlerbehebung

### "Konnte keine Verbindung zur Datenbank herstellen"

1. Überprüfe die Datenbankzugangsdaten in der `config.yml`
2. Stelle sicher, dass der MariaDB-Server läuft
3. Prüfe, ob die Datenbank existiert und der Benutzer Zugriffsrechte hat
4. Kontrolliere Firewall-Einstellungen (Port 3306)

### "Ein Datenbankfehler ist aufgetreten"

Überprüfe die Server-Konsole auf detaillierte Fehlermeldungen. Häufige Ursachen:
- Verbindungstimeout (Netzwerkprobleme)
- Zu viele gleichzeitige Verbindungen (Pool-Größe erhöhen)

---

## 📜 Lizenz

Dieses Plugin wurde von **Z7534** entwickelt.

---

## 📝 Changelog

### Version 1.0
- Initiale Veröffentlichung
- Grundlegende Spielzeit-Tracking Funktionalität
- MariaDB-Unterstützung mit HikariCP
- Anpassbare Nachrichten mit Hex-Farben Support
- Tab-Completion für Spielernamen
