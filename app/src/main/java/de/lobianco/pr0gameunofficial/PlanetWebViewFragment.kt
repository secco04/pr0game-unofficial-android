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
            setupHorizontalScrollPriority()

            // Lade Planet Overview
            webView.loadUrl(planet.getUrl("overview"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupHorizontalScrollPriority() {
        // Allow ViewPager2 swiping when WebView is at horizontal scroll edges
        var startX = 0f
        var startY = 0f
        val SWIPE_THRESHOLD = 30 // Minimum pixels to recognize as intentional horizontal swipe

        webView.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    // Allow parent to intercept initially
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
                android.view.MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.x - startX
                    val deltaY = event.y - startY
                    val absDeltaX = Math.abs(deltaX)
                    val absDeltaY = Math.abs(deltaY)

                    // Require significant horizontal movement before considering it a swipe
                    if (absDeltaX > SWIPE_THRESHOLD || absDeltaY > SWIPE_THRESHOLD) {
                        // Clearly vertical scroll - always allow WebView to handle
                        if (absDeltaY > absDeltaX * 1.5) {
                            v.parent.requestDisallowInterceptTouchEvent(true)
                        }
                        // Clearly horizontal swipe
                        else if (absDeltaX > absDeltaY * 1.5) {
                            // Check if WebView can scroll in the direction of the swipe
                            val swipingLeft = deltaX < 0  // Moving finger left (content scrolls right)

                            val canScrollInSwipeDirection = if (swipingLeft) {
                                webView.canScrollHorizontally(1)  // Can scroll content right
                            } else {
                                webView.canScrollHorizontally(-1) // Can scroll content left
                            }

                            if (canScrollInSwipeDirection) {
                                // WebView needs the scroll - block ViewPager
                                v.parent.requestDisallowInterceptTouchEvent(true)
                            } else {
                                // WebView is at edge - allow ViewPager to handle tab switch
                                v.parent.requestDisallowInterceptTouchEvent(false)
                            }
                        }
                    }
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false // Don't consume the event, let WebView handle it
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

        // FIX: SwipeRefresh wird nur aktiviert wenn:
        // 1. Man ganz oben ist (mit kleiner Toleranz)
        // 2. Die WebView NICHT horizontal scrollen kann
        webView.setOnScrollChangeListener { _, scrollX, scrollY, _, _ ->
            val canScrollHorizontally = webView.canScrollHorizontally(-1) || webView.canScrollHorizontally(1)
            swipeRefresh.isEnabled = scrollY <= 10 && !canScrollHorizontally
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

                // CHECK FOR SESSION EXPIRY - if redirected to login page
                if (url?.contains("/index.php") == true && !url.contains("page=")) {
                    // We're on login page - session expired!
                    android.util.Log.d("PlanetFragment", "Session expired - on login page")
                    (activity as? MainActivity)?.handleSessionExpired()
                    return
                }

                // OPTIMIERT: Planeten nur alle 30 Sekunden überprüfen (nicht bei jeder Seite!)
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastPlanetCheckTime > 30000) { // 30 Sekunden
                    checkAndUpdatePlanets()
                    lastPlanetCheckTime = currentTime
                }

                // Check if redirected to login page (session timeout)
                if (url?.contains("index.php") == true || url?.contains("login") == true) {
                    android.util.Log.d("PlanetFragment", "========== LOGIN PAGE DETECTED ==========")
                    android.util.Log.d("PlanetFragment", "URL: $url")

                    // Unlock swipe (important for touch events!)
                    val mainActivity = activity as? MainActivity
                    android.util.Log.d("PlanetFragment", "MainActivity: $mainActivity")
                    android.util.Log.d("PlanetFragment", "isSwipeLocked: ${mainActivity?.isSwipeLocked}")
                    mainActivity?.unlockSwipe()

                    // Force remove all touch listeners and re-enable WebView
                    android.util.Log.d("PlanetFragment", "Removing touch listener and re-enabling WebView")
                    webView.setOnTouchListener(null)  // Remove touch listener completely!

                    // Enable WebView interaction
                    webView.requestFocus()
                    webView.isFocusable = true
                    webView.isFocusableInTouchMode = true
                    webView.isEnabled = true
                    webView.isClickable = true
                    webView.isLongClickable = true

                    android.util.Log.d("PlanetFragment", "WebView enabled: ${webView.isEnabled}")
                    android.util.Log.d("PlanetFragment", "WebView clickable: ${webView.isClickable}")
                    android.util.Log.d("PlanetFragment", "WebView focusable: ${webView.isFocusable}")

                    // Make sure parent ViewPager allows touch events
                    webView.parent?.requestDisallowInterceptTouchEvent(false)

                    // Inject JavaScript to test if page is interactive
                    webView.evaluateJavascript("""
                        (function() {
                            document.body.style.border = '5px solid red';
                            return 'Login page marked';
                        })();
                    """.trimIndent()) { result ->
                        android.util.Log.d("PlanetFragment", "JS test result: $result")
                    }

                    webView.postDelayed({
                        autoFillLoginCredentials()
                    }, 500)

                    android.util.Log.d("PlanetFragment", "========== LOGIN PAGE SETUP COMPLETE ==========")
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

                // Injiziere Empire Formatter auf Empire-Seite
                if (url?.contains("page=empire") == true || url?.contains("page=Empire") == true) {
                    webView.postDelayed({
                        injectEmpireFormatter()
                    }, 200)
                }

                // Injiziere Fleet Table Formatter auf Flotten-Seite
                if (url?.contains("page=fleetTable") == true || url?.contains("page=FleetTable") == true) {
                    webView.postDelayed({
                        injectFleetTableFormatter()
                    }, 200)
                }

                // Deaktiviere ViewPager Swipen auf Seiten mit horizontalem Scrollen
                val mainActivity = activity as? MainActivity
                if (url?.contains("page=Empire") == true ||
                    url?.contains("page=empire") == true ||
                    url?.contains("page=fleetTable") == true ||
                    url?.contains("page=FleetTable") == true) {
                    // Galaxy/Empire pages - swipe handled by lock state
                    android.util.Log.d("PlanetFragment", "Page with horizontal scrolling detected")
                } else {
                    android.util.Log.d("PlanetFragment", "Normal page")
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()

                // Nur pr0game URLs erlauben
                if (!url.contains(Config.BASE_DOMAIN)) {
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
        val isSwipeLocked = mainActivity?.isSwipeLocked ?: false

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
                val isSwipeLocked = mainActivity?.isSwipeLocked ?: false

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
    fun checkForNewMessages() {
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
                (activity as? MainActivity)?.updateMessageBadge(count)
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
                    // Prüfe ob wir bereits auf der "Alle Nachrichten"-Seite sind
                    const currentUrl = window.location.href;
                    if (currentUrl.includes('page=messages') && currentUrl.includes('category=100')) {
                        return 'already on page';
                    }
                    
                    // Finde den "Alle Nachrichten"-Link (category=100)
                    const links = document.querySelectorAll('a[href*="page=messages"][href*="category=100"]');
                    if (links.length > 0) {
                        links[0].click();
                        return 'clicked';
                    }
                    // Fallback: Lade URL direkt
                    window.location.href = 'game.php?page=messages&category=100';
                    return 'fallback';
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
     * Klickt auf den Imperium-Link via JavaScript
     */
    fun clickEmpireLink() {
        if (::webView.isInitialized) {
            val js = """
                (function() {
                    // Prüfe ob wir bereits auf der Imperium-Seite sind
                    const currentUrl = window.location.href;
                    if (currentUrl.includes('page=Empire') || currentUrl.includes('page=empire')) {
                        return 'already on page';
                    }
                    
                    // Finde den Imperium-Link
                    const links = document.querySelectorAll('a[href*="page=Empire"], a[href*="page=empire"]');
                    if (links.length > 0) {
                        links[0].click();
                        return 'clicked';
                    }
                    return 'not found';
                })();
            """.trimIndent()

            webView.evaluateJavascript(js) { result ->
                android.util.Log.d("PlanetFragment", "Click empire link result: $result")
            }
        }
    }

    /**
     * Klickt auf den Flotten-Link via JavaScript
     */
    fun clickFleetLink() {
        if (::webView.isInitialized) {
            val js = """
                (function() {
                    // Prüfe ob wir bereits auf der Flotten-Seite sind
                    const currentUrl = window.location.href;
                    if (currentUrl.includes('page=fleetTable') || currentUrl.includes('page=FleetTable')) {
                        return 'already on page';
                    }
                    
                    // Finde den Flotten-Link
                    const links = document.querySelectorAll('a[href*="page=fleetTable"], a[href*="page=FleetTable"]');
                    if (links.length > 0) {
                        links[0].click();
                        return 'clicked';
                    }
                    return 'not found';
                })();
            """.trimIndent()

            webView.evaluateJavascript(js) { result ->
                android.util.Log.d("PlanetFragment", "Click fleet link result: $result")
            }
        }
    }

    /**
     * Lädt die Übersichtsseite direkt mit dem korrekten Planeten-Parameter
     */
    fun clickOverviewLink() {
        if (::webView.isInitialized) {
            // Direkt die Overview-URL für diesen Planeten laden
            val overviewUrl = planet.getUrl("overview")
            android.util.Log.d("PlanetFragment", "Loading overview for planet ${planet.id}: $overviewUrl")
            webView.loadUrl(overviewUrl)
        }
    }

    /**
     * Klickt auf den Galaxy-Link via JavaScript
     */
    fun clickGalaxyLink() {
        if (::webView.isInitialized) {
            val js = """
                (function() {
                    const currentUrl = window.location.href;
                    if (currentUrl.includes('page=galaxy') || currentUrl.includes('page=Galaxy')) {
                        return 'already on page';
                    }
                    
                    const links = document.querySelectorAll('a[href*="page=galaxy"], a[href*="page=Galaxy"]');
                    if (links.length > 0) {
                        links[0].click();
                        return 'clicked';
                    }
                    return 'not found';
                })();
            """.trimIndent()

            webView.evaluateJavascript(js) { result ->
                android.util.Log.d("PlanetFragment", "Click galaxy link result: $result")
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
                    val prefs = requireContext().getSharedPreferences("pr0game_settings", android.content.Context.MODE_PRIVATE)
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
                        (activity as? MainActivity)?.onPlanetsLoaded(newPlanets)
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

        // Individual category settings - hide when show=false
        val hideCategories = mapOf(
            "0" to !prefs.getBoolean("show_category_spy_reports", true),
            "7" to !prefs.getBoolean("show_category_spy_defense", true),
            "1" to !prefs.getBoolean("show_category_player", true),
            "2" to !prefs.getBoolean("show_category_alliance", true),
            "3" to !prefs.getBoolean("show_category_combat", true),
            "4" to !prefs.getBoolean("show_category_system", true),
            "5" to !prefs.getBoolean("show_category_transport", true),
            "6" to !prefs.getBoolean("show_category_foreign_transport", true),
            "15" to !prefs.getBoolean("show_category_expedition", true),
            "50" to !prefs.getBoolean("show_category_game", true),
            "99" to !prefs.getBoolean("show_category_build_queue", true),
            "100" to !prefs.getBoolean("show_category_all", true),
            "998" to !prefs.getBoolean("show_category_favorites", true),
            "999" to !prefs.getBoolean("show_category_search", true)
        )

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
                    /* Nur das orange "Du hast eine Nachricht" Banner verstecken */
                    .message-box,
                    div.message-box,
                    .message-box .message,
                    div.message {
                        display: none !important;
                        visibility: hidden !important;
                        height: 0 !important;
                        overflow: hidden !important;
                    }
                `;
                """ else ""}
                
                
                style.textContent = css;
                document.head.appendChild(style);
                
                // JavaScript-basiertes Verstecken von Nachrichtenkategorien (präzises Matching)
                ${hideCategories.filter { it.value }.map { (categoryId, _) -> """
                document.querySelectorAll('.message-category-item').forEach(function(item) {
                    const link = item.querySelector('a[href*="category="]');
                    if (link) {
                        const href = link.getAttribute('href');
                        // Exaktes Match: category=$categoryId mit & oder Ende der URL
                        if (href.includes('category=$categoryId&') || href.includes('category=$categoryId\'') || href.match(/category=$categoryId$/)) {
                            item.style.display = 'none';
                        }
                    }
                });
                """ }.joinToString("\n")}
                
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
                
                // Überschreibe alle Links mit cp Parameter AGGRESSIV
                const forcePlanetId = '${planet.id}';
                
                // Funktion um cp Parameter zu erzwingen
                function forceCpParameter() {
                    document.querySelectorAll('a').forEach(function(link) {
                        if (link.href && link.href.includes('${Config.BASE_DOMAIN}')) {
                            try {
                                const url = new URL(link.href);
                                url.searchParams.set('cp', forcePlanetId);
                                link.href = url.toString();
                            } catch(e) {}
                        }
                    });
                }
                
                // Sofort ausführen
                forceCpParameter();
                
                // Bei jedem Klick prüfen
                document.addEventListener('click', function(e) {
                    forceCpParameter();
                }, true);
                
                // MutationObserver für dynamisch erstellte Links
                const linkObserver = new MutationObserver(function() {
                    forceCpParameter();
                });
                linkObserver.observe(document.body, { childList: true, subtree: true });
                
                // Erzwinge cp auch bei Form-Submits
                document.querySelectorAll('form').forEach(function(form) {
                    const cpInput = form.querySelector('input[name="cp"]');
                    if (!cpInput) {
                        const hidden = document.createElement('input');
                        hidden.type = 'hidden';
                        hidden.name = 'cp';
                        hidden.value = forcePlanetId;
                        form.appendChild(hidden);
                    } else {
                        cpInput.value = forcePlanetId;
                    }
                });
                
                ${if (hideMessageBanner) """
                // MutationObserver um das orange Nachrichten-Banner zu verstecken
                const hideMessageBanner = function() {
                    // Nur das spezifische orange Banner verstecken, nicht die Nachrichtenkategorien
                    const messageBanners = document.querySelectorAll('div.message-box');
                    messageBanners.forEach(function(banner) {
                        // Prüfe ob es wirklich das Banner ist (enthält einen Link zu messages)
                        const link = banner.querySelector('a[href*="page=messages"]');
                        if (link && banner.querySelector('.message')) {
                            banner.style.display = 'none';
                            banner.style.visibility = 'hidden';
                            banner.style.height = '0';
                            banner.style.overflow = 'hidden';
                        }
                    });
                };
                
                // Sofort ausführen
                hideMessageBanner();
                
                // Observer für neue Elemente
                const observer = new MutationObserver(function(mutations) {
                    hideMessageBanner();
                });
                
                observer.observe(document.body, {
                    childList: true,
                    subtree: true
                });
                """ else ""}
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

    /**
     * Injiziert Empire Formatter für mobile Ansicht
     */
    private fun injectEmpireFormatter() {
        val prefs = requireContext().getSharedPreferences("pr0game_settings", android.content.Context.MODE_PRIVATE)
        val empireStickyHeadersEnabled = prefs.getBoolean("empire_sticky_headers_enabled", true)

        if (!empireStickyHeadersEnabled) {
            android.util.Log.d("EmpireFormatter", "Empire Sticky Headers disabled in settings")
            return
        }

        val empireColumnWidth = prefs.getInt("empire_first_column_width", 120)
        val empireAlternatingColors = prefs.getBoolean("empire_alternating_colors_enabled", true)
        val script = EmpireFormatter.getFormatterScript(empireColumnWidth, empireAlternatingColors)

        android.util.Log.d("EmpireFormatter", "Injecting Empire Formatter Script with column width: $empireColumnWidth, alternating colors: $empireAlternatingColors")

        webView.evaluateJavascript(script) { result ->
            android.util.Log.d("EmpireFormatter", "Script executed: $result")
        }
    }

    private fun injectFleetTableFormatter() {
        val prefs = requireContext().getSharedPreferences("pr0game_settings", android.content.Context.MODE_PRIVATE)
        val fleetFontSize = prefs.getInt("fleet_font_size", 11)
        val script = FleetTableFormatter.getFormatterScript(fleetFontSize)

        android.util.Log.d("FleetTableFormatter", "Injecting Fleet Table Formatter Script with font size: ${fleetFontSize}px")

        webView.evaluateJavascript(script) { result ->
            android.util.Log.d("FleetTableFormatter", "Script executed: $result")
        }
    }

    private fun autoFillLoginCredentials(attempt: Int = 1) {
        // Check if fragment is still attached
        if (!isAdded || context == null) {
            android.util.Log.d("PlanetFragment", "Fragment not attached - skipping auto-fill")
            return
        }

        val prefs = requireContext().getSharedPreferences("pr0game_settings", android.content.Context.MODE_PRIVATE)
        val savedUsername = prefs.getString("login_username", "") ?: ""
        val savedPassword = prefs.getString("login_password", "") ?: ""

        android.util.Log.d("PlanetFragment", "Auto-fill attempt $attempt - Username: ${if (savedUsername.isNotEmpty()) "***" else "empty"}, Password: ${if (savedPassword.isNotEmpty()) "***" else "empty"}")

        if (savedUsername.isEmpty() || savedPassword.isEmpty()) {
            android.util.Log.d("PlanetFragment", "No credentials saved - opening keyboard for manual input")
            // Focus email field to open keyboard
            webView.evaluateJavascript("""
                (function() {
                    var emailField = document.querySelector('input[name="email"]') || 
                                    document.getElementById('email') ||
                                    document.querySelector('input[type="email"]');
                    if (emailField) {
                        emailField.focus();
                        return 'Focused email field';
                    }
                    return 'Email field not found';
                })();
            """.trimIndent(), null)
            return
        }

        // Escape quotes in credentials
        val escapedUsername = savedUsername.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"")
        val escapedPassword = savedPassword.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"")

        val jsCode = """
            (function() {
                // pr0game uses email field, not username
                var emailField = document.querySelector('input[name="email"]') || 
                                document.getElementById('email') ||
                                document.querySelector('input[type="email"]');
                var passwordField = document.querySelector('input[name="password"]') || 
                                   document.getElementById('password') ||
                                   document.querySelector('input[type="password"]');
                
                if (emailField && passwordField) {
                    emailField.value = '$escapedUsername';
                    passwordField.value = '$escapedPassword';
                    
                    // Trigger all possible events for validation
                    ['input', 'change', 'blur', 'keyup', 'keydown'].forEach(function(eventType) {
                        emailField.dispatchEvent(new Event(eventType, { bubbles: true }));
                        passwordField.dispatchEvent(new Event(eventType, { bubbles: true }));
                    });
                    
                    // Find and enable login button
                    var loginButton = document.querySelector('input[type="submit"]') ||
                                     document.querySelector('button[type="submit"]') ||
                                     document.querySelector('button[name="login"]') ||
                                     document.querySelector('input[name="login"]');
                    
                    if (loginButton) {
                        loginButton.disabled = false;
                        loginButton.removeAttribute('disabled');
                        
                        // Also trigger form validation if it exists
                        var form = emailField.form || passwordField.form;
                        if (form && form.checkValidity) {
                            try {
                                form.checkValidity();
                            } catch(e) {}
                        }
                    }
                    
                    // Focus password field to show keyboard
                    passwordField.focus();
                    
                    return 'SUCCESS: Auto-filled, button enabled, focused';
                }
                
                return 'FAIL: Email or password field not found';
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsCode) { result ->
            android.util.Log.d("PlanetFragment", "Auto-fill result: $result")

            // Retry if fields not found and we haven't tried too many times
            if (result.contains("FAIL") && attempt < 5 && isAdded) {
                android.util.Log.d("PlanetFragment", "Retrying auto-fill in 300ms...")
                webView.postDelayed({
                    autoFillLoginCredentials(attempt + 1)
                }, 300)
            }
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

    fun clickBuildingsLink() {
        if (::webView.isInitialized) {
            webView.evaluateJavascript("""
                (function() {
                    const url = window.location.href;
                    if (url.includes('page=buildings') || url.includes('page=Buildings')) return 'already';
                    const links = document.querySelectorAll('a[href*="page=buildings"], a[href*="page=Buildings"]');
                    if (links.length > 0) { links[0].click(); return 'clicked'; }
                    return 'not found';
                })();
            """.trimIndent()) { }
        }
    }

    fun clickShipyardLink() {
        if (::webView.isInitialized) {
            webView.evaluateJavascript("""
                (function() {
                    const url = window.location.href;
                    if (url.includes('page=shipyard') && url.includes('mode=fleet')) return 'already';
                    const links = document.querySelectorAll('a[href*="page=shipyard"][href*="mode=fleet"]');
                    if (links.length > 0) { links[0].click(); return 'clicked'; }
                    return 'not found';
                })();
            """.trimIndent()) { }
        }
    }

    fun clickDefenseLink() {
        if (::webView.isInitialized) {
            webView.evaluateJavascript("""
                (function() {
                    const url = window.location.href;
                    if (url.includes('page=shipyard') && url.includes('mode=defense')) return 'already';
                    const links = document.querySelectorAll('a[href*="page=shipyard"][href*="mode=defense"]');
                    if (links.length > 0) { links[0].click(); return 'clicked'; }
                    return 'not found';
                })();
            """.trimIndent()) { }
        }
    }

    fun clickResearchLink() {
        if (::webView.isInitialized) {
            webView.evaluateJavascript("""
                (function() {
                    const url = window.location.href;
                    if (url.includes('page=research')) return 'already';
                    const links = document.querySelectorAll('a[href*="page=research"]');
                    if (links.length > 0) { links[0].click(); return 'clicked'; }
                    return 'not found';
                })();
            """.trimIndent()) { }
        }
    }
}