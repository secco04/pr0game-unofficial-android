package de.lobianco.pr0gameunofficial

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
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

        val closeButton: Button = view.findViewById(R.id.btnClose)
        val clearDataButton: Button = view.findViewById(R.id.btnClearData)
        val versionText: TextView = view.findViewById(R.id.tvVersion)
        val btnDonate: Button = view.findViewById(R.id.btnDonate)

        // Galaxy Formatter Settings
        val switchGalaxyFormatter: Switch = view.findViewById(R.id.switchGalaxyFormatter)
        val switchGalaxyNavigation: Switch = view.findViewById(R.id.switchGalaxyNavigation)
        val seekBarDelay: SeekBar = view.findViewById(R.id.seekBarDelay)
        val tvDelayValue: TextView = view.findViewById(R.id.tvDelayValue)
        val seekBarRowHeight: SeekBar = view.findViewById(R.id.seekBarRowHeight)
        val tvRowHeightValue: TextView = view.findViewById(R.id.tvRowHeightValue)

        // UI Customization Settings
        val switchHidePlanetSelector: Switch = view.findViewById(R.id.switchHidePlanetSelector)
        val switchHideMessageBanner: Switch = view.findViewById(R.id.switchHideMessageBanner)

        // Version anzeigen
        try {
            val version = requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0).versionName
            versionText.text = "Version $version"
        } catch (e: Exception) {
            versionText.text = "Version 1.0"
        }

        // Lade gespeicherte Settings
        val galaxyFormatterEnabled = prefs.getBoolean("galaxy_formatter_enabled", true)
        val galaxyNavigationEnabled = prefs.getBoolean("galaxy_navigation_enabled", true)
        val delayMs = prefs.getInt("galaxy_formatter_delay", 200)
        val rowHeight = prefs.getInt("galaxy_row_height", 20)
        val hidePlanetSelector = prefs.getBoolean("hide_planet_selector", true)
        val hideMessageBanner = prefs.getBoolean("hide_message_banner", true)

        switchGalaxyFormatter.isChecked = galaxyFormatterEnabled
        switchGalaxyNavigation.isChecked = galaxyNavigationEnabled
        switchHidePlanetSelector.isChecked = hidePlanetSelector
        switchHideMessageBanner.isChecked = hideMessageBanner
        seekBarDelay.progress = delayMs / 50 // 0-500ms in 50ms Schritten
        tvDelayValue.text = "${delayMs}ms"
        seekBarRowHeight.progress = rowHeight - 12 // 12-32px, also 20-12=8
        tvRowHeightValue.text = "${rowHeight}px"

        android.util.Log.d("Settings", "Loaded: formatter=$galaxyFormatterEnabled, navigation=$galaxyNavigationEnabled, delay=$delayMs, rowHeight=$rowHeight")

        // Enable/Disable SeekBars basierend auf Toggle
        seekBarDelay.isEnabled = galaxyFormatterEnabled
        seekBarRowHeight.isEnabled = galaxyFormatterEnabled

        // Galaxy Formatter Toggle
        switchGalaxyFormatter.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("galaxy_formatter_enabled", isChecked).apply()
            seekBarDelay.isEnabled = isChecked
            seekBarRowHeight.isEnabled = isChecked
            android.util.Log.d("Settings", "Galaxy Formatter: $isChecked")
        }

        // Galaxy Navigation Toggle
        switchGalaxyNavigation.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("galaxy_navigation_enabled", isChecked).apply()
            android.util.Log.d("Settings", "Galaxy Navigation: $isChecked")

            // Benachrichtige MainActivity über Änderung
            (activity as? MainActivity)?.onGalaxyNavigationSettingChanged(isChecked)
        }

        // UI Customization Toggles
        switchHidePlanetSelector.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("hide_planet_selector", isChecked).apply()
            android.util.Log.d("Settings", "Hide Planet Selector: $isChecked")
        }

        switchHideMessageBanner.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("hide_message_banner", isChecked).apply()
            android.util.Log.d("Settings", "Hide Message Banner: $isChecked")
        }

        // Delay SeekBar
        seekBarDelay.max = 10 // 0-500ms in 50ms Schritten
        seekBarDelay.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val delay = progress * 50 // 0-500ms in 50ms Schritten
                tvDelayValue.text = "${delay}ms"
                android.util.Log.d("Settings", "Delay changed to: ${delay}ms")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val delay = (seekBar?.progress ?: 4) * 50
                prefs.edit().putInt("galaxy_formatter_delay", delay).apply()
                android.util.Log.d("Settings", "Delay saved: ${delay}ms")
            }
        })

        // Row Height SeekBar
        seekBarRowHeight.max = 20 // 12-32px
        seekBarRowHeight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val height = progress + 12 // 12-32px
                tvRowHeightValue.text = "${height}px"
                if (fromUser) {
                    android.util.Log.d("Settings", "Row height slider moved to: ${height}px")
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                android.util.Log.d("Settings", "Started touching row height slider")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val height = (seekBar?.progress ?: 8) + 12
                prefs.edit().putInt("galaxy_row_height", height).apply()
                android.util.Log.d("Settings", "Row height saved: ${height}px")
            }
        })

        // Schließen Button
        closeButton.setOnClickListener {
            (activity as? MainActivity)?.closeSettings()
        }

        // Daten löschen Button
        clearDataButton.setOnClickListener {
            val dataPrefs = requireContext().getSharedPreferences("pr0game_data", Context.MODE_PRIVATE)
            dataPrefs.edit().clear().apply()

            // Neustart der App
            requireActivity().recreate()
        }

        // Spenden Button
        btnDonate.setOnClickListener {
            openDonateLink()
        }
    }

    /**
     * Öffnet den Buy Me A Coffee Link im Browser
     */
    private fun openDonateLink() {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse("https://www.buymeacoffee.com/derbutcher")
            startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("SettingsFragment", "Could not open donate link: ${e.message}")
            android.widget.Toast.makeText(
                requireContext(),
                "Browser konnte nicht geöffnet werden",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
}