package com.infoezeer.urlshortner.deeplink

/**
 * Parse Shortener deferred token from Play Install Referrer string.
 * Expected: `cw_dl=OPAQUE_TOKEN&v=1` (plus optional other params).
 */
object InstallReferrerParser {
    private const val KEY = "cw_dl"
    private const val VERSION = "1"

    fun parseDeferredToken(installReferrer: String?): String? {
        if (installReferrer.isNullOrBlank()) return null
        return try {
            val params =
                installReferrer.trim().split('&').associate { part ->
                    val idx = part.indexOf('=')
                    if (idx <= 0) "" to ""
                    else {
                        val k = part.substring(0, idx)
                        val v = part.substring(idx + 1)
                        k to java.net.URLDecoder.decode(v, Charsets.UTF_8.name())
                    }
                }
            val token = params[KEY]?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            val version = params["v"]
            if (version != null && version != VERSION) return null
            token
        } catch (_: Exception) {
            null
        }
    }

    /** Never log the full bearer token. */
    fun redact(token: String?): String {
        if (token.isNullOrEmpty()) return "[empty]"
        if (token.length <= 8) return "***"
        return "${token.take(4)}…${token.takeLast(2)}(len=${token.length})"
    }
}
