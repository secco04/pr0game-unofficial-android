package de.lobianco.pr0gameunofficial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class PlanetWebViewFragment : Fragment() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var planet: Planet

    companion object {
        fun newInstance(planet: Planet): PlanetWebViewFragment {
            val fragment = PlanetWebViewFragment()
            val args = Bundle()
            args.putString("planet_id", planet.id)
            args.putString("planet_name", planet.name)
            args.putString("planet_coords", planet.coordinates)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            planet = Planet(
                it.getString("planet_id")!!,
                it.getString("planet_name")!!,
                it.getString("planet_coords")!!
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_planet_webview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            webView = view.findViewById(R.id.webview)
            progressBar = view.findViewById(R.id.progressBar)
            swipeRefresh = view.findViewById(R.id.swipeRefresh)

            setupSwipeRefresh()
            setupWebView()

            // Lade Planet Overview
            webView.loadUrl(planet.getUrl("overview"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupSwipeRefresh() {
        // Farben für den Refresh-Indicator
        swipeRefresh.setColorSchemeColors(
            0xFF64b5f6.toInt(), // Blau
            0xFF4FC3F7.toInt(), // Hell-Blau
            0xFF29B6F6.toInt()  // Mittel-Blau
        )
        swipeRefresh.setProgressBackgroundColorSchemeColor(0xFF16213e.toInt())

        // Nur aktivieren wenn ViewPager nicht gesperrt ist
        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }

        // Deaktiviere Swipe-to-Refresh wenn WebView nicht ganz oben ist
        webView.viewTreeObserver.addOnScrollChangedListener {
            swipeRefresh.isEnabled = webView.scrollY == 0
        }
    }

    private fun setupWebView() {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
                swipeRefresh.isRefreshing = false // Stop refresh animation

                // Injiziere JavaScript um cp Parameter zu erzwingen
                injectPlanetLock()

                // Injiziere Galaxy Formatter wenn aktiviert und auf Galaxy-Seite
                if (url?.contains("page=galaxy") == true) {
                    val prefs = requireContext().getSharedPreferences("pr0game_settings", android.content.Context.MODE_PRIVATE)
                    val isEnabled = prefs.getBoolean("galaxy_formatter_enabled", true)
                    val delay = prefs.getInt("galaxy_formatter_delay", 200).toLong()

                    if (isEnabled) {
                        webView.postDelayed({
                            injectGalaxyFormatter()
                        }, delay)
                    }
                }

                // Deaktiviere ViewPager Swipen auf Seiten mit horizontalem Scrollen
                val mainActivity = activity as? MainActivity
                if (url?.contains("page=Empire") == true ||
                    url?.contains("page=empire") == true ||
                    url?.contains("page=fleetTable") == true ||
                    url?.contains("page=FleetTable") == true) {
                    mainActivity?.setViewPagerSwipeEnabled(false)
                    android.util.Log.d("PlanetFragment", "Disabled ViewPager swipe on page with horizontal scrolling")
                } else {
                    mainActivity?.setViewPagerSwipeEnabled(true)
                    android.util.Log.d("PlanetFragment", "Enabled ViewPager swipe")
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()

                // Nur pr0game.com URLs erlauben
                if (!url.contains("pr0game.com")) {
                    return true
                }

                // Füge cp Parameter hinzu wenn nicht vorhanden
                val modifiedUrl = ensurePlanetParameter(url)
                view?.loadUrl(modifiedUrl)
                return true
            }
        }

        val settings: WebSettings = webView.settings

        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true

        // MAXIMALES CACHING
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

        // Performance
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            settings.offscreenPreRaster = true
        }

        // Cookies
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)
    }

    /**
     * Stellt sicher dass jede URL den cp Parameter für diesen Planeten hat
     */
    private fun ensurePlanetParameter(url: String): String {
        // Wenn URL bereits cp Parameter hat, ersetze ihn
        val cpRegex = """[&?]cp=\d+""".toRegex()
        var modifiedUrl = url.replace(cpRegex, "")

        // Füge korrekten cp Parameter hinzu
        val separator = if (modifiedUrl.contains("?")) "&" else "?"
        modifiedUrl += "${separator}cp=${planet.id}"

        return modifiedUrl
    }

    /**
     * Injiziert JavaScript um Planet-Wechsel zu verhindern
     */
    private fun injectPlanetLock() {
        val js = """
            (function() {
                // Blockiere planetSelector Änderungen
                const selector = document.getElementById('planetSelector');
                if (selector) {
                    selector.addEventListener('change', function(e) {
                        e.preventDefault();
                        e.stopPropagation();
                        this.value = '${planet.id}';
                        return false;
                    }, true);
                    
                    // Setze aktuellen Planet
                    selector.value = '${planet.id}';
                }
                
                // Überschreibe alle Links mit cp Parameter
                document.querySelectorAll('a').forEach(function(link) {
                    if (link.href.includes('pr0game.com')) {
                        const url = new URL(link.href);
                        url.searchParams.set('cp', '${planet.id}');
                        link.href = url.toString();
                    }
                });
            })();
        """.trimIndent()

        webView.evaluateJavascript(js, null)
    }

    /**
     * Injiziert Galaxy Formatter für mobile Ansicht
     */
    private fun injectGalaxyFormatter() {
        val prefs = requireContext().getSharedPreferences("pr0game_settings", android.content.Context.MODE_PRIVATE)
        val rowHeight = prefs.getInt("galaxy_row_height", 20)

        android.util.Log.d("GalaxyFormatter", "Loading row height from prefs: ${rowHeight}px")

        val script = GalaxyFormatter.getFormatterScript(rowHeight)

        // Debug: Log dass wir injizieren
        android.util.Log.d("GalaxyFormatter", "Injecting Galaxy Formatter Script with row height: ${rowHeight}px")

        webView.evaluateJavascript(script) { result ->
            android.util.Log.d("GalaxyFormatter", "Script executed: $result")
        }
    }

    fun canGoBack(): Boolean = if (::webView.isInitialized) webView.canGoBack() else false
    fun goBack() {
        if (::webView.isInitialized) {
            webView.goBack()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::webView.isInitialized) {
            webView.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::webView.isInitialized) {
            webView.onResume()
        }
    }
}