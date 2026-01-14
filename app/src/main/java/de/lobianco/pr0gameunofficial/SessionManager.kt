package de.lobianco.pr0gameunofficial

import android.content.Context
import android.webkit.CookieManager
import kotlinx.coroutines.*

/**
 * Monitors session cookie expiration and triggers logout when session expires
 */
class SessionManager(private val context: Context) {
    
    private var sessionCheckJob: Job? = null
    private var sessionExpiredCallback: (() -> Unit)? = null
    
    companion object {
        private const val CHECK_INTERVAL_MS = 30000L // Check every 30 seconds
        private const val SESSION_COOKIE_NAME = "PHPSESSID" // pr0game session cookie name
    }
    
    /**
     * Start monitoring session
     */
    fun startMonitoring(onSessionExpired: () -> Unit) {
        sessionExpiredCallback = onSessionExpired
        
        sessionCheckJob?.cancel()
        sessionCheckJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                delay(CHECK_INTERVAL_MS)
                
                if (isSessionExpired()) {
                    withContext(Dispatchers.Main) {
                        sessionExpiredCallback?.invoke()
                    }
                    break
                }
            }
        }
    }
    
    /**
     * Stop monitoring
     */
    fun stopMonitoring() {
        sessionCheckJob?.cancel()
        sessionCheckJob = null
    }
    
    /**
     * Check if session is expired by checking if session cookie exists
     */
    private fun isSessionExpired(): Boolean {
        val cookieManager = CookieManager.getInstance()
        val cookies = cookieManager.getCookie(Config.BASE_URL) ?: return true
        
        // Check if PHPSESSID cookie exists
        val sessionCookieExists = cookies.split(";")
            .map { it.trim() }
            .any { it.startsWith("$SESSION_COOKIE_NAME=") }
        
        return !sessionCookieExists
    }
    
    /**
     * Manual check if session is valid
     */
    fun isSessionValid(): Boolean {
        return !isSessionExpired()
    }
}
