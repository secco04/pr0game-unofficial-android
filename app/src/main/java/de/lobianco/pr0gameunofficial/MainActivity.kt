package de.lobianco.pr0gameunofficial

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.CookieManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var bottomButtons: View
    private lateinit var btnSettings: ImageButton
    private lateinit var btnSwipeLock: ImageButton
    private lateinit var btnMessages: View
    private lateinit var btnSpyReports: View
    private lateinit var messagesBadge: android.widget.TextView
    private lateinit var spyReportsBadge: android.widget.TextView
    private lateinit var adapter: PlanetPagerAdapter

    private var planets: List<Planet> = emptyList()
    private var isSettingsOpen = false
    private var isSwipeLocked = false
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Fullscreen Mode - Navigation Bar ausblenden (nach setContentView!)
        enableEdgeToEdge()

        setupInsets()
        setupCookies()

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        bottomButtons = findViewById(R.id.buttonBar)
        btnSettings = findViewById(R.id.btnSettings)
        btnSwipeLock = findViewById(R.id.btnSwipeLock)
        btnMessages = findViewById(R.id.btnMessages)
        btnSpyReports = findViewById(R.id.btnSpyReports)
        messagesBadge = findViewById(R.id.messagesBadge)
        spyReportsBadge = findViewById(R.id.spyReportsBadge)

        // Mache Badges rund
        messagesBadge.clipToOutline = true
        messagesBadge.outlineProvider = object : android.view.ViewOutlineProvider() {
            override fun getOutline(view: android.view.View, outline: android.graphics.Outline) {
                outline.setOval(0, 0, view.width, view.height)
            }
        }

        spyReportsBadge.clipToOutline = true
        spyReportsBadge.outlineProvider = object : android.view.ViewOutlineProvider() {
            override fun getOutline(view: android.view.View, outline: android.graphics.Outline) {
                outline.setOval(0, 0, view.width, view.height)
            }
        }

        // Messages Button
        btnMessages.setOnClickListener {
            android.util.Log.d("MainActivity", "Messages button clicked!")
            openMessages()
        }

        // Spy Reports Button
        btnSpyReports.setOnClickListener {
            android.util.Log.d("MainActivity", "Spy Reports button clicked!")
            openSpyReports()
        }

        // Settings Button - Toggle öffnen/schließen
        btnSettings.setOnClickListener {
            if (isSettingsOpen) {
                closeSettings()
            } else {
                openSettings()
            }
        }

        // Swipe Lock Button
        btnSwipeLock.setOnClickListener {
            toggleSwipeLock()
        }

        // Lade Planeten aus SharedPreferences
        loadPlanets()
    }

    private fun loadPlanets() {
        val prefs = getSharedPreferences("pr0game_data", MODE_PRIVATE)
        val savedPlanets = prefs.getString("planets_json", null)

        if (savedPlanets != null) {
            planets = PlanetParser.fromJson(savedPlanets)
            setupViewPager()
        } else {
            // Zeige InitialLoadFragment zum Planeten auslesen
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, InitialLoadFragment())
                .commit()
        }
    }

    fun setupViewPager() {
        if (planets.isEmpty()) return

        adapter = PlanetPagerAdapter(this, planets)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 2  // 2 Seiten vorgeladen für besseres Swipen

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Erstelle Custom View für zweizeilige Tabs
            val customView = layoutInflater.inflate(R.layout.custom_tab, null)
            val nameText = customView.findViewById<android.widget.TextView>(R.id.tab_name)
            val coordsText = customView.findViewById<android.widget.TextView>(R.id.tab_coords)

            nameText.text = planets[position].name
            coordsText.text = planets[position].coordinates

            tab.customView = customView
        }.attach()

        viewPager.visibility = ViewPager2.VISIBLE
        tabLayout.visibility = TabLayout.VISIBLE
        bottomButtons.visibility = View.VISIBLE

        // Setup ViewPager mit Helper
        ViewPagerHelper.setupViewPager(viewPager)
        ViewPagerHelper.setSwipeEnabled(true)

        // Update Lock Icon
        updateSwipeLockIcon()
    }

    /**
     * Togglet den Swipe Lock manuell
     */
    private fun toggleSwipeLock() {
        isSwipeLocked = !isSwipeLocked
        ViewPagerHelper.setSwipeEnabled(!isSwipeLocked)
        updateSwipeLockIcon()

        val status = if (isSwipeLocked) "gesperrt" else "entsperrt"
        android.widget.Toast.makeText(this, "Planeten-Wechsel $status", android.widget.Toast.LENGTH_SHORT).show()
        android.util.Log.d("MainActivity", "Swipe lock toggled: locked=$isSwipeLocked")

        // Benachrichtige alle Fragments über Lock-Änderung (für Galaxy Swipe Navigation)
        notifyFragmentsOfLockChange(isSwipeLocked)
    }

    /**
     * Benachrichtigt alle Fragments über eine Änderung des Lock-Status
     */
    private fun notifyFragmentsOfLockChange(isLocked: Boolean) {
        if (::adapter.isInitialized) {
            for (i in 0 until planets.size) {
                val fragment = adapter.getFragmentAtPosition(i)
                fragment?.onSwipeLockChanged(isLocked)
            }
        }
    }

    /**
     * Wird aufgerufen wenn sich die Galaxy Navigation Einstellung ändert
     */
    fun onGalaxyNavigationSettingChanged(enabled: Boolean) {
        android.util.Log.d("MainActivity", "Galaxy Navigation setting changed: $enabled")

        // Benachrichtige alle Fragments über die Änderung
        if (::adapter.isInitialized) {
            for (i in 0 until planets.size) {
                val fragment = adapter.getFragmentAtPosition(i)
                fragment?.onGalaxyNavigationSettingChanged(enabled)
            }
        }
    }

    /**
     * Aktualisiert das Lock-Icon (offen/geschlossen)
     */
    private fun updateSwipeLockIcon() {
        if (::btnSwipeLock.isInitialized) {
            val iconRes = if (isSwipeLocked) {
                android.R.drawable.ic_lock_lock // Geschlossen
            } else {
                android.R.drawable.ic_lock_idle_lock // Offen
            }
            btnSwipeLock.setImageResource(iconRes)

            // Farbe anpassen
            val color = if (isSwipeLocked) {
                0xFFFF6B6B.toInt() // Rot = gesperrt
            } else {
                0xFFFFFFFF.toInt() // Weiß = offen
            }
            btnSwipeLock.setColorFilter(color)
        }
    }

    /**
     * Aktiviert/Deaktiviert das Swipen zwischen Planeten
     * Wird von Fragments aufgerufen (z.B. Empire-Seite)
     * ABER: Nur wenn nicht manuell gesperrt
     */
    fun setViewPagerSwipeEnabled(enabled: Boolean) {
        // Wenn manuell gesperrt, ignoriere automatische Änderungen
        if (isSwipeLocked && enabled) {
            android.util.Log.d("MainActivity", "Swipe manually locked, ignoring auto-enable")
            return
        }

        ViewPagerHelper.setSwipeEnabled(enabled)
        android.util.Log.d("MainActivity", "ViewPager swipe set to: $enabled")
    }

    /**
     * Gibt zurück ob der Swipe aktuell manuell gesperrt ist
     */
    fun isSwipeLocked(): Boolean = isSwipeLocked

    fun onPlanetsLoaded(planetList: List<Planet>) {
        planets = planetList

        // Speichere in SharedPreferences
        val prefs = getSharedPreferences("pr0game_data", MODE_PRIVATE)
        prefs.edit()
            .putString("planets_json", PlanetParser.toJson(planetList))
            .apply()

        // Entferne InitialLoadFragment
        supportFragmentManager.findFragmentById(R.id.container)?.let {
            supportFragmentManager.beginTransaction()
                .remove(it)
                .commit()
        }

        setupViewPager()
    }

    /**
     * Wird automatisch aufgerufen wenn sich die Planetenliste geändert hat
     * Aktualisiert die Tabs ohne die App neu zu starten
     */
    fun onPlanetsUpdated(newPlanets: List<Planet>) {
        val oldCount = planets.size
        val newCount = newPlanets.size

        android.util.Log.d("MainActivity", "Planets updated: $oldCount -> $newCount")

        planets = newPlanets

        // Speichere in SharedPreferences
        val prefs = getSharedPreferences("pr0game_data", MODE_PRIVATE)
        prefs.edit()
            .putString("planets_json", PlanetParser.toJson(newPlanets))
            .apply()

        // Merke aktuellen Planet
        val currentPosition = if (::viewPager.isInitialized) viewPager.currentItem else 0

        // Aktualisiere ViewPager
        setupViewPager()

        // Versuche zur gleichen Position zurückzukehren (falls noch vorhanden)
        if (currentPosition < newPlanets.size) {
            viewPager.setCurrentItem(currentPosition, false)
        }

        // Zeige Toast nur wenn neue Planeten hinzugekommen sind
        if (newCount > oldCount) {
            val addedCount = newCount - oldCount
            android.widget.Toast.makeText(
                this,
                "✨ $addedCount neue${if (addedCount == 1) "r" else ""} Planet${if (addedCount == 1) "" else "en"} hinzugefügt!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openSettings() {
        if (isSettingsOpen) return

        isSettingsOpen = true
        viewPager.visibility = View.GONE

        // Ändere Farbe des Settings-Buttons wenn offen
        btnSettings.setColorFilter(0xFF64b5f6.toInt()) // Blau = aktiv

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, SettingsFragment())
            .commit()
    }

    /**
     * Öffnet die Nachrichten-Seite auf dem aktuellen Planeten
     */
    private fun openMessages() {
        android.util.Log.d("MainActivity", "openMessages called")

        try {
            if (planets.isNotEmpty() && ::viewPager.isInitialized) {
                val currentPosition = viewPager.currentItem

                // Hole Fragment
                val fragment = supportFragmentManager.findFragmentByTag("f$currentPosition") as? PlanetWebViewFragment
                    ?: (if (::adapter.isInitialized) adapter.getFragmentAtPosition(currentPosition) else null)

                if (fragment != null) {
                    android.util.Log.d("MainActivity", "Fragment found, clicking messages link via JavaScript")
                    fragment.clickMessagesLink()
                } else {
                    android.util.Log.e("MainActivity", "Fragment not found!")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error opening messages", e)
        }
    }

    /**
     * Öffnet die Spionageberichte auf dem aktuellen Planeten
     */
    private fun openSpyReports() {
        android.util.Log.d("MainActivity", "openSpyReports called")

        try {
            if (planets.isNotEmpty() && ::viewPager.isInitialized) {
                val currentPosition = viewPager.currentItem

                // Hole Fragment
                val fragment = supportFragmentManager.findFragmentByTag("f$currentPosition") as? PlanetWebViewFragment
                    ?: (if (::adapter.isInitialized) adapter.getFragmentAtPosition(currentPosition) else null)

                if (fragment != null) {
                    android.util.Log.d("MainActivity", "Fragment found, clicking spy reports link via JavaScript")
                    fragment.clickSpyReportsLink()
                } else {
                    android.util.Log.e("MainActivity", "Fragment not found!")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error opening spy reports", e)
        }
    }

    /**
     * Aktualisiert das Nachrichten-Badge
     * Wird von Fragments aufgerufen wenn neue Nachrichtenzahl erkannt wird
     */
    fun updateMessagesBadge(count: Int) {
        if (::messagesBadge.isInitialized) {
            if (count > 0) {
                messagesBadge.visibility = View.VISIBLE
                messagesBadge.text = if (count >= 50) "50+" else count.toString()
            } else {
                messagesBadge.visibility = View.GONE
            }
        }
    }

    /**
     * Aktualisiert das Spionageberichte-Badge
     * Wird von Fragments aufgerufen wenn neue Berichtszahl erkannt wird
     */
    fun updateSpyReportsBadge(count: Int) {
        if (::spyReportsBadge.isInitialized) {
            if (count > 0) {
                spyReportsBadge.visibility = View.VISIBLE
                spyReportsBadge.text = if (count >= 50) "50+" else count.toString()
            } else {
                spyReportsBadge.visibility = View.GONE
            }
        }
    }

    fun closeSettings() {
        isSettingsOpen = false
        viewPager.visibility = View.VISIBLE

        // Setze Farbe des Settings-Buttons zurück
        btnSettings.setColorFilter(0xFFFFFFFF.toInt()) // Weiß = normal

        supportFragmentManager.findFragmentById(R.id.container)?.let {
            supportFragmentManager.beginTransaction()
                .remove(it)
                .commit()
        }

        // Update Lock Icon nach Settings-Schließen
        updateSwipeLockIcon()
    }

    private fun setupInsets() {
        val rootView = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.root)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Padding unten hinzufügen für Gesture Bar
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    /**
     * Aktiviert Edge-to-Edge Modus und blendet Gesture Bar aus
     */
    private fun enableEdgeToEdge() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11+ (API 30+)
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                // Verstecke die Gesture Bar (weißer Strich)
                controller.hide(android.view.WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Android 10 und älter
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }

    private fun setupCookies() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Settings schließen wenn offen
            if (isSettingsOpen) {
                closeSettings()
                return true
            }

            // Sonst WebView zurück (OHNE Toast!)
            if (::adapter.isInitialized) {
                val currentFragment = adapter.getFragmentAtPosition(viewPager.currentItem)
                if (currentFragment?.canGoBack() == true) {
                    currentFragment.goBack()
                    return true
                }
            }

            // NUR HIER: App beenden mit Toast-Warnung (doppelt drücken innerhalb 2 Sekunden)
            // Dieser Code wird nur erreicht wenn WebView NICHT zurück kann
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                // Zweiter Druck innerhalb 2 Sekunden -> App beenden
                finish()
                return true
            } else {
                // Erster Druck -> Toast zeigen
                android.widget.Toast.makeText(this, "Nochmal drücken zum Beenden", android.widget.Toast.LENGTH_SHORT).show()
                backPressedTime = System.currentTimeMillis()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        CookieManager.getInstance().flush()
    }
}