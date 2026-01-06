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
    private var lastPlanetCheckTime: Long = 0

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

        // OPTIMIERT: Nutze setOnScrollChangeListener statt ViewTreeObserver
        webView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            swipeRefresh.isEnabled = scrollY == 0
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

                // OPTIMIERT: Planeten nur alle 30 Sekunden überprüfen (nicht bei jeder Seite!)
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastPlanetCheckTime > 30000) { // 30 Sekunden
                    checkAndUpdatePlanets()
                    lastPlanetCheckTime = currentTime
                }

                // Injiziere JavaScript um cp Parameter zu erzwingen
                injectPlanetLock()

                // Prüfe auf neue Nachrichten und aktualisiere Badge
                checkForNewMessages()

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

                    // Aktiviere Galaxy Navigation wenn Swipe gelockt ist
                    setupGalaxySwipeNavigation()
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

                // Füge cp Parameter hinzu
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

        // PERFORMANCE OPTIMIERUNGEN
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            settings.offscreenPreRaster = true
        }

        // Hardware-Beschleunigung aktivieren
        webView.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

        // Unnötige Features deaktivieren für bessere Performance
        settings.setGeolocationEnabled(false)
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        settings.displayZoomControls = false

        // Medien nur auf Anfrage laden (spart CPU/Bandbreite)
        settings.mediaPlaybackRequiresUserGesture = true

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
     * Richtet Swipe-Navigation für die Galaxy-Ansicht ein
     * Wenn Swipe gelockt ist, können wir zwischen Systemen navigieren
     */
    private fun setupGalaxySwipeNavigation() {
        val mainActivity = activity as? MainActivity
        val isSwipeLocked = mainActivity?.isSwipeLocked() ?: false

        // Prüfe ob Galaxy Navigation in den Einstellungen aktiviert ist
        val prefs = requireContext().getSharedPreferences("pr0game_settings", android.content.Context.MODE_PRIVATE)
        val isNavigationEnabled = prefs.getBoolean("galaxy_navigation_enabled", true)

        if (isSwipeLocked && isNavigationEnabled) {
            injectGalaxySwipeHandler()
        } else {
            removeGalaxySwipeHandler()
        }
    }

    /**
     * Injiziert den Touch-Handler für Galaxy Swipe Navigation
     */
    private fun injectGalaxySwipeHandler() {
        android.util.Log.d("GalaxySwipe", "Injecting galaxy swipe handler")

        val js = """
            (function() {
                // Entferne alten Handler falls vorhanden
                if (window.galaxySwipeHandler) {
                    document.removeEventListener('touchstart', window.galaxySwipeHandler.start);
                    document.removeEventListener('touchmove', window.galaxySwipeHandler.move);
                    document.removeEventListener('touchend', window.galaxySwipeHandler.end);
                }
                
                let startX = 0;
                let startY = 0;
                let isHorizontalSwipe = false;
                
                const handleTouchStart = function(e) {
                    startX = e.touches[0].clientX;
                    startY = e.touches[0].clientY;
                    isHorizontalSwipe = false;
                };
                
                const handleTouchMove = function(e) {
                    // Erkenne früh ob es ein horizontaler oder vertikaler Swipe ist
                    if (!isHorizontalSwipe) {
                        const currentX = e.touches[0].clientX;
                        const currentY = e.touches[0].clientY;
                        const diffX = Math.abs(currentX - startX);
                        const diffY = Math.abs(currentY - startY);
                        
                        // Wenn horizontal-Bewegung dominiert
                        if (diffX > 20 && diffX > diffY * 1.5) {
                            isHorizontalSwipe = true;
                            // Verhindere Scroll/Refresh bei horizontalem Swipe
                            e.preventDefault();
                        }
                    } else {
                        // Weiterhin verhindern während horizontalem Swipe
                        e.preventDefault();
                    }
                };
                
                const handleTouchEnd = function(e) {
                    const endX = e.changedTouches[0].clientX;
                    const endY = e.changedTouches[0].clientY;
                    
                    const diffX = endX - startX;
                    const diffY = endY - startY;
                    
                    const absDiffX = Math.abs(diffX);
                    const absDiffY = Math.abs(diffY);
                    
                    // LOCKERER: Horizontal muss nur 0.7x größer sein als vertikal
                    // Und nur 60px Mindestdistanz
                    if (absDiffX > absDiffY * 0.7 && absDiffX > 60) {
                        const systemInput = document.querySelector('input[name="system"]');
                        const submitButton = document.querySelector('input[type="submit"][value="Anzeigen"]');
                        
                        if (systemInput && submitButton) {
                            let currentSystem = parseInt(systemInput.value);
                            
                            if (diffX > 0) {
                                // Swipe nach rechts = System runter
                                currentSystem--;
                            } else {
                                // Swipe nach links = System hoch
                                currentSystem++;
                            }
                            
                            // Begrenze auf 1-499
                            if (currentSystem >= 1 && currentSystem <= 499) {
                                systemInput.value = currentSystem;
                                submitButton.click();
                            }
                        }
                    }
                };
                
                document.addEventListener('touchstart', handleTouchStart, { passive: true });
                document.addEventListener('touchmove', handleTouchMove, { passive: false }); // Nicht passive!
                document.addEventListener('touchend', handleTouchEnd, { passive: true });
                
                // Speichere Handler für späteres Entfernen
                window.galaxySwipeHandler = {
                    start: handleTouchStart,
                    move: handleTouchMove,
                    end: handleTouchEnd
                };
            })();
        """.trimIndent()

        webView.evaluateJavascript(js, null)
    }

    /**
     * Entfernt den Galaxy Swipe Handler
     */
    private fun removeGalaxySwipeHandler() {
        android.util.Log.d("GalaxySwipe", "Removing galaxy swipe handler")

        val js = """
            (function() {
                if (window.galaxySwipeHandler) {
                    document.removeEventListener('touchstart', window.galaxySwipeHandler.start);
                    document.removeEventListener('touchmove', window.galaxySwipeHandler.move);
                    document.removeEventListener('touchend', window.galaxySwipeHandler.end);
                    delete window.galaxySwipeHandler;
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(js, null)
    }

    /**
     * Wird von MainActivity aufgerufen wenn sich der Lock-Status ändert
     */
    fun onSwipeLockChanged(isLocked: Boolean) {
        // Prüfe ob WebView bereits initialisiert ist
        if (!::webView.isInitialized) {
            android.util.Log.d("PlanetFragment", "WebView not initialized yet, ignoring lock change")
            return
        }

        // Nur auf Galaxy-Seite reagieren
        webView.url?.let { url ->
            if (url.contains("page=galaxy")) {
                // Prüfe ob Galaxy Navigation aktiviert ist
                val prefs = requireContext().getSharedPreferences("pr0game_settings", android.content.Context.MODE_PRIVATE)
                val isNavigationEnabled = prefs.getBoolean("galaxy_navigation_enabled", true)

                if (isLocked && isNavigationEnabled) {
                    injectGalaxySwipeHandler()
                } else {
                    removeGalaxySwipeHandler()
                }
            }
        }
    }

    /**
     * Wird von MainActivity aufgerufen wenn sich die Galaxy Navigation Einstellung ändert
     */
    fun onGalaxyNavigationSettingChanged(enabled: Boolean) {
        // Prüfe ob WebView bereits initialisiert ist
        if (!::webView.isInitialized) {
            android.util.Log.d("PlanetFragment", "WebView not initialized yet, ignoring setting change")
            return
        }

        // Nur auf Galaxy-Seite reagieren
        webView.url?.let { url ->
            if (url.contains("page=galaxy")) {
                val mainActivity = activity as? MainActivity
                val isSwipeLocked = mainActivity?.isSwipeLocked() ?: false

                if (enabled && isSwipeLocked) {
                    injectGalaxySwipeHandler()
                } else {
                    removeGalaxySwipeHandler()
                }
            }
        }
    }

    /**
     * Prüft auf neue Nachrichten und aktualisiert das Badge
     */
    private fun checkForNewMessages() {
        // Gesamte Nachrichten
        val jsMessages = """
            (function() {
                const newMesNum = document.getElementById('newmesnum');
                if (newMesNum) {
                    return parseInt(newMesNum.textContent);
                }
                return 0;
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsMessages) { result ->
            try {
                val count = result?.replace("\"", "")?.toIntOrNull() ?: 0
                (activity as? MainActivity)?.updateMessagesBadge(count)
            } catch (e: Exception) {
                android.util.Log.e("Messages", "Error parsing message count: ${e.message}")
            }
        }

        // Spionageberichte (unread_0)
        val jsSpyReports = """
            (function() {
                const unreadSpyReports = document.getElementById('unread_0');
                if (unreadSpyReports) {
                    return parseInt(unreadSpyReports.textContent);
                }
                return 0;
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsSpyReports) { result ->
            try {
                val count = result?.replace("\"", "")?.toIntOrNull() ?: 0
                (activity as? MainActivity)?.updateSpyReportsBadge(count)
                android.util.Log.d("SpyReports", "Unread spy reports: $count")
            } catch (e: Exception) {
                android.util.Log.e("SpyReports", "Error parsing spy report count: ${e.message}")
            }
        }
    }

    /**
     * Lädt die Nachrichten-Seite
     */
    fun loadMessagesPage() {
        if (::webView.isInitialized) {
            val messagesUrl = planet.getUrl("messages")
            android.util.Log.d("Messages", "Loading messages page: $messagesUrl")
            webView.loadUrl(messagesUrl)
        } else {
            android.util.Log.e("Messages", "WebView not initialized")
        }
    }

    /**
     * Lädt eine beliebige URL in der WebView
     */
    fun loadUrl(url: String) {
        if (::webView.isInitialized) {
            // Füge cp Parameter hinzu
            val urlWithCp = ensurePlanetParameter(url)
            android.util.Log.d("PlanetFragment", "Direct loadUrl: $urlWithCp")

            // Lade direkt - wird durch shouldOverrideUrlLoading gehen aber URL ist schon korrekt
            webView.loadUrl(urlWithCp)
        } else {
            android.util.Log.e("PlanetFragment", "WebView not initialized")
        }
    }

    /**
     * Klickt auf den Nachrichten-Link im Menü via JavaScript
     */
    fun clickMessagesLink() {
        if (::webView.isInitialized) {
            val js = """
                (function() {
                    // Prüfe ob wir bereits auf der Nachrichten-Seite sind
                    const currentUrl = window.location.href;
                    if (currentUrl.includes('page=messages') && !currentUrl.includes('category=')) {
                        return 'already on page';
                    }
                    
                    // Finde den Nachrichten-Link im Menü (ohne category)
                    const links = document.querySelectorAll('a[href*="page=messages"]:not([href*="category"])');
                    if (links.length > 0) {
                        links[0].click();
                        return 'clicked';
                    }
                    return 'not found';
                })();
            """.trimIndent()

            webView.evaluateJavascript(js) { result ->
                android.util.Log.d("PlanetFragment", "Click messages link result: $result")
            }
        }
    }

    /**
     * Klickt auf den Spionageberichte-Link via JavaScript
     */
    fun clickSpyReportsLink() {
        if (::webView.isInitialized) {
            val js = """
                (function() {
                    // Prüfe ob wir bereits auf der Spionageberichte-Seite sind
                    const currentUrl = window.location.href;
                    if (currentUrl.includes('page=messages') && currentUrl.includes('category=0')) {
                        return 'already on page';
                    }
                    
                    // Finde den Spionageberichte-Link (category=0)
                    const links = document.querySelectorAll('a[href*="page=messages"][href*="category=0"]');
                    if (links.length > 0) {
                        links[0].click();
                        return 'clicked';
                    }
                    // Fallback: Lade URL direkt
                    window.location.href = 'game.php?page=messages&category=0';
                    return 'fallback';
                })();
            """.trimIndent()

            webView.evaluateJavascript(js) { result ->
                android.util.Log.d("PlanetFragment", "Click spy reports link result: $result")
            }
        }
    }

    /**
     * Überprüft ob neue Planeten hinzugekommen sind und aktualisiert die Liste automatisch
     */
    private fun checkAndUpdatePlanets() {
        val js = """
            (function() {
                const selector = document.getElementById('planetSelector');
                if (!selector) return null;
                
                const planets = [];
                for (let option of selector.options) {
                    planets.push({
                        id: option.value,
                        name: option.textContent.trim().split('[')[0].trim(),
                        coords: option.textContent.match(/\[([^\]]+)\]/)?.[1] || ''
                    });
                }
                return JSON.stringify(planets);
            })();
        """.trimIndent()

        webView.evaluateJavascript(js) { result ->
            if (result != null && result != "null" && result != "\"null\"") {
                try {
                    // Entferne Anführungszeichen und parse JSON
                    val cleanResult = result.trim('"').replace("\\\"", "\"")
                    val jsonArray = org.json.JSONArray(cleanResult)

                    val newPlanets = mutableListOf<Planet>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        newPlanets.add(
                            Planet(
                                obj.getString("id"),
                                obj.getString("name"),
                                obj.getString("coords")
                            )
                        )
                    }

                    // Vergleiche mit aktuellen Planeten
                    val prefs = requireContext().getSharedPreferences("pr0game_data", android.content.Context.MODE_PRIVATE)
                    val savedPlanets = prefs.getString("planets_json", null)
                    val currentPlanets = if (savedPlanets != null) {
                        PlanetParser.fromJson(savedPlanets)
                    } else {
                        emptyList()
                    }

                    // Prüfe ob sich die Liste geändert hat
                    if (newPlanets.size != currentPlanets.size ||
                        newPlanets.map { it.id }.toSet() != currentPlanets.map { it.id }.toSet()) {

                        android.util.Log.d("PlanetUpdate", "Planet list changed! Old: ${currentPlanets.size}, New: ${newPlanets.size}")

                        // Aktualisiere MainActivity mit neuer Planetenliste
                        (activity as? MainActivity)?.onPlanetsUpdated(newPlanets)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PlanetUpdate", "Error parsing planets: ${e.message}")
                }
            }
        }
    }

    /**
     * Injiziert JavaScript um Planet-Wechsel zu verhindern
     */
    private fun injectPlanetLock() {
        val prefs = requireContext().getSharedPreferences("pr0game_settings", android.content.Context.MODE_PRIVATE)
        val hidePlanetSelector = prefs.getBoolean("hide_planet_selector", true)
        val hideMessageBanner = prefs.getBoolean("hide_message_banner", true)

        val js = """
            (function() {
                // CSS injizieren basierend auf Einstellungen
                const style = document.createElement('style');
                let css = '';
                
                ${if (hidePlanetSelector) """
                css += `
                    #planetSelector { 
                        display: none !important; 
                    }
                `;
                """ else ""}
                
                ${if (hideMessageBanner) """
                css += `
                    .message-box,
                    div.message-box {
                        display: none !important;
                    }
                `;
                """ else ""}
                
                style.textContent = css;
                document.head.appendChild(style);
                
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