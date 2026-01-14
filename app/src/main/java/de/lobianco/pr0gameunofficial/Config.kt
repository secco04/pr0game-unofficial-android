package de.lobianco.pr0gameunofficial

/**
 * Zentrale Konfiguration für die App
 *
 * Um zwischen Production und Dev-System zu wechseln:
 * - Production: BASE_DOMAIN = "pr0game.com"
 * - Dev/Test:   BASE_DOMAIN = "test.pr0game.com"
 */
object Config {
    /**
     * Base Domain für pr0game
     *
     * PRODUCTION: "pr0game.com"
     * DEV/TEST:   "test.pr0game.com"
     */
    const val BASE_DOMAIN = "test.pr0game.com"

    /**
     * Vollständige Base URL mit HTTPS
     */
    const val BASE_URL = "https://$BASE_DOMAIN"
}