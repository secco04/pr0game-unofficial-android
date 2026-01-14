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
        webView.loadUrl(Config.BASE_URL)
    }

    private fun setupWebView() {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Auto-fill login credentials if saved - with delay for DOM to be ready
                webView.postDelayed({
                    autoFillLoginCredentials()
                }, 500) // 500ms delay

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
                        android.util.Log.d("InitialLoad", "Showing login page - enabling interaction")

                        // Hide loading screen COMPLETELY
                        progressBar.visibility = View.GONE
                        statusText?.visibility = View.GONE

                        // Hide the ENTIRE LinearLayout parent to prevent touch blocking
                        val loadingLayout = view?.findViewById<View>(R.id.loadingLayout)
                        if (loadingLayout != null) {
                            loadingLayout.visibility = View.GONE
                            android.util.Log.d("InitialLoad", "Loading layout hidden")
                        } else {
                            // Try finding by parent
                            (progressBar.parent as? View)?.visibility = View.GONE
                            android.util.Log.d("InitialLoad", "Loading layout parent hidden")
                        }

                        // Show WebView
                        webView.visibility = View.VISIBLE

                        // Enable WebView interaction explicitly
                        webView.requestFocus()
                        webView.isFocusable = true
                        webView.isFocusableInTouchMode = true
                        webView.isEnabled = true
                        webView.isClickable = true
                        webView.isLongClickable = true

                        // Bring WebView to front
                        webView.bringToFront()
                        webView.invalidate()

                        android.util.Log.d("InitialLoad", "WebView enabled: ${webView.isEnabled}, clickable: ${webView.isClickable}, visible: ${webView.visibility == View.VISIBLE}")
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

    private fun autoFillLoginCredentials(attempt: Int = 1) {
        // Check if fragment is still attached
        if (!isAdded || context == null) {
            android.util.Log.d("InitialLoad", "Fragment not attached - skipping auto-fill")
            return
        }

        // Ensure WebView is interactive
        webView.requestFocus()
        webView.isFocusable = true
        webView.isFocusableInTouchMode = true
        webView.isEnabled = true
        webView.isClickable = true

        val prefs = requireContext().getSharedPreferences("pr0game_settings", android.content.Context.MODE_PRIVATE)
        val savedUsername = prefs.getString("login_username", "") ?: ""
        val savedPassword = prefs.getString("login_password", "") ?: ""

        android.util.Log.d("InitialLoad", "Auto-fill attempt $attempt - Username: ${if (savedUsername.isNotEmpty()) "***" else "empty"}, Password: ${if (savedPassword.isNotEmpty()) "***" else "empty"}")

        if (savedUsername.isEmpty() || savedPassword.isEmpty()) {
            android.util.Log.d("InitialLoad", "No credentials saved - opening keyboard for manual input")
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
                // Debug: List all input fields
                var allInputs = document.querySelectorAll('input');
                var inputInfo = 'Total inputs: ' + allInputs.length + '; ';
                for(var i = 0; i < allInputs.length; i++) {
                    inputInfo += 'Input' + i + ': name=' + allInputs[i].name + 
                                 ' type=' + allInputs[i].type + 
                                 ' id=' + allInputs[i].id + '; ';
                }
                console.log(inputInfo);
                
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
                    
                    // Focus password field to show it's ready
                    passwordField.focus();
                    
                    return 'SUCCESS: Auto-filled email=' + emailField.value.substring(0, 3) + '*** and password=***';
                }
                
                return 'FAIL: ' + inputInfo + ' Email field=' + (emailField ? 'found' : 'NOT FOUND') + 
                       ', Password field=' + (passwordField ? 'found' : 'NOT FOUND');
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsCode) { result ->
            android.util.Log.d("InitialLoad", "Auto-fill result: $result")

            // Retry if fields not found and we haven't tried too many times
            // Also check if fragment is still attached before retrying
            if (result.contains("NOT FOUND") && attempt < 5 && isAdded) {
                android.util.Log.d("InitialLoad", "Retrying auto-fill in 300ms...")
                webView.postDelayed({
                    autoFillLoginCredentials(attempt + 1)
                }, 300)
            }
        }
    }
}