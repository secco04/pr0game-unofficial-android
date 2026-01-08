package de.lobianco.pr0gameunofficial

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var buttonBarContainer: FrameLayout
    private lateinit var buttonBarScrollSingle: View
    private lateinit var buttonBarSingle: LinearLayout
    private lateinit var buttonBarDouble: View
    private lateinit var buttonBarRow1: LinearLayout
    private lateinit var buttonBarRow2: LinearLayout

    private lateinit var adapter: PlanetPagerAdapter
    private lateinit var prefs: android.content.SharedPreferences

    private var planets: MutableList<Planet> = mutableListOf()
    var isSwipeLocked = false
    private var isSettingsOpen = false

    // Badge TextViews
    private var messagesBadge: TextView? = null
    private var spyReportsBadge: TextView? = null

    // Button references for updates
    private var btnSwipeLock: ImageButton? = null
    private var btnSettings: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Fullscreen Mode (optional, user configurable)
        prefs = getSharedPreferences("pr0game_settings", Context.MODE_PRIVATE)
        val fullscreenEnabled = prefs.getBoolean("fullscreen_enabled", true)

        if (fullscreenEnabled) {
            // Modern approach using WindowInsetsController (less intrusive)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
                window.insetsController?.let {
                    it.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                    it.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // Fallback for older Android versions
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                        android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                                or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
            }
        }

        prefs = getSharedPreferences("pr0game_settings", Context.MODE_PRIVATE)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        buttonBarContainer = findViewById(R.id.buttonBarContainer)
        buttonBarScrollSingle = findViewById(R.id.buttonBarScrollSingle)
        buttonBarSingle = findViewById(R.id.buttonBarSingle)
        buttonBarDouble = findViewById(R.id.buttonBarDouble)
        buttonBarRow1 = findViewById(R.id.buttonBarRow1)
        buttonBarRow2 = findViewById(R.id.buttonBarRow2)

        loadPlanetsAndSetup()
    }

    private fun loadPlanetsAndSetup() {
        val savedPlanets = prefs.getString("planets_json", null)

        if (savedPlanets != null) {
            planets = PlanetParser.fromJson(savedPlanets).toMutableList()
            setupPlanets()
        } else {
            showInitialLoadFragment()
        }
    }

    private fun showInitialLoadFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, InitialLoadFragment())
            .commit()
    }

    fun onPlanetsLoaded(loadedPlanets: List<Planet>) {
        planets.clear()
        planets.addAll(loadedPlanets)

        val json = PlanetParser.toJson(planets)
        prefs.edit().putString("planets_json", json).apply()

        setupPlanets()
    }

    private fun setupPlanets() {
        supportFragmentManager.findFragmentById(R.id.container)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }

        adapter = PlanetPagerAdapter(this, planets)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 2

        setupTabs()
        setupButtons()

        viewPager.visibility = View.VISIBLE
        tabLayout.visibility = View.VISIBLE
        buttonBarContainer.visibility = View.VISIBLE

        updateSwipeLockIcon()
    }

    private fun setupTabs() {
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val customView = layoutInflater.inflate(R.layout.custom_tab, null)
            val tvName = customView.findViewById<TextView>(R.id.tvPlanetName)
            val tvCoords = customView.findViewById<TextView>(R.id.tvPlanetCoords)

            val planet = planets[position]
            tvName.text = planet.name
            tvCoords.text = planet.getCoordinatesString()

            tab.customView = customView
        }.attach()
    }

    private fun setupButtons() {
        val twoRows = prefs.getBoolean("two_row_buttons", false)
        val alignment = prefs.getString("button_alignment", "right") ?: "right"

        // Apply gravity to containers based on alignment
        val gravity = when (alignment) {
            "left" -> android.view.Gravity.START
            "center" -> android.view.Gravity.CENTER_HORIZONTAL
            else -> android.view.Gravity.END
        }

        if (twoRows) {
            buttonBarScrollSingle.visibility = View.GONE
            buttonBarDouble.visibility = View.VISIBLE
            buttonBarRow1.gravity = gravity
            buttonBarRow2.gravity = gravity
            setupButtonsTwoRows()

            // Scroll to position based on alignment after layout
            if (alignment == "right") {
                buttonBarRow1.post {
                    val scroll1 = findViewById<android.widget.HorizontalScrollView>(R.id.buttonBarScrollRow1)
                    val scroll2 = findViewById<android.widget.HorizontalScrollView>(R.id.buttonBarScrollRow2)
                    scroll1?.postDelayed({
                        val maxScroll = buttonBarRow1.width - scroll1.width
                        if (maxScroll > 0) scroll1.scrollTo(maxScroll, 0)
                    }, 100)
                    scroll2?.postDelayed({
                        val maxScroll = buttonBarRow2.width - scroll2.width
                        if (maxScroll > 0) scroll2.scrollTo(maxScroll, 0)
                    }, 100)
                }
            }
        } else {
            buttonBarScrollSingle.visibility = View.VISIBLE
            buttonBarDouble.visibility = View.GONE
            buttonBarSingle.gravity = gravity
            setupButtonsSingleRow()

            // Scroll to position based on alignment after layout
            if (alignment == "right") {
                buttonBarSingle.postDelayed({
                    val maxScroll = buttonBarSingle.width - buttonBarScrollSingle.width
                    if (maxScroll > 0) buttonBarScrollSingle.scrollTo(maxScroll, 0)
                }, 100)
            }
        }
    }

    private fun setupButtonsSingleRow() {
        buttonBarSingle.removeAllViews()

        val buttons = getButtonConfigs()
        buttons.forEach { config ->
            if (isButtonVisible(config.id)) {
                addButton(buttonBarSingle, config)
            }
        }
    }

    private fun setupButtonsTwoRows() {
        buttonBarRow1.removeAllViews()
        buttonBarRow2.removeAllViews()

        val buttons = getButtonConfigs()
        val visibleButtons = buttons.filter { isButtonVisible(it.id) }

        // Fill horizontally: first 6 buttons in row 1, rest in row 2
        val maxFirstRow = 6

        visibleButtons.take(maxFirstRow).forEach { config ->
            addButton(buttonBarRow1, config)
        }

        if (visibleButtons.size > maxFirstRow) {
            visibleButtons.drop(maxFirstRow).forEach { config ->
                addButton(buttonBarRow2, config)
            }
        }
    }

    private fun getButtonConfigs(): List<ButtonConfig> {
        return listOf(
            ButtonConfig("overview", R.string.btn_overview, R.drawable.ic_overview, Color.WHITE, false) { openOverview() },
            ButtonConfig("empire", R.string.btn_empire, R.drawable.ic_empire, null, false) { openEmpire() },
            ButtonConfig("buildings", R.string.btn_buildings, R.drawable.ic_buildings, Color.WHITE, false) { openBuildings() },
            ButtonConfig("shipyard", R.string.btn_shipyard, R.drawable.ic_shipyard, Color.WHITE, false) { openShipyard() },
            ButtonConfig("defense", R.string.btn_defense, R.drawable.ic_defense, Color.WHITE, false) { openDefense() },
            ButtonConfig("research", R.string.btn_research, R.drawable.ic_research, Color.WHITE, false) { openResearch() },
            ButtonConfig("fleet", R.string.btn_fleet, R.drawable.ic_fleet, null, false) { openFleet() },
            ButtonConfig("galaxy", R.string.btn_galaxy, R.drawable.ic_galaxy, Color.WHITE, false) { openGalaxy() },
            ButtonConfig("messages", R.string.btn_messages, android.R.drawable.ic_dialog_email, Color.WHITE, true) { openMessages() },
            ButtonConfig("spy_reports", R.string.btn_spy_reports, R.drawable.ic_spyreport, Color.parseColor("#FFFF00"), true) { openSpyReports() },
            ButtonConfig("swipe_lock", R.string.btn_swipe_lock, android.R.drawable.ic_lock_lock, Color.WHITE, false) { toggleSwipeLock() },
            ButtonConfig("settings", R.string.btn_settings, R.drawable.ic_settings_gear, null, false) { toggleSettings() }
        )
    }

    private fun isButtonVisible(id: String): Boolean {
        return prefs.getBoolean("show_button_$id", true)
    }

    private fun addSeparator(parent: LinearLayout) {
        val separator = View(this)
        separator.layoutParams = LinearLayout.LayoutParams(
            resources.displayMetrics.density.toInt(),
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        separator.setBackgroundColor(Color.parseColor("#2a3f5f"))
        parent.addView(separator)
    }

    private fun addButton(parent: LinearLayout, config: ButtonConfig) {
        val buttonSize = prefs.getInt("button_size", 56)
        val buttonSizePx = (buttonSize * resources.displayMetrics.density).toInt()
        val iconSizePx = (buttonSize * 0.57f * resources.displayMetrics.density).toInt() // 57% of button size

        if (config.hasBadge) {
            val frameLayout = FrameLayout(this)
            frameLayout.layoutParams = LinearLayout.LayoutParams(
                buttonSizePx,
                ViewGroup.LayoutParams.MATCH_PARENT  // Match bar height (48dp)
            )

            val imageView = ImageView(this)
            imageView.layoutParams = FrameLayout.LayoutParams(
                iconSizePx,
                iconSizePx,
                android.view.Gravity.CENTER
            )
            imageView.setImageResource(config.iconRes)
            config.tintColor?.let { imageView.setColorFilter(it) }
            imageView.contentDescription = getString(config.nameResId)

            val badge = TextView(this)
            val badgeSize = (20 * resources.displayMetrics.density).toInt()
            val badgeParams = FrameLayout.LayoutParams(badgeSize, badgeSize)
            badgeParams.gravity = android.view.Gravity.TOP or android.view.Gravity.END
            badgeParams.topMargin = (4 * resources.displayMetrics.density).toInt()
            badgeParams.rightMargin = (4 * resources.displayMetrics.density).toInt()
            badge.layoutParams = badgeParams
            badge.gravity = android.view.Gravity.CENTER
            badge.textSize = 10f
            badge.setTextColor(if (config.id == "spy_reports") Color.BLACK else Color.WHITE)
            badge.setBackgroundColor(if (config.id == "spy_reports") Color.parseColor("#FFFF00") else Color.parseColor("#FF4444"))
            badge.visibility = View.GONE

            if (config.id == "messages") messagesBadge = badge
            if (config.id == "spy_reports") spyReportsBadge = badge

            frameLayout.addView(imageView)
            frameLayout.addView(badge)
            frameLayout.setOnClickListener { config.onClick() }
            frameLayout.isClickable = true
            frameLayout.isFocusable = true

            // Set ripple effect foreground
            val outValue = android.util.TypedValue()
            theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            frameLayout.foreground = ContextCompat.getDrawable(this, outValue.resourceId)

            parent.addView(frameLayout)
        } else {
            val button = ImageButton(this)
            button.layoutParams = LinearLayout.LayoutParams(
                buttonSizePx,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // Calculate padding to center and scale icon properly
            val paddingPx = ((buttonSizePx - iconSizePx) / 2)
            button.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
            button.scaleType = ImageView.ScaleType.FIT_CENTER

            button.setImageResource(config.iconRes)
            config.tintColor?.let { button.setColorFilter(it) }
            button.contentDescription = getString(config.nameResId)

            // Set ripple effect background
            val outValue = android.util.TypedValue()
            theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
            button.setBackgroundResource(outValue.resourceId)

            button.setOnClickListener { config.onClick() }

            if (config.id == "swipe_lock") btnSwipeLock = button
            if (config.id == "settings") btnSettings = button

            parent.addView(button)
        }
    }

    fun updateButtonBarVisibility() {
        setupButtons()
    }

    fun checkForNewMessages() {
        if (planets.isEmpty() || !::viewPager.isInitialized) return

        val currentPosition = viewPager.currentItem
        val fragment = supportFragmentManager.findFragmentByTag("f$currentPosition") as? PlanetWebViewFragment
            ?: (if (::adapter.isInitialized) adapter.getFragmentAtPosition(currentPosition) else null)

        fragment?.checkForNewMessages()
    }

    fun updateMessageBadge(count: Int) {
        messagesBadge?.let {
            if (count > 0) {
                it.text = if (count > 50) "50+" else count.toString()
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }
    }

    fun updateSpyReportsBadge(count: Int) {
        spyReportsBadge?.let {
            if (count > 0) {
                it.text = if (count > 50) "50+" else count.toString()
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }
    }

    private fun toggleSwipeLock() {
        isSwipeLocked = !isSwipeLocked
        updateSwipeLockIcon()

        viewPager.isUserInputEnabled = !isSwipeLocked

        val message = if (isSwipeLocked) R.string.swipe_locked else R.string.swipe_unlocked
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()

        notifyFragmentsOfSwipeLockChange(isSwipeLocked)
    }

    private fun updateSwipeLockIcon() {
        btnSwipeLock?.setColorFilter(if (isSwipeLocked) Color.RED else Color.WHITE)
    }

    private fun toggleSettings() {
        if (isSettingsOpen) {
            closeSettings()
        } else {
            openSettings()
        }
    }

    private fun openSettings() {
        isSettingsOpen = true
        viewPager.visibility = View.GONE
        btnSettings?.setColorFilter(Color.parseColor("#64b5f6"))

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, SettingsFragment())
            .commit()
    }

    fun closeSettings() {
        isSettingsOpen = false
        viewPager.visibility = View.VISIBLE
        btnSettings?.setColorFilter(Color.WHITE)

        supportFragmentManager.findFragmentById(R.id.container)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }

        updateSwipeLockIcon()
    }

    private fun notifyFragmentsOfSwipeLockChange(isLocked: Boolean) {
        for (i in 0 until planets.size) {
            val fragment = supportFragmentManager.findFragmentByTag("f$i") as? PlanetWebViewFragment
                ?: (if (::adapter.isInitialized) adapter.getFragmentAtPosition(i) else null)
            fragment?.onSwipeLockChanged(isLocked)
        }
    }

    private fun getCurrentFragment(): PlanetWebViewFragment? {
        if (planets.isEmpty() || !::viewPager.isInitialized) return null
        val currentPosition = viewPager.currentItem
        return supportFragmentManager.findFragmentByTag("f$currentPosition") as? PlanetWebViewFragment
            ?: (if (::adapter.isInitialized) adapter.getFragmentAtPosition(currentPosition) else null)
    }

    private fun openOverview() { getCurrentFragment()?.clickOverviewLink() }
    private fun openEmpire() { getCurrentFragment()?.clickEmpireLink() }
    private fun openBuildings() { getCurrentFragment()?.clickBuildingsLink() }
    private fun openShipyard() { getCurrentFragment()?.clickShipyardLink() }
    private fun openDefense() { getCurrentFragment()?.clickDefenseLink() }
    private fun openResearch() { getCurrentFragment()?.clickResearchLink() }
    private fun openFleet() { getCurrentFragment()?.clickFleetLink() }
    private fun openGalaxy() { getCurrentFragment()?.clickGalaxyLink() }
    private fun openMessages() { getCurrentFragment()?.clickMessagesLink() }
    private fun openSpyReports() { getCurrentFragment()?.clickSpyReportsLink() }

    fun clearPlanetCache() {
        prefs.edit().remove("planets_json").apply()
        planets.clear()

        android.widget.Toast.makeText(this, R.string.cache_cleared, android.widget.Toast.LENGTH_LONG).show()

        recreate()
    }

    override fun onPause() {
        super.onPause()
        CookieManager.getInstance().flush()
    }

    override fun onBackPressed() {
        // If settings are open, close them
        if (isSettingsOpen) {
            closeSettings()
            return
        }

        // Check if any WebView can go back
        if (::adapter.isInitialized && planets.isNotEmpty()) {
            val currentPosition = viewPager.currentItem
            val fragment = supportFragmentManager.findFragmentByTag("f$currentPosition") as? PlanetWebViewFragment
                ?: adapter.getFragmentAtPosition(currentPosition)

            if (fragment?.canGoBack() == true) {
                fragment.goBack()
                return
            }
        }

        // Default back behavior
        super.onBackPressed()
    }
}