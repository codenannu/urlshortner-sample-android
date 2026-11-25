package com.infoezeer.urlshortner.deeplink

import org.json.JSONObject

data class DeepLinkPayload(
    val version: Int,
    val linkCode: String,
    /** `deep_link` = app payload; `standard` = classic short URL → open destinationUrl. */
    val type: String,
    val screen: String,
    val paramsJson: String,
    val metadataJson: String,
    val expiresAt: String?,
    val requiresAuth: Boolean,
    val platformOverridesJson: String?,
    val fallbacksJson: String?,
    /** Set for STANDARD links (browser destination). */
    val destinationUrl: String?,
    val rawJson: String,
) {
    fun toPrettyJson(): String = rawJson

    val isStandardRedirect: Boolean
        get() = type.equals("standard", ignoreCase = true)
}

object DeepLinkPayloadParser {
    /**
     * Unwraps Shortener envelope `{ success, data }` or accepts a bare payload object.
     */
    fun parseEnvelope(body: String): DeepLinkPayload {
        val root = JSONObject(body)
        val data =
            when {
                root.has("data") && !root.isNull("data") -> root.getJSONObject("data")
                root.has("linkCode") -> root
                else -> throw IllegalArgumentException("Unexpected resolve response shape")
            }
        if (root.has("success") && !root.optBoolean("success", true)) {
            val err = root.optString("error", "Resolve failed")
            throw IllegalArgumentException(err)
        }
        val params = data.optJSONObject("params")
        val destination =
            data.optString("destinationUrl").ifBlank { null }
                ?: params?.optString("destinationUrl")?.ifBlank { null }
                ?: data.optJSONObject("fallbacks")?.optString("web")?.ifBlank { null }
        return DeepLinkPayload(
            version = data.optInt("version", 1),
            linkCode = data.optString("linkCode"),
            type = data.optString("type", "deep_link"),
            screen = data.optString("screen", "custom"),
            paramsJson = params?.toString(2) ?: "{}",
            metadataJson = data.optJSONObject("metadata")?.toString(2) ?: "{}",
            expiresAt =
                if (data.isNull("expiresAt")) {
                    null
                } else {
                    data.optString("expiresAt").ifBlank { null }
                },
            requiresAuth = data.optBoolean("requiresAuth", false),
            platformOverridesJson = data.optJSONObject("platformOverrides")?.toString(2),
            fallbacksJson = data.optJSONObject("fallbacks")?.toString(2),
            destinationUrl = destination,
            rawJson = data.toString(2),
        )
    }
}
