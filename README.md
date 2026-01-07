# 🚀 pr0game Unofficial Android App

Eine inoffizielle Android-App für das Browser-Spiel [pr0game.com](https://pr0game.com) - optimiert für mobile Geräte.

[![License: CC BY-NC 4.0](https://img.shields.io/badge/License-CC%20BY--NC%204.0-lightgrey.svg)](https://creativecommons.org/licenses/by-nc/4.0/)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)

## 📱 Features

### 🌍 Navigation & Verwaltung
- ✨ **Multi-Planet Management**: Verwalte alle deine Planeten in einer App mit Tab-Navigation
- 🔄 **Swipe-Navigation**: Wechsle schnell zwischen deinen Planeten durch Wischen
- 🔒 **Swipe-Lock**: Sperre die Planeten-Navigation bei Bedarf (verhindert versehentliches Wechseln)
- 🌌 **Galaxy Navigation**: Wische zwischen Systemen in der Galaxy-Ansicht (wenn Swipe gelockt)

### 🎯 Quick-Access Icons
- 🏛️ **Imperium**: Direktzugriff auf deine Imperiums-Übersicht
- 🚀 **Flotte**: Schnellzugriff auf die Flotten-Verwaltung
- 📧 **Nachrichten**: Mit Badge für ungelesene Nachrichten
- 🔍 **Spionageberichte**: Direkter Zugang zu Spionageberichten mit Badge
- ⚙️ **Einstellungen**: Umfangreiche Anpassungsmöglichkeiten

### 🎨 Anpassung & Optimierung
- 📐 **Galaxy Formatter**: Passt die Darstellung der Galaxy-Ansicht an (Zeilenhöhe, Ladeanimation)
- 🎯 **UI-Anpassungen**: Verstecke Planeten-Dropdown oder Nachrichten-Banner nach Belieben
- 🌐 **WebView Integration**: Vollständige pr0game-Funktionalität ohne Einschränkungen
- 💾 **Cookie-Verwaltung**: Bleibe automatisch eingeloggt (bis Session-Cookie erlischt)
- ⚡ **Swipe-to-Refresh**: Aktualisiere Seiten durch Herunterziehen
- 🎯 **Performance-Optimiert**: Hardware-Beschleunigung, optimierte Scroll-Performance

## 📸 Screenshots

WIP

## 🔧 Technische Details

### Voraussetzungen

- **Android Studio**: Arctic Fox oder neuer
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Kotlin**: 1.9+

### Dependencies

- AndroidX Core KTX
- AndroidX AppCompat
- Material Design Components
- ViewPager2
- SwipeRefreshLayout
- Fragment KTX
- ConstraintLayout

## 🚀 Installation

### Für Entwickler

1. **Repository klonen**
```bash
git clone https://github.com/secco04/pr0game-unofficial-android.git
cd pr0game-unofficial-android
```

2. **Projekt in Android Studio öffnen**
    - Öffne Android Studio
    - File → Open → Wähle den Projekt-Ordner

3. **Build & Run**
    - Warte bis Gradle sync abgeschlossen ist
    - Klicke auf den "Run" Button oder drücke `Shift + F10`

### Für Nutzer

1. Lade die neueste APK aus den [Releases](https://github.com/secco04/pr0game-unofficial-android/releases)
2. Installiere die APK auf deinem Android-Gerät
3. Öffne die App und logge dich mit deinen pr0game-Zugangsdaten ein

⚠️ **Hinweis**: Du musst möglicherweise die Installation aus unbekannten Quellen in den Android-Einstellungen aktivieren.

## 📖 Verwendung

### Erste Schritte

1. **Login**: Beim ersten Start wirst du zur pr0game-Login-Seite weitergeleitet
2. **Planeten laden**: Nach dem Login werden deine Planeten automatisch erkannt
3. **Navigation**: Wische nach links/rechts um zwischen Planeten zu wechseln

### Steuerung

| Aktion | Beschreibung |
|--------|--------------|
| **Wischen** | Zwischen Planeten wechseln (wenn nicht gelockt) |
| **🔒 Lock-Button** | Planeten-Navigation sperren/entsperren |
| **🏛️ Empire-Icon** | Direkt zum Imperium |
| **🚀 Fleet-Icon** | Direkt zur Flotten-Übersicht |
| **📧 Messages-Icon** | Direkt zu den Nachrichten (mit Badge) |
| **🔍 Spy-Icon** | Direkt zu Spionageberichten (mit Badge) |
| **⚙️ Settings-Button** | Einstellungen öffnen/schließen |
| **Zurück-Taste** | Schließt Einstellungen oder geht in WebView zurück |
| **Herunterziehen** | Seite aktualisieren (Swipe-to-Refresh) |

### Features im Detail

#### 🔒 Swipe-Lock
- Verhindert versehentliches Wechseln der Planeten
- Icon wird rot wenn gesperrt
- Nützlich beim Scrollen in der WebView
- Aktiviert Galaxy-Navigation in der Galaxy-Ansicht

#### 🌌 Galaxy Navigation
- Automatisch aktiv in der Galaxy-Ansicht wenn Swipe gelockt ist
- Wische nach links/rechts um zwischen Systemen zu navigieren (1-499)
- Anpassbare Verzögerung und Zeilenhöhe in den Einstellungen
- Ein/Ausschaltbar in den Einstellungen

#### 📧 Nachrichten & Spionage Badges
- Zeigen Anzahl ungelesener Nachrichten/Berichte
- Aktualisieren sich automatisch bei jedem Seitenwechsel
- Badge verschwindet wenn keine neuen Nachrichten vorhanden
- Direkter Klick öffnet die entsprechende Seite

#### ⚙️ Einstellungen

**Galaxy-Ansicht:**
- Galaxy Formatter ein/ausschalten
- Galaxy Swipe-Navigation aktivieren/deaktivieren
- Ladeanimation-Verzögerung anpassen (0-2000ms)
- Zeilenhöhe anpassen (20-60px)

**Generelle UI Anpassung:**
- Planeten-Dropdown ausblenden
- Nachrichten-Banner ausblenden

**Daten:**
- Planeten-Cache löschen (erzwingt Neuanmeldung)

## 🏗️ Architektur

```
app/
├── MainActivity.kt              # Hauptaktivität mit ViewPager & Bottom-Icons
├── PlanetWebViewFragment.kt     # WebView für jeden Planeten + JavaScript-Injection
├── InitialLoadFragment.kt       # Erstes Login & Planeten-Erkennung
├── SettingsFragment.kt          # Einstellungen-Dialog
├── PlanetPagerAdapter.kt        # Adapter für ViewPager2
├── ViewPagerHelper.kt           # Swipe-Steuerung
├── Planet.kt                    # Datenmodell für Planeten
├── PlanetParser.kt              # JSON Serialisierung
└── res/
    ├── layout/
    │   ├── activity_main.xml    # Main Layout mit Button-Bar
    │   ├── custom_tab.xml       # Custom Tab mit Name + Koordinaten
    │   └── fragment_settings.xml # Settings UI
    └── drawable/
        ├── ic_empire.xml        # Custom Skyline-Icon
        ├── ic_fleet.xml         # Custom Raumschiff-Icon
        └── ic_settings_gear.xml # Custom Settings-Icon
```

## 🤝 Beitragen

Contributions sind willkommen! Bitte beachte:

1. Fork das Repository
2. Erstelle einen Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit deine Änderungen (`git commit -m 'Add some AmazingFeature'`)
4. Push zum Branch (`git push origin feature/AmazingFeature`)
5. Öffne einen Pull Request

### Code Style

- Folge den [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Kommentare auf Deutsch/Englisch für Benutzer-relevante Funktionen
- Englische Variablennamen und technische Kommentare sind OK

## 🐛 Bug Reports

Gefunden einen Bug? [Erstelle ein Issue](https://github.com/secco04/pr0game-unofficial-android/issues/new) mit:

- Beschreibung des Problems
- Schritte zur Reproduktion
- Erwartetes vs. tatsächliches Verhalten
- Screenshots (wenn relevant)
- Android-Version und Gerätemodell

## 📝 Changelog

### Version 1.0 (Current)
- ✅ Multi-Planet Tab-Navigation mit Koordinaten
- ✅ Quick-Access Icons (Empire, Fleet, Messages, Spy)
- ✅ Badges für ungelesene Nachrichten/Berichte
- ✅ Galaxy Formatter & Navigation
- ✅ Swipe-Lock Funktion
- ✅ UI Anpassungsoptionen
- ✅ Custom Icons (Skyline, Raumschiff)
- ✅ Performance-Optimierungen

## ⚖️ Lizenz

Dieses Projekt steht unter der **Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0)**.

### Das bedeutet:

✅ **Erlaubt:**
- Das Projekt verwenden und ausführen
- Den Code einsehen und lernen
- Änderungen vornehmen (für private Nutzung)
- Das Projekt teilen (mit Namensnennung)

❌ **Nicht erlaubt:**
- Kommerzielle Nutzung
- Verkauf der App oder modifizierter Versionen
- Werbung ohne Zustimmung

### Vollständiger Lizenztext

```
Creative Commons Attribution-NonCommercial 4.0 International

Copyright (c) 2024-2026

Diese Arbeit ist lizenziert unter einer Creative Commons 
Attribution-NonCommercial 4.0 International License.

Um eine Kopie dieser Lizenz zu sehen, besuche:
https://creativecommons.org/licenses/by-nc/4.0/
```

**Wichtig**: Bei Verwendung oder Weiterverbreitung muss der ursprüngliche Autor genannt werden.

## ⚠️ Disclaimer

Diese App ist **NICHT offiziell** und wird nicht von den Betreibern von pr0game.com unterstützt oder endorsed. Dies ist ein Community-Projekt von Fans für Fans.

**⚠️ WICHTIG: Aktuell ist sie noch nicht von pr0game.com genehmigt! Bitte noch nicht im produktiven Einsatz verwenden!**

- Die App nutzt die öffentlich zugängliche Website von pr0game.com
- Alle Rechte an pr0game.com liegen bei den jeweiligen Eigentümern
- Verwendung auf eigene Verantwortung
- Keine Garantie für Funktionalität oder Sicherheit

## 🙏 Danksagungen

- pr0game.com Team für das großartige Spiel
- Alle Contributors die geholfen haben
- Die Android & Kotlin Community
- Claude (Anthropic) für Unterstützung bei der Entwicklung

---

**Made with ❤️ for the pr0game Community**

[Buy me a Coffee ☕](https://www.buymeacoffee.com/derbutcher)

---

**Viel Spaß beim Spielen! 🚀👾**
