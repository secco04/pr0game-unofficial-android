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

        setupLanguagePicker(view)
        setupButtonBar(view)
        setupGalaxySettings(view)
        setupUICustomization(view)
        setupAppInfo(view)
        setupDataSection(view)
        setupSupport(view)
        setupCloseButton(view)
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

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            languages.map { it.displayName }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val currentLang = prefs.getString("app_language", "en") ?: "en"
        val currentIndex = languages.indexOfFirst { it.code == currentLang }
        if (currentIndex >= 0) {
            spinner.setSelection(currentIndex)
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
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

    private fun setupButtonBar(view: View) {
        val switchTwoRows: Switch = view.findViewById(R.id.switchTwoRowButtons)
        val radioGroupAlignment: android.widget.RadioGroup = view.findViewById(R.id.radioGroupAlignment)
        val radioAlignLeft: android.widget.RadioButton = view.findViewById(R.id.radioAlignLeft)
        val radioAlignCenter: android.widget.RadioButton = view.findViewById(R.id.radioAlignCenter)
        val radioAlignRight: android.widget.RadioButton = view.findViewById(R.id.radioAlignRight)
        val seekBarButtonSize: SeekBar = view.findViewById(R.id.seekBarButtonSize)
        val tvButtonSizeValue: TextView = view.findViewById(R.id.tvButtonSizeValue)

        switchTwoRows.isChecked = prefs.getBoolean("two_row_buttons", false)
        switchTwoRows.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("two_row_buttons", isChecked).apply()
            (activity as? MainActivity)?.updateButtonBarVisibility()
        }

        // Alignment
        val alignment = prefs.getString("button_alignment", "right") ?: "right"
        when (alignment) {
            "left" -> radioAlignLeft.isChecked = true
            "center" -> radioAlignCenter.isChecked = true
            else -> radioAlignRight.isChecked = true
        }

        radioGroupAlignment.setOnCheckedChangeListener { _, checkedId ->
            val newAlignment = when (checkedId) {
                R.id.radioAlignLeft -> "left"
                R.id.radioAlignCenter -> "center"
                else -> "right"
            }
            prefs.edit().putString("button_alignment", newAlignment).apply()
            (activity as? MainActivity)?.updateButtonBarVisibility()
        }

        // Button Size
        val buttonSize = prefs.getInt("button_size", 56)
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
        val loadingDelay = prefs.getInt("galaxy_loading_delay", 200)
        val rowHeight = prefs.getInt("galaxy_row_height", 30)

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

    private fun setupUICustomization(view: View) {
        val switchHidePlanetSelector: Switch = view.findViewById(R.id.switchHidePlanetSelector)
        val switchHideMessageBanner: Switch = view.findViewById(R.id.switchHideMessageBanner)
        val switchFullscreen: Switch = view.findViewById(R.id.switchFullscreen)

        val hidePlanetSelector = prefs.getBoolean("hide_planet_selector", true)
        val hideMessageBanner = prefs.getBoolean("hide_message_banner", true)
        val fullscreenEnabled = prefs.getBoolean("fullscreen_enabled", true)

        switchHidePlanetSelector.isChecked = hidePlanetSelector
        switchHideMessageBanner.isChecked = hideMessageBanner
        switchFullscreen.isChecked = fullscreenEnabled

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
        val tvVersion: TextView = view.findViewById(R.id.tvVersion)
        try {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            tvVersion.text = "${getString(R.string.version)}: ${packageInfo.versionName}"
        } catch (e: Exception) {
            tvVersion.text = "${getString(R.string.version)}: Unknown"
        }
    }

    private fun setupDataSection(view: View) {
        val btnClearCache: Button = view.findViewById(R.id.btnClearCache)
        btnClearCache.setOnClickListener {
            (activity as? MainActivity)?.clearPlanetCache()
        }
    }

    private fun setupSupport(view: View) {
        val btnDonate: Button = view.findViewById(R.id.btnDonate)
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