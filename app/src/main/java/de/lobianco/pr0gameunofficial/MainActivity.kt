package de.lobianco.pr0gameunofficial

import android.os.Bundle
import android.view.KeyEvent
import android.webkit.CookieManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: PlanetPagerAdapter

    private var planets: List<Planet> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupInsets()
        setupCookies()

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

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
    }

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
        if (keyCode == KeyEvent.KEYCODE_BACK && ::adapter.isInitialized) {
            val currentFragment = adapter.getFragmentAtPosition(viewPager.currentItem)
            if (currentFragment?.canGoBack() == true) {
                currentFragment.goBack()
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