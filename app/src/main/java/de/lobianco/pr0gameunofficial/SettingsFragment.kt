package de.lobianco.pr0gameunofficial

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireContext().getSharedPreferences("pr0game_settings", Context.MODE_PRIVATE)

        setupCollapsibleSections(view)
        setupLanguagePicker(view)
        setupButtonBar(view)
        setupGalaxySettings(view)
        setupEmpireSettings(view)
        setupFleetSettings(view)
        setupUICustomization(view)
        setupMessageCategories(view)
        setupAppInfo(view)
        setupDataSection(view)
        setupSupport(view)
        setupCloseButton(view)
    }

    private fun setupCollapsibleSections(view: View) {
        // Language Section
        try {
            setupCollapsibleHeader(
                view.findViewById(R.id.sectionLanguageHeader),
                view.findViewById(R.id.expandLanguage),
                view.findViewById(R.id.sectionLanguageContent)
            )
        } catch (e: Exception) {}
        
        // Button Bar Section
        try {
            setupCollapsibleHeader(
                view.findViewById(R.id.sectionButtonBarHeader),
                view.findViewById(R.id.expandButtonBar),
                view.findViewById(R.id.sectionButtonBarContent)
            )
        } catch (e: Exception) {}
        
        // Galaxy Section
        try {
            setupCollapsibleHeader(
                view.findViewById(R.id.sectionGalaxyHeader),
                view.findViewById(R.id.expandGalaxy),
                view.findViewById(R.id.sectionGalaxyContent)
            )
        } catch (e: Exception) {}
        
        // Empire Section
        try {
            setupCollapsibleHeader(
                view.findViewById(R.id.sectionEmpireHeader),
                view.findViewById(R.id.expandEmpire),
                view.findViewById(R.id.sectionEmpireContent)
            )
        } catch (e: Exception) {}
        
        // Fleet Section
        try {
            setupCollapsibleHeader(
                view.findViewById(R.id.sectionFleetHeader),
                view.findViewById(R.id.expandFleet),
                view.findViewById(R.id.sectionFleetContent)
            )
        } catch (e: Exception) {}
        
        // UI Customization Section
        try {
            setupCollapsibleHeader(
                view.findViewById(R.id.sectionUIHeader),
                view.findViewById(R.id.expandUI),
                view.findViewById(R.id.sectionUIContent)
            )
        } catch (e: Exception) {}
        
        // Message Categories Section
        try {
            setupCollapsibleHeader(
                view.findViewById(R.id.sectionMessageCategoriesHeader),
                view.findViewById(R.id.expandMessageCategories),
                view.findViewById(R.id.sectionMessageCategoriesContent)
            )
        } catch (e: Exception) {}
        
        // Data Section
        try {
            setupCollapsibleHeader(
                view.findViewById(R.id.sectionDataHeader),
                view.findViewById(R.id.expandData),
                view.findViewById(R.id.sectionDataContent)
            )
        } catch (e: Exception) {}
    }

    private fun setupCollapsibleHeader(header: View?, expandIcon: TextView?, content: View?) {
        if (header == null || expandIcon == null || content == null) return
        
        header.setOnClickListener {
            val isVisible = content.visibility == View.VISIBLE
            content.visibility = if (isVisible) View.GONE else View.VISIBLE
            expandIcon.text = if (isVisible) "▶" else "▼"
        }
    }

    private fun setupMessageCategories(view: View) {
        val categories = listOf(
            Triple(R.id.switchCategorySpyReports, "category_spy_reports", "0"),
            Triple(R.id.switchCategorySpyDefense, "category_spy_defense", "7"),
            Triple(R.id.switchCategoryPlayer, "category_player", "1"),
            Triple(R.id.switchCategoryAlliance, "category_alliance", "2"),
            Triple(R.id.switchCategoryCombat, "category_combat", "3"),
            Triple(R.id.switchCategorySystem, "category_system", "4"),
            Triple(R.id.switchCategoryTransport, "category_transport", "5"),
            Triple(R.id.switchCategoryForeignTransport, "category_foreign_transport", "6"),
            Triple(R.id.switchCategoryExpedition, "category_expedition", "15"),
            Triple(R.id.switchCategoryGame, "category_game", "50"),
            Triple(R.id.switchCategoryBuildQueue, "category_build_queue", "99"),
            Triple(R.id.switchCategoryAll, "category_all", "100"),
            Triple(R.id.switchCategoryFavorites, "category_favorites", "998"),
            Triple(R.id.switchCategorySearch, "category_search", "999")
        )
        
        categories.forEach { (switchId, prefKey, categoryId) ->
            try {
                val switch = view.findViewById<Switch>(switchId) ?: return@forEach
                // Inverted logic: ON = Show, OFF = Hide
                // We store "show_" instead of "hide_"
                switch.isChecked = prefs.getBoolean("show_$prefKey", true) // Default: show
                switch.setOnCheckedChangeListener { _, isChecked ->
                    prefs.edit().putBoolean("show_$prefKey", isChecked).apply()
                }
            } catch (e: Exception) {}
        }
    }

    private fun setupLanguagePicker(view: View) {
        val spinner: Spinner = view.findViewById(R.id.spinnerLanguage)

        val languages = listOf(
            LanguageItem("en", getString(R.string.lang_english)),
            LanguageItem("de", getString(R.string.lang_german)),
            LanguageItem("tr", getString(R.string.lang_turkish)),
            LanguageItem("pt", getString(R.string.lang_portuguese)),
            LanguageItem("pl", getString(R.string.lang_polish)),
            LanguageItem("ru", getString(R.string.lang_russian)),
            LanguageItem("es", getString(R.string.lang_spanish)),
            LanguageItem("fr", getString(R.string.lang_french)),
            LanguageItem("pi", getString(R.string.lang_pirate)),
            LanguageItem("of", getString(R.string.lang_ogre))
        )

        // Custom adapter with white text for selected item, black text for dropdown
        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            languages.map { it.displayName }
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                // Selected item shown in spinner - white text
                (view as? TextView)?.setTextColor(android.graphics.Color.WHITE)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                // Dropdown items - black text for visibility on white background
                (view as? TextView)?.setTextColor(android.graphics.Color.BLACK)
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Get system language for default
        val systemLang = resources.configuration.locales.get(0).language
        val currentLang = prefs.getString("app_language", systemLang) ?: systemLang
        val currentIndex = languages.indexOfFirst { it.code == currentLang }
        if (currentIndex >= 0) {
            spinner.setSelection(currentIndex)
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Make sure selected text is white
                (view as? TextView)?.setTextColor(android.graphics.Color.WHITE)
                
                val selectedLang = languages[position].code
                if (selectedLang != currentLang) {
                    prefs.edit().putString("app_language", selectedLang).apply()
                    setAppLocale(selectedLang)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setAppLocale(languageCode: String) {
        val localeList = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    private fun setupOrientationSpinner(spinner: Spinner) {
        val orientations = listOf(
            getString(R.string.orientation_auto),
            getString(R.string.orientation_portrait),
            getString(R.string.orientation_landscape)
        )

        // Custom adapter with white text for selected, black for dropdown
        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            orientations
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                // Selected item - white text
                (view as? TextView)?.setTextColor(android.graphics.Color.WHITE)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                // Dropdown items - black text for visibility
                (view as? TextView)?.setTextColor(android.graphics.Color.BLACK)
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Get current orientation setting
        val currentOrientation = prefs.getString("screen_orientation", "auto") ?: "auto"
        val currentIndex = when (currentOrientation) {
            "portrait" -> 1
            "landscape" -> 2
            else -> 0
        }
        spinner.setSelection(currentIndex)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(android.graphics.Color.WHITE)
                
                val orientation = when (position) {
                    1 -> "portrait"
                    2 -> "landscape"
                    else -> "auto"
                }
                
                prefs.edit().putString("screen_orientation", orientation).apply()
                applyOrientation(orientation)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun applyOrientation(orientation: String) {
        val activity = requireActivity()
        activity.requestedOrientation = when (orientation) {
            "portrait" -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            "landscape" -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    private fun setupButtonBar(view: View) {
        val switchTwoRows: Switch = view.findViewById(R.id.switchTwoRowButtons)
        val seekBarButtonSize: SeekBar = view.findViewById(R.id.seekBarButtonSize)
        val tvButtonSizeValue: TextView = view.findViewById(R.id.tvButtonSizeValue)
        val seekBarFirstRowButtons: SeekBar = view.findViewById(R.id.seekBarFirstRowButtons)
        val tvFirstRowButtonsValue: TextView = view.findViewById(R.id.tvFirstRowButtonsValue)

        switchTwoRows.isChecked = prefs.getBoolean("two_row_buttons", false)
        switchTwoRows.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("two_row_buttons", isChecked).apply()
            
            // Show/hide first row buttons setting
            seekBarFirstRowButtons.visibility = if (isChecked) View.VISIBLE else View.GONE
            view.findViewById<TextView>(R.id.tvFirstRowButtonsLabel)?.visibility = if (isChecked) View.VISIBLE else View.GONE
            tvFirstRowButtonsValue.visibility = if (isChecked) View.VISIBLE else View.GONE
            
            (activity as? MainActivity)?.updateButtonBarVisibility()
        }

        // Button Size (default 66dp)
        val buttonSize = prefs.getInt("button_size", 66)
        seekBarButtonSize.progress = (buttonSize - 48) / 2
        tvButtonSizeValue.text = "${buttonSize}dp"

        seekBarButtonSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val size = 48 + (progress * 2)
                tvButtonSizeValue.text = "${size}dp"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val size = 48 + (seekBar!!.progress * 2)
                prefs.edit().putInt("button_size", size).apply()
                (activity as? MainActivity)?.updateButtonBarVisibility()
            }
        })

        // First Row Buttons Count (only for two-row mode)
        val firstRowButtons = prefs.getInt("first_row_buttons", 6)
        seekBarFirstRowButtons.max = 10 // Max 10 buttons in first row
        seekBarFirstRowButtons.progress = firstRowButtons - 1 // 1-11 buttons (0-10 progress)
        tvFirstRowButtonsValue.text = "$firstRowButtons"
        
        // Show/hide based on current two-row setting
        val twoRowsEnabled = prefs.getBoolean("two_row_buttons", false)
        seekBarFirstRowButtons.visibility = if (twoRowsEnabled) View.VISIBLE else View.GONE
        view.findViewById<TextView>(R.id.tvFirstRowButtonsLabel)?.visibility = if (twoRowsEnabled) View.VISIBLE else View.GONE
        tvFirstRowButtonsValue.visibility = if (twoRowsEnabled) View.VISIBLE else View.GONE

        seekBarFirstRowButtons.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val count = progress + 1 // 1-11 buttons
                tvFirstRowButtonsValue.text = "$count"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val count = seekBar!!.progress + 1
                prefs.edit().putInt("first_row_buttons", count).apply()
                (activity as? MainActivity)?.updateButtonBarVisibility()
            }
        })

        setupButtonToggle(view, R.id.switchShowOverview, "overview")
        setupButtonToggle(view, R.id.switchShowEmpire, "empire")
        setupButtonToggle(view, R.id.switchShowBuildings, "buildings")
        setupButtonToggle(view, R.id.switchShowShipyard, "shipyard")
        setupButtonToggle(view, R.id.switchShowDefense, "defense")
        setupButtonToggle(view, R.id.switchShowResearch, "research")
        setupButtonToggle(view, R.id.switchShowFleet, "fleet")
        setupButtonToggle(view, R.id.switchShowGalaxy, "galaxy")
        setupButtonToggle(view, R.id.switchShowMessages, "messages")
        setupButtonToggle(view, R.id.switchShowSpyReports, "spy_reports")
    }

    private fun setupButtonToggle(view: View, switchId: Int, buttonId: String) {
        val switch: Switch = view.findViewById(switchId)
        switch.isChecked = prefs.getBoolean("show_button_$buttonId", true)
        switch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("show_button_$buttonId", isChecked).apply()
            (activity as? MainActivity)?.updateButtonBarVisibility()
        }
    }

    private fun setupGalaxySettings(view: View) {
        val switchGalaxyFormatter: Switch = view.findViewById(R.id.switchGalaxyFormatter)
        val switchGalaxyNavigation: Switch = view.findViewById(R.id.switchGalaxyNavigation)
        val seekBarDelay: SeekBar = view.findViewById(R.id.seekBarDelay)
        val tvDelayValue: TextView = view.findViewById(R.id.tvDelayValue)
        val seekBarRowHeight: SeekBar = view.findViewById(R.id.seekBarRowHeight)
        val tvRowHeightValue: TextView = view.findViewById(R.id.tvRowHeightValue)

        val galaxyFormatterEnabled = prefs.getBoolean("galaxy_formatter_enabled", true)
        val galaxyNavigationEnabled = prefs.getBoolean("galaxy_navigation_enabled", true)
        val loadingDelay = prefs.getInt("galaxy_loading_delay", 0)
        val rowHeight = prefs.getInt("galaxy_row_height", 22)

        switchGalaxyFormatter.isChecked = galaxyFormatterEnabled
        switchGalaxyNavigation.isChecked = galaxyNavigationEnabled

        seekBarDelay.max = 40
        seekBarDelay.progress = loadingDelay / 50
        tvDelayValue.text = "${loadingDelay}ms"

        seekBarRowHeight.max = 40
        seekBarRowHeight.progress = rowHeight - 20
        tvRowHeightValue.text = "${rowHeight}px"

        switchGalaxyFormatter.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("galaxy_formatter_enabled", isChecked).apply()
        }

        switchGalaxyNavigation.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("galaxy_navigation_enabled", isChecked).apply()
        }

        seekBarDelay.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val delay = progress * 50
                tvDelayValue.text = "${delay}ms"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val delay = seekBar!!.progress * 50
                prefs.edit().putInt("galaxy_loading_delay", delay).apply()
            }
        })

        seekBarRowHeight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val height = progress + 20
                tvRowHeightValue.text = "${height}px"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val height = seekBar!!.progress + 20
                prefs.edit().putInt("galaxy_row_height", height).apply()
            }
        })
    }

    private fun setupEmpireSettings(view: View) {
        val switchEmpireStickyHeaders: Switch = view.findViewById(R.id.switchEmpireStickyHeaders)
        val switchEmpireAlternatingColors: Switch = view.findViewById(R.id.switchEmpireAlternatingColors)
        val seekBarEmpireColumnWidth: SeekBar = view.findViewById(R.id.seekBarEmpireColumnWidth)
        val tvEmpireColumnWidthValue: TextView = view.findViewById(R.id.tvEmpireColumnWidthValue)

        val empireStickyHeadersEnabled = prefs.getBoolean("empire_sticky_headers_enabled", true)
        val empireAlternatingColorsEnabled = prefs.getBoolean("empire_alternating_colors_enabled", true)
        val empireColumnWidth = prefs.getInt("empire_first_column_width", 120)

        switchEmpireStickyHeaders.isChecked = empireStickyHeadersEnabled
        switchEmpireAlternatingColors.isChecked = empireAlternatingColorsEnabled

        // Empire column width (70-120px, default 120)
        seekBarEmpireColumnWidth.max = 50
        seekBarEmpireColumnWidth.progress = empireColumnWidth - 70
        tvEmpireColumnWidthValue.text = "${empireColumnWidth}px"

        switchEmpireStickyHeaders.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("empire_sticky_headers_enabled", isChecked).apply()
        }

        switchEmpireAlternatingColors.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("empire_alternating_colors_enabled", isChecked).apply()
        }

        seekBarEmpireColumnWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val width = progress + 70
                tvEmpireColumnWidthValue.text = "${width}px"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val width = seekBar!!.progress + 70
                prefs.edit().putInt("empire_first_column_width", width).apply()
            }
        })
    }

    private fun setupFleetSettings(view: View) {
        val seekBarFleetFontSize: SeekBar = view.findViewById(R.id.seekBarFleetFontSize)
        val tvFleetFontSizeValue: TextView = view.findViewById(R.id.tvFleetFontSizeValue)

        val fleetFontSize = prefs.getInt("fleet_font_size", 11)

        // Fleet font size: 8-16px (progress 0-8 maps to 8-16)
        seekBarFleetFontSize.max = 8
        seekBarFleetFontSize.progress = fleetFontSize - 8
        tvFleetFontSizeValue.text = "${fleetFontSize}px"

        seekBarFleetFontSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val size = progress + 8
                tvFleetFontSizeValue.text = "${size}px"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val size = seekBar!!.progress + 8
                prefs.edit().putInt("fleet_font_size", size).apply()
            }
        })
    }

    private fun setupUICustomization(view: View) {
        val switchHidePlanetSelector: Switch = view.findViewById(R.id.switchHidePlanetSelector)
        val switchHideMessageBanner: Switch = view.findViewById(R.id.switchHideMessageBanner)
        val switchFullscreen: Switch = view.findViewById(R.id.switchFullscreen)
        val spinnerOrientation: Spinner = view.findViewById(R.id.spinnerOrientation)

        val hidePlanetSelector = prefs.getBoolean("hide_planet_selector", true)
        val hideMessageBanner = prefs.getBoolean("hide_message_banner", true)
        val fullscreenEnabled = prefs.getBoolean("fullscreen_enabled", false)

        switchHidePlanetSelector.isChecked = hidePlanetSelector
        switchHideMessageBanner.isChecked = hideMessageBanner
        switchFullscreen.isChecked = fullscreenEnabled

        // Setup orientation spinner
        setupOrientationSpinner(spinnerOrientation)

        switchHidePlanetSelector.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("hide_planet_selector", isChecked).apply()
        }

        switchHideMessageBanner.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("hide_message_banner", isChecked).apply()
        }

        switchFullscreen.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("fullscreen_enabled", isChecked).apply()
            android.widget.Toast.makeText(
                requireContext(),
                "Please restart the app for fullscreen changes to take effect",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupAppInfo(view: View) {
        val tvVersion: TextView = view.findViewById(R.id.tvAppVersion)
        try {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            tvVersion.text = "${getString(R.string.version)}: ${packageInfo.versionName}"
        } catch (e: Exception) {
            tvVersion.text = "${getString(R.string.version)}: Unknown"
        }
    }

    private fun setupDataSection(view: View) {
        val etLoginUsername: android.widget.EditText = view.findViewById(R.id.etLoginUsername)
        val etLoginPassword: android.widget.EditText = view.findViewById(R.id.etLoginPassword)
        val btnSaveLoginCredentials: Button = view.findViewById(R.id.btnSaveLoginCredentials)
        val btnClearCache: Button = view.findViewById(R.id.btnClearCache)
        val btnResetSettings: Button = view.findViewById(R.id.btnResetSettings)
        
        // Load saved credentials
        val savedUsername = prefs.getString("login_username", "") ?: ""
        val savedPassword = prefs.getString("login_password", "") ?: ""
        etLoginUsername.setText(savedUsername)
        etLoginPassword.setText(savedPassword)
        
        btnSaveLoginCredentials.setOnClickListener {
            val username = etLoginUsername.text.toString()
            val password = etLoginPassword.text.toString()
            
            prefs.edit().apply {
                putString("login_username", username)
                putString("login_password", password)
                apply()
            }
            
            android.widget.Toast.makeText(
                requireContext(),
                R.string.credentials_saved,
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
        
        btnClearCache.setOnClickListener {
            (activity as? MainActivity)?.clearPlanetCache()
        }
        
        btnResetSettings.setOnClickListener {
            showResetConfirmationDialog()
        }
    }
    
    private fun showResetConfirmationDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Reset Settings")
            .setMessage("Reset all settings to default values?\n\n• Button size: 66dp\n• Buttons in first row: 6\n• Fullscreen: OFF\n• Loading delay: 0ms\n• Row height: 22px\n• Language: System default")
            .setPositiveButton("Reset") { _, _ ->
                resetAllSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun resetAllSettings() {
        val systemLang = resources.configuration.locales.get(0).language
        
        prefs.edit().apply {
            // Button Bar
            putInt("button_size", 66)
            putInt("first_row_buttons", 6)
            // DON'T reset two_row_buttons - keep current layout
            
            // All buttons visible by default
            putBoolean("show_button_overview", true)
            putBoolean("show_button_empire", true)
            putBoolean("show_button_buildings", true)
            putBoolean("show_button_shipyard", true)
            putBoolean("show_button_defense", true)
            putBoolean("show_button_research", true)
            putBoolean("show_button_fleet", true)
            putBoolean("show_button_galaxy", true)
            putBoolean("show_button_messages", true)
            putBoolean("show_button_spy_reports", true)
            
            // Galaxy
            putInt("galaxy_loading_delay", 0)
            putInt("galaxy_row_height", 22)
            putBoolean("galaxy_formatter_enabled", true)
            putBoolean("galaxy_navigation_enabled", true)
            
            // UI
            putBoolean("hide_planet_selector", true)
            putBoolean("hide_message_banner", true)
            putBoolean("fullscreen_enabled", false)
            putString("screen_orientation", "auto")
            
            // Language - system default
            putString("app_language", systemLang)
            
            // Message Categories - all visible
            putBoolean("show_category_spy_reports", true)
            putBoolean("show_category_spy_defense", true)
            putBoolean("show_category_player", true)
            putBoolean("show_category_alliance", true)
            putBoolean("show_category_combat", true)
            putBoolean("show_category_system", true)
            putBoolean("show_category_transport", true)
            putBoolean("show_category_foreign_transport", true)
            putBoolean("show_category_expedition", true)
            putBoolean("show_category_game", true)
            putBoolean("show_category_build_queue", true)
            putBoolean("show_category_all", true)
            putBoolean("show_category_favorites", true)
            putBoolean("show_category_search", true)
            
            apply()
        }
        
        // Reload settings fragment
        android.widget.Toast.makeText(
            requireContext(),
            "Settings reset to defaults. Restarting app...",
            android.widget.Toast.LENGTH_SHORT
        ).show()
        
        // Restart app
        val intent = requireActivity().intent
        requireActivity().finish()
        startActivity(intent)
    }

    private fun setupSupport(view: View) {
        val btnDonate: Button = view.findViewById(R.id.btnBuyCoffee)
        btnDonate.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.buymeacoffee.com/derbutcher"))
                startActivity(intent)
            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    requireContext(),
                    R.string.browser_error,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupCloseButton(view: View) {
        val btnClose: Button = view.findViewById(R.id.btnClose)
        btnClose.setOnClickListener {
            (activity as? MainActivity)?.closeSettings()
        }
    }

    data class LanguageItem(val code: String, val displayName: String)
}