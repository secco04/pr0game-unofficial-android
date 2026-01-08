# PR0GAME UNOFFICIAL - ANDROID APP

## 🚀 COMPLETE FEATURE UPDATE

This is a comprehensive update package for the pr0game Unofficial Android app with multi-language support, advanced button customization, and improved fullscreen mode.

---

## ✨ NEW FEATURES

### 🌍 **Multi-Language Support (10 Languages)**
- **English** (en) - Default
- **Deutsch** (de)
- **Türkçe** (tr)
- **Português** (pt)
- **Polski** (pl)
- **Русский** (ru)
- **Español** (es)
- **Français** (fr)
- **SetSails/Pirate** (pi) - Fun language variant
- **Ogerfränkisch** (of) - Regional dialect

**Language Picker in Settings:**
- Dropdown menu with all 10 languages
- Changes take effect immediately
- No app restart required

---

### 🎛️ **Advanced Button Bar Customization**

#### **12 Navigation Buttons:**
1. **Overview** - Planet overview page
2. **Empire** - Empire view
3. **Buildings** - Building construction
4. **Shipyard** - Ship construction
5. **Defense** - Defense systems
6. **Research** - Technology research
7. **Fleet** - Fleet management
8. **Galaxy** - Galaxy view
9. **Messages** - Messages with badge counter
10. **Spy Reports** - Spy reports with badge counter (custom eye icon)
11. **Lock** - Planet swipe lock toggle
12. **Settings** - App settings

#### **Button Layout Options:**
- **Single Row** - All buttons in scrollable horizontal bar
- **Two Rows** - First 6 buttons in row 1, rest in row 2
    - Fills horizontally: 1-2-3-4-5-6 | 7-8-9-10-11-12

#### **Button Alignment:**
- **Left** - Buttons start from left edge
- **Center** - Buttons centered (when they fit)
- **Right** - Buttons right-aligned (DEFAULT)
    - Auto-scrolls to right position

#### **Button Size Slider:**
- **Range:** 48dp - 112dp
- **Default:** 56dp
- **Icons scale proportionally** (57% of button size)
- Examples:
    - 48dp button → ~27dp icon
    - 56dp button → ~32dp icon
    - 80dp button → ~46dp icon
    - 112dp button → ~64dp icon

#### **Show/Hide Individual Buttons:**
- Toggle each of the 12 buttons on/off
- Customize your navigation bar
- Hide buttons you don't use

#### **Clean Design:**
- No separators between buttons
- Icons scale smoothly with button size
- Badge counters for Messages and Spy Reports

---

### 🖥️ **Improved Fullscreen Mode**

#### **Modern Implementation:**
- **Android 11+ (API 30+):** Uses new WindowInsetsController API
    - `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`
    - **No annoying "Swipe to exit" system toasts**
    - Bars appear on swipe, auto-hide

- **Android 10 and older:** Falls back to classic IMMERSIVE_STICKY mode

#### **Toggle Option:**
- Enable/disable fullscreen in Settings
- Default: **ON** (fullscreen enabled)
- **Requires app restart** to take effect
- Toast notification reminds user to restart

---

### 🎨 **UI Customization**

- **Hide Planet Dropdown** - Remove planet selector from web view
- **Hide Message Banner** - Remove message notification banner
- **Fullscreen Mode** - Toggle navigation bar visibility
- **Button Alignment** - Choose left/center/right alignment
- **Button Size** - Customize button and icon size
- **Two Row Layout** - Split buttons into two rows

---

### 👁️ **Custom Icons**

- **Spy Reports:** Custom eye icon (visibility symbol)
- **Buildings:** House/building icon
- **Shipyard:** Construction/shipyard icon
- **Defense:** Shield icon
- **Research:** Science flask icon
- **Overview:** Grid layout icon
- **Galaxy:** Planet icon

All icons from Google Material Symbols (Apache 2.0 licensed)

---

## 📋 SETTINGS OVERVIEW

### **Language Section:**
```
🌍 Language
   Dropdown: Select interface language
```

### **Button Bar Section:**
```
🎛️ Button Bar
   ✓ Show Two Rows
   
   Button Alignment:
   ○ Left  ○ Center  ● Right
   
   Button Size:
   ━━━━━━━━━━━━━ 56dp
   
   Select Visible Buttons:
   ✓ Overview
   ✓ Empire
   ✓ Buildings
   ✓ Shipyard
   ✓ Defense
   ✓ Research
   ✓ Fleet
   ✓ Galaxy
   ✓ Messages
   ✓ Spy Reports
   (Lock and Settings always visible)
```

### **Galaxy View Section:**
```
🌌 Galaxy View
   ✓ Galaxy Formatter
   ✓ Galaxy Swipe Navigation
   Loading Animation Delay: ━━━━━ 200ms
   Row Height: ━━━━━ 30px
```

### **UI Customization Section:**
```
🎨 UI Customization
   ✓ Hide Planet Dropdown
   ✓ Hide Message Banner
   ✓ Fullscreen Mode
      Hide navigation bar (requires app restart)
```

### **Data Section:**
```
🗄️ Data
   Clear Planet Cache (Re-login)
```

### **Support Section:**
```
☕ Support
   Buy me a coffee
```

---

## 📂 FILE STRUCTURE

