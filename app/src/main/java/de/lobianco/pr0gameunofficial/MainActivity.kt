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
    private lateinit var adapter: PlanetPagerAdapter

    private var planets: List<Planet> = emptyList()
    private var isSettingsOpen = false
    private var isSwipeLocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupInsets()
        setupCookies()

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        bottomButtons = findViewById(R.id.bottomBar)
        btnSettings = findViewById(R.id.btnSettings)
        btnSwipeLock = findViewById(R.id.btnSwipeLock)

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
        viewPager.offscreenPageLimit = 2

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = planets[position].name
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
            v.setPadding(bars.left, bars.top, bars.right, 0)
            insets
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

            // Sonst WebView zurück
            if (::adapter.isInitialized) {
                val currentFragment = adapter.getFragmentAtPosition(viewPager.currentItem)
                if (currentFragment?.canGoBack() == true) {
                    currentFragment.goBack()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        CookieManager.getInstance().flush()
    }
}