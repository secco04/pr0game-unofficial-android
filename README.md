# 🚀 pr0game Unofficial Android App

Eine inoffizielle Android-App für das Browser-Spiel [pr0game.com](https://pr0game.com) - optimiert für mobile Geräte.

[![License: CC BY-NC 4.0](https://img.shields.io/badge/License-CC%20BY--NC%204.0-lightgrey.svg)](https://creativecommons.org/licenses/by-nc/4.0/)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)

## 📱 Features

- ✨ **Multi-Planet Management**: Verwalte alle deine Planeten in einer App
- 🔄 **Swipe-Navigation**: Wechsle schnell zwischen deinen Planeten durch Wischen
- 🔒 **Swipe-Lock**: Sperre die Planeten-Navigation bei Bedarf
- ⚙️ **Einstellungen**: Passe die App nach deinen Wünschen an
- 🌐 **WebView Integration**: Vollständige pr0game-Funktionalität
- 💾 **Cookie-Verwaltung**: Bleibe automatisch eingeloggt (bis session cockie erlischt)
- 📊 **Tab-Navigation**: Übersichtliche Anzeige aller Planeten
- ⚡ **Swipe-to-Refresh**: Aktualisiere Seiten durch Herunterziehen

## 📸 Screenshots

WIP

## 🔧 Technische Details
WIP
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

## 🚀 Installation

### Für Entwickler

1. **Repository klonen**
```bash
git clone https://github.com/dein-username/pr0game-unofficial-android.git
cd pr0game-unofficial-android
```

2. **Projekt in Android Studio öffnen**
    - Öffne Android Studio
    - File → Open → Wähle den Projekt-Ordner

3. **Build & Run**
    - Warte bis Gradle sync abgeschlossen ist
    - Klicke auf den "Run" Button oder drücke `Shift + F10`

### Für Nutzer

1. Lade die neueste APK aus den [Releases](https://github.com/dein-username/pr0game-unofficial-android/releases)
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
| **Wischen** | Zwischen Planeten wechseln |
| **🔒 Lock-Button** | Planeten-Navigation sperren/entsperren |
| **⚙️ Settings-Button** | Einstellungen öffnen/schließen |
| **Zurück-Taste** | Schließt Einstellungen oder geht in WebView zurück |
| **Herunterziehen** | Seite aktualisieren (Swipe-to-Refresh) |

### Features im Detail

#### 🔒 Swipe-Lock
- Verhindert versehentliches Wechseln der Planeten
- Icon wird rot wenn gesperrt
- Nützlich beim Scrollen in der WebView

#### ⚙️ Einstellungen
- Öffnet/schließt mit einem Klick auf das Zahnrad
- Icon wird blau wenn Einstellungen geöffnet sind
- Planeten neu laden
- App-Informationen

## 🏗️ Architektur

```
app/
├── MainActivity.kt              # Hauptaktivität mit ViewPager
├── PlanetWebViewFragment.kt     # WebView für jeden Planeten
├── InitialLoadFragment.kt       # Erstes Login & Planeten-Erkennung
├── SettingsFragment.kt          # Einstellungen
├── PlanetPagerAdapter.kt        # Adapter für ViewPager2
├── ViewPagerHelper.kt           # Swipe-Steuerung
├── Planet.kt                    # Datenmodell für Planeten
├── PlanetParser.kt              # JSON Serialisierung
└── GalaxyFormatter.kt           # Planeten-Koordinaten Formatierung
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
- Kommentare auf Deutsch /englisch für Benutzer-relevante Funktionen
- Englische Variablennamen und technische Kommentare sind OK

## 🐛 Bug Reports

Gefunden einen Bug? [Erstelle ein Issue](https://github.com/secco04/pr0game-unofficial-android/issues/new) mit:

- Beschreibung des Problems
- Schritte zur Reproduktion
- Erwartetes vs. tatsächliches Verhalten
- Screenshots (wenn relevant)
- Android-Version und Gerätemodell

## 📝 To-Do

WIP

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

Copyright (c) 2024 [Dein Name]

Diese Arbeit ist lizenziert unter einer Creative Commons 
Attribution-NonCommercial 4.0 International License.

Um eine Kopie dieser Lizenz zu sehen, besuche:
https://creativecommons.org/licenses/by-nc/4.0/
```

**Wichtig**: Bei Verwendung oder Weiterverbreitung muss der ursprüngliche Autor genannt werden.

## ⚠️ Disclaimer

Diese App ist **NICHT offiziell** und wird nicht von den Betreibern von pr0game.com unterstützt oder endorsed. Dies ist ein Community-Projekt von Fans für Fans.
Aktuell ist sie noch nicht genehmigt!!!

- Die App nutzt die öffentlich zugängliche Website von pr0game.com
- Alle Rechte an pr0game.com liegen bei den jeweiligen Eigentümern
- Verwendung auf eigene Verantwortung
- Keine Garantie für Funktionalität oder Sicherheit

## 👤 Autor

WIP

## 🙏 Danksagungen

- pr0game.com Team 
- Alle Contributors die geholfen haben
- Die Android Community


---

**Viel Spaß beim Spielen! 🚀👾**

*Made with ❤️ by the pr0game community*