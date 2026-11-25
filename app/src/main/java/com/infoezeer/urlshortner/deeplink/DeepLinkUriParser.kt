package com.infoezeer.urlshortner.deeplink

import android.net.Uri
import java.net.URI

data class ParsedDeepLink(
    val incomingUrl: String,
    val host: String,
    val shortCode: String,
    /** Exact first `d=1` → open public Flow Inspector in browser instead of resolve. */
    val diagramMode: Boolean = false,
)

sealed class DeepLinkParseResult {
    data class Ok(val value: ParsedDeepLink) : DeepLinkParseResult()

    data class Err(val message: String) : DeepLinkParseResult()
}

/**
 * Extracts a short-link code from an incoming HTTPS App Link URI.
 * Rejects non-https schemes and hosts outside the configured allowlist.
 */
object DeepLinkUriParser {
    private val codePattern = Regex("^[A-Za-z0-9_-]{3,64}$")

    fun parse(uri: Uri?, allowedHosts: Set<String>): DeepLinkParseResult {
        if (uri == null) return DeepLinkParseResult.Err("No deep-link URI")
        return parseUrl(uri.toString(), allowedHosts)
    }

    /** JVM-friendly entry for unit tests and non-Android callers. */
    fun parseUrl(url: String?, allowedHosts: Set<String>): DeepLinkParseResult {
        if (url.isNullOrBlank()) return DeepLinkParseResult.Err("No deep-link URI")
        val uri =
            try {
                URI(url)
            } catch (_: Exception) {
                return DeepLinkParseResult.Err("Malformed URL")
            }
        val scheme = uri.scheme?.lowercase()
        if (scheme != "https") {
            return DeepLinkParseResult.Err("Unsupported scheme: ${scheme ?: "null"}")
        }
        val host = uri.host?.lowercase()
        if (host.isNullOrBlank()) {
            return DeepLinkParseResult.Err("Missing host")
        }
        if (allowedHosts.isNotEmpty() && host !in allowedHosts) {
            return DeepLinkParseResult.Err("Host not allowlisted: $host")
        }
        val path = uri.path ?: ""
        val segments = path.split('/').filter { it.isNotBlank() }
        if (segments.isEmpty()) {
            return DeepLinkParseResult.Err("Missing short code in path")
        }
        val code = segments.last().trim()
        if (segments.size >= 2 && segments[segments.lastIndex - 1].equals("deeplink", true)) {
            if (!codePattern.matches(code)) {
                return DeepLinkParseResult.Err("Invalid short code format")
            }
            val diagramMode = isDiagramMode(uri.rawQuery)
            return DeepLinkParseResult.Ok(ParsedDeepLink(url, host, code, diagramMode))
        }
        if (code.equals("deeplink", ignoreCase = true)) {
            return DeepLinkParseResult.Err("Missing short code after /deeplink/")
        }
        if (!codePattern.matches(code)) {
            return DeepLinkParseResult.Err("Invalid short code format")
        }
        val diagramMode = isDiagramMode(uri.rawQuery)
        return DeepLinkParseResult.Ok(ParsedDeepLink(url, host, code, diagramMode))
    }

    /** First `d` query value must be exactly `1`. */
    fun isDiagramMode(rawQuery: String?): Boolean {
        if (rawQuery.isNullOrBlank()) return false
        val first =
            rawQuery
                .split('&')
                .asSequence()
                .map { it.split('=', limit = 2) }
                .firstOrNull { it.isNotEmpty() && it[0] == "d" }
        return first != null && first.getOrNull(1) == "1"
    }
}
