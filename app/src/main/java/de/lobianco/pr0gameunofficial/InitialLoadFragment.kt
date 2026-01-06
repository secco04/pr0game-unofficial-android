package de.lobianco.pr0gameunofficial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * Fragment zum initialen Laden von pr0game.com
 * Liest Planeten-Liste aus dem HTML aus
 */
class InitialLoadFragment : Fragment() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private var statusText: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_initial_load, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.webview)
        progressBar = view.findViewById(R.id.progressBar)
        statusText = view.findViewById(R.id.statusText)

        setupWebView()
        webView.loadUrl("https://pr0game.com")
    }

    private fun setupWebView() {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Extrahiere HTML mit JavaScript
                webView.evaluateJavascript(
                    "(function() { return document.documentElement.outerHTML; })();"
                ) { html ->
                    val cleanHtml = html?.replace("\\u003C", "<")
                        ?.replace("\\\"", "\"")
                        ?.replace("\\n", "\n") ?: ""

                    // Parse Planeten
                    val planets = PlanetParser.parseFromHtml(cleanHtml)

                    if (planets.isNotEmpty()) {
                        // Übergebe an MainActivity
                        (activity as? MainActivity)?.onPlanetsLoaded(planets)
                    } else {
                        // Zeige WebView für Login - OHNE Text-Overlay
                        progressBar.visibility = View.GONE
                        statusText?.visibility = View.GONE  // Text verstecken!
                        webView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                // Kein Text mehr während des Ladens
            }
        }

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)
    }
}