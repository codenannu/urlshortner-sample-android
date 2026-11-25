package com.infoezeer.urlshortner.deeplink

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

sealed class ResolveResult {
    data class Success(val payload: DeepLinkPayload) : ResolveResult()

    data class Failure(val message: String, val httpCode: Int? = null) : ResolveResult()
}

/**
 * Calls the public Shortener resolve API. Never embed MMID client secrets in the sample.
 */
class DeepLinkResolver(
    private val config: DeepLinkConfig = DeepLinkConfig,
    private val client: OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(config.httpTimeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(config.httpTimeoutMs, TimeUnit.MILLISECONDS)
            .callTimeout(config.httpTimeoutMs + 5_000, TimeUnit.MILLISECONDS)
            .build(),
) {
    fun resolve(shortCode: String, hostHint: String? = null): ResolveResult {
        val url =
            buildString {
                append(config.apiBaseUrl)
                append("/deep-links/resolve/")
                append(shortCode)
                if (!hostHint.isNullOrBlank()) {
                    append("?host=")
                    append(java.net.URLEncoder.encode(hostHint, Charsets.UTF_8.name()))
                }
            }
        return try {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (config.debugLogging) {
                    Log.d(TAG, "resolve $shortCode → HTTP ${response.code}")
                }
                if (!response.isSuccessful) {
                    val msg = parseErrorMessage(body, response.code)
                    return ResolveResult.Failure(msg, response.code)
                }
                ResolveResult.Success(DeepLinkPayloadParser.parseEnvelope(body))
            }
        } catch (e: Exception) {
            if (config.debugLogging) Log.e(TAG, "resolve failed", e)
            ResolveResult.Failure(e.message ?: "Network error")
        }
    }

    fun postAppOpen(linkCode: String) {
        if (!config.postAppOpen) return
        try {
            val json = """{"linkCode":"$linkCode","platform":"android"}"""
            val request =
                Request.Builder()
                    .url("${config.apiBaseUrl}/events/app-open")
                    .post(json.toRequestBody("application/json".toMediaType()))
                    .build()
            client.newCall(request).execute().close()
        } catch (e: Exception) {
            if (config.debugLogging) Log.w(TAG, "app-open failed: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "DeepLinkResolver"

        /**
         * API errors are `{ "success": false, "error": { "code", "message" }, "requestId" }`.
         * Older clients used `optString("error")` which yields "" for nested objects.
         */
        fun parseErrorMessage(body: String, httpCode: Int): String {
            val fallback = body.ifBlank { "HTTP $httpCode" }
            return runCatching {
                val root = org.json.JSONObject(body)
                val err = root.opt("error")
                when (err) {
                    is org.json.JSONObject -> {
                        val code = err.optString("code", "")
                        val message = err.optString("message", "").ifBlank { null }
                        when {
                            code == "LINK_EXPIRED" || httpCode == 410 ->
                                message ?: "This link has expired"
                            message != null -> message
                            else -> fallback
                        }
                    }
                    is String -> err.ifBlank { fallback }
                    else -> root.optString("message", fallback).ifBlank { fallback }
                }
            }.getOrDefault(fallback)
        }
    }
}