```
app/src/main/
├── java/de/lobianco/pr0gameunofficial/
│   ├── MainActivity.kt
│   ├── SettingsFragment.kt
│   ├── PlanetWebViewFragment.kt
│   ├── ButtonConfig.kt
│   ├── Planet.kt
│   ├── Config.kt
│   ├── PlanetPagerAdapter.kt
│   ├── InitialLoadFragment.kt
│   └── PlanetParser.kt
│
└── res/
    ├── layout/
    │   ├── activity_main.xml
    │   ├── fragment_settings.xml
    │   ├── custom_tab.xml
    │   ├── fragment_initial_load.xml
    │   └── fragment_planet_webview.xml
    │
    ├── drawable/
    │   ├── ic_overview.xml
    │   ├── ic_empire.xml (SVG)
    │   ├── ic_buildings.xml
    │   ├── ic_shipyard.xml
    │   ├── ic_defense.xml
    │   ├── ic_research.xml
    │   ├── ic_fleet.xml (SVG)
    │   ├── ic_galaxy.xml
    │   ├── ic_spyreport.xml
    │   └── ic_settings_gear.xml (SVG)
    │
    └── values*/strings.xml (10 language folders)
        ├── values/strings.xml (English - Default)
        ├── values-de/strings.xml (Deutsch)
        ├── values-tr/strings.xml (Türkçe)
        ├── values-pt/strings.xml (Português)
        ├── values-pl/strings.xml (Polski)
        ├── values-ru/strings.xml (Русский)
        ├── values-es/strings.xml (Español)
        ├── values-fr/strings.xml (Français)
        ├── values-pi/strings.xml (SetSails)
        └── values-of/strings.xml (Ogerfränkisch)
```

---

## 🔧 TECHNICAL DETAILS

### **Button Scaling Algorithm:**
```kotlin
iconSizePx = buttonSize * 0.57f * density
paddingPx = (buttonSizePx - iconSizePx) / 2
```

### **Alignment Implementation:**
- **Gravity:** Applied to LinearLayout containers
- **Scroll Position:** Auto-scroll for right-aligned buttons
- **Two-Row Support:** Separate ScrollViews for each row

### **Fullscreen Modes:**
```kotlin
// Android 11+ (R/API 30)
window.insetsController.systemBarsBehavior = 
    BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

// Android 10 and older
SYSTEM_UI_FLAG_IMMERSIVE_STICKY
```

### **Badge System:**
- FrameLayout with ImageView + TextView overlay
- Badge positioned: TOP | END with margins
- Auto-hide when count = 0
- Max display: "50+"

---

## 🚀 INSTALLATION

1. **Extract the ZIP package**
2. **Navigate to your project:**
   ```
   C:\Users\Alessandro\AndroidStudioProjects\pr0gameunofficial\
   ```
3. **Copy the `app` folder from ZIP and MERGE** (not replace!)
4. **In Android Studio:**
    - Build → Clean Project
    - Build → Rebuild Project
5. **Run the app**

---

## ⚙️ CONFIGURATION

### **Switching Between Production and Test Server:**

Edit `Config.kt`:
```kotlin
object Config {
    const val BASE_DOMAIN = "pr0game.com"  // Production
    // const val BASE_DOMAIN = "test.pr0game.com"  // Test server
    const val BASE_URL = "https://$BASE_DOMAIN"
}
```

### **Default Settings:**

All settings are stored in SharedPreferences (`pr0game_settings`):
- `app_language`: "en" (English)
- `two_row_buttons`: false
- `button_alignment`: "right"
- `button_size`: 56
- `show_button_*`: true (all buttons visible)
- `fullscreen_enabled`: true
- `hide_planet_selector`: true
- `hide_message_banner`: true
- `galaxy_formatter_enabled`: true
- `galaxy_navigation_enabled`: true
- `galaxy_loading_delay`: 200
- `galaxy_row_height`: 30

---

## 🐛 TROUBLESHOOTING

### **Language not changing:**
- Ensure corresponding `values-XX/strings.xml` file exists
- Restart app if needed

### **Buttons not showing:**
- Check Settings → Button Bar → Make sure buttons are enabled
- Clear app data and restart

### **Icons too small:**
- Increase Button Size in settings (48-112dp)
- Clean & rebuild project

### **Fullscreen not working:**
- Toggle setting in UI Customization
- **Restart app** (required for fullscreen changes)

### **Build fails:**
- Clean Project
- Rebuild Project
- Check all `values-*/strings.xml` files are present

### **Alignment doesn't work:**
- Change setting → Close settings
- Should apply immediately
- Check if buttons overflow (need scrolling)

---

## 📱 REQUIREMENTS

- **Minimum Android:** 7.0 (API 24)
- **Target Android:** 15 (API 35)
- **Recommended:** Android 11+ for best fullscreen experience

---

## 📝 VERSION HISTORY

### **Version 1.3 (January 2026):**
- ✅ 10 language support with language picker
- ✅ 12 customizable navigation buttons
- ✅ Button alignment (left/center/right)
- ✅ Button size slider with proper icon scaling
- ✅ Two-row layout option
- ✅ Individual button show/hide toggles
- ✅ Improved fullscreen mode (no system toasts)
- ✅ Custom spy report icon
- ✅ New navigation icons (buildings, shipyard, defense, research)
- ✅ Removed button separators
- ✅ Badge system for Messages and Spy Reports
- ✅ Fixed navigation links (shipyard, defense, research)
- ✅ Centralized Config.kt for server switching

---

## 🙏 CREDITS

- **Original pr0game:** pr0game.com
- **Icons:** Google Material Symbols (Apache 2.0)
- **Development:** Community contributions

---

## ⚖️ LICENSE

This is an unofficial community app for pr0game.com.

**Icons:** Apache License 2.0 (Google Material Symbols)

---

## 📞 SUPPORT

For issues or feature requests, please use the feedback system in the app or contact the development team.

**Buy me a coffee:** https://www.buymeacoffee.com/derbutcher

---

**Last Updated:** January 8, 2026  
**Build Version:** 1.3  
**Package:** de.lobianco.pr0gameunofficial