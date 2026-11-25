package com.infoezeer.urlshortner.deeplink

import com.infoezeer.urlshortner.BuildConfig

/**
 * Central sample configuration. Clients should supply their own API base and hosts
 * via Gradle properties (`API_BASE_URL`, `ALLOWED_HOSTS`, `DEEP_LINK_HOST`) — never commit secrets.
 */
object DeepLinkConfig {
    val apiBaseUrl: String
        get() = BuildConfig.API_BASE_URL.trimEnd('/')

    val allowedHosts: Set<String>
        get() =
            BuildConfig.ALLOWED_HOSTS
                .split(',')
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() }
                .toSet()

    val httpTimeoutMs: Long
        get() = BuildConfig.HTTP_TIMEOUT_MS

    val debugLogging: Boolean
        get() = BuildConfig.DEBUG_LOGGING

    val postAppOpen: Boolean
        get() = BuildConfig.POST_APP_OPEN

    /** Admin / WEB_FALLBACK origin hosting `/deeplink/{code}?d=1` public inspector. */
    val webFallbackBaseUrl: String
        get() = BuildConfig.WEB_FALLBACK_BASE_URL.trimEnd('/')

    fun publicDiagramInspectorUrl(shortCode: String, host: String): String {
        val q = "d=1&host=${java.net.URLEncoder.encode(host, Charsets.UTF_8.name())}"
        return "$webFallbackBaseUrl/deeplink/$shortCode?$q"
    }
}
