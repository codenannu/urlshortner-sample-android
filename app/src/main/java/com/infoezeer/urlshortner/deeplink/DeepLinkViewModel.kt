package com.infoezeer.urlshortner.deeplink

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class LaunchKind {
    Normal,
    DeepLink,
    /** Public Flow Inspector — open in external browser. */
    DiagramInspector,
}

enum class ResolutionStatus {
    Idle,
    Loading,
    Success,
    Error,
}

data class DeepLinkUiState(
    val launchKind: LaunchKind = LaunchKind.Normal,
    val incomingUrl: String? = null,
    val shortCode: String? = null,
    val resolution: ResolutionStatus = ResolutionStatus.Idle,
    val screen: String? = null,
    val paramsJson: String? = null,
    val metadataJson: String? = null,
    val requiresAuth: Boolean = false,
    val payloadJson: String? = null,
    val errorMessage: String? = null,
    val rawApiNote: String? = null,
    /** Retained public Flow Inspector URL when launchKind is DiagramInspector. */
    val inspectorUrl: String? = null,
    /** One-shot: Activity opens this URL in a browser/Custom Tab, then calls consumeBrowserOpen(). */
    val openBrowserUrl: String? = null,
    /**
     * When true with [openBrowserUrl], Activity should [android.app.Activity.finish] after opening
     * so STANDARD WhatsApp/App Link taps leave the app and stay in the browser.
     */
    val finishAfterBrowserOpen: Boolean = false,
)

class DeepLinkViewModel(
    private val parser: DeepLinkUriParser = DeepLinkUriParser,
    private val resolver: DeepLinkResolver = DeepLinkResolver(),
    private val config: DeepLinkConfig = DeepLinkConfig,
) : ViewModel() {
    private val _state = MutableStateFlow(DeepLinkUiState())
    val state: StateFlow<DeepLinkUiState> = _state.asStateFlow()

    /** Prevents duplicate processing when the same Intent is delivered more than once. */
    private var lastHandledUri: String? = null

    fun onNewIntent(intent: Intent?) {
        handleIntent(intent, force = false)
    }

    fun handleIntent(intent: Intent?, force: Boolean = false) {
        val data: Uri? = intent?.data
        if (data == null) {
            if (_state.value.launchKind == LaunchKind.Normal && _state.value.incomingUrl == null) {
                _state.update {
                    it.copy(
                        launchKind = LaunchKind.Normal,
                        rawApiNote = "Opened normally — waiting for an App Link.",
                    )
                }
            }
            return
        }
        val key = data.toString()
        if (!force && key == lastHandledUri && _state.value.resolution == ResolutionStatus.Success) {
            return
        }
        lastHandledUri = key

        when (val parsed = parser.parse(data, config.allowedHosts)) {
            is DeepLinkParseResult.Err -> {
                _state.update {
                    it.copy(
                        launchKind = LaunchKind.DeepLink,
                        incomingUrl = key,
                        shortCode = null,
                        resolution = ResolutionStatus.Error,
                        errorMessage = parsed.message,
                        payloadJson = null,
                    )
                }
            }
            is DeepLinkParseResult.Ok -> {
                if (parsed.value.diagramMode) {
                    val inspector =
                        config.publicDiagramInspectorUrl(parsed.value.shortCode, parsed.value.host)
                    _state.update {
                        it.copy(
                            launchKind = LaunchKind.DiagramInspector,
                            incomingUrl = parsed.value.incomingUrl,
                            shortCode = parsed.value.shortCode,
                            resolution = ResolutionStatus.Idle,
                            errorMessage = null,
                            payloadJson = null,
                            inspectorUrl = inspector,
                            rawApiNote =
                                "Diagram mode (d=1): opening public Flow Inspector in browser. " +
                                    "App Links may otherwise swallow ?d=1 — use Open Flow Inspector if needed.",
                            openBrowserUrl = inspector,
                        )
                    }
                    return
                }
                _state.update {
                    it.copy(
                        launchKind = LaunchKind.DeepLink,
                        incomingUrl = parsed.value.incomingUrl,
                        shortCode = parsed.value.shortCode,
                        resolution = ResolutionStatus.Loading,
                        errorMessage = null,
                        screen = null,
                        paramsJson = null,
                        metadataJson = null,
                        payloadJson = null,
                        inspectorUrl = null,
                        openBrowserUrl = null,
                    )
                }
                resolve(parsed.value.shortCode, parsed.value.host)
            }
        }
    }

    fun consumeBrowserOpen() {
        _state.update { it.copy(openBrowserUrl = null, finishAfterBrowserOpen = false) }
    }

    /** Re-open the public Flow Inspector (diagram mode) in the system browser. */
    fun reopenInspector() {
        val url = _state.value.inspectorUrl ?: return
        _state.update { it.copy(openBrowserUrl = url) }
    }

    /** Open STANDARD destination (or last openBrowserUrl) in the system browser. */
    fun openDestinationInBrowser() {
        val url =
            _state.value.openBrowserUrl
                ?: runCatching {
                    val raw = _state.value.payloadJson ?: return
                    org.json.JSONObject(raw).optString("destinationUrl").ifBlank { null }
                }.getOrNull()
                ?: return
        _state.update { it.copy(openBrowserUrl = url) }
    }

    fun retry() {
        val code = _state.value.shortCode ?: return
        val host =
            _state.value.incomingUrl
                ?.let { Uri.parse(it).host }
        _state.update { it.copy(resolution = ResolutionStatus.Loading, errorMessage = null) }
        resolve(code, host)
    }

    fun clear() {
        lastHandledUri = null
        _state.value =
            DeepLinkUiState(
                launchKind = LaunchKind.Normal,
                rawApiNote = "Payload cleared.",
            )
    }

    private fun resolve(code: String, host: String?) {
        viewModelScope.launch {
            val result =
                withContext(Dispatchers.IO) {
                    resolver.resolve(code, host)
                }
            when (result) {
                is ResolveResult.Success -> {
                    val p = result.payload
                    if (p.isStandardRedirect) {
                        val dest = p.destinationUrl
                        _state.update {
                            it.copy(
                                resolution = if (dest.isNullOrBlank()) ResolutionStatus.Error else ResolutionStatus.Success,
                                screen = p.screen,
                                paramsJson = p.paramsJson,
                                metadataJson = p.metadataJson,
                                requiresAuth = false,
                                payloadJson = p.toPrettyJson(),
                                errorMessage =
                                    if (dest.isNullOrBlank()) {
                                        "STANDARD short link has no destinationUrl"
                                    } else {
                                        null
                                    },
                                rawApiNote =
                                    "STANDARD short link (WhatsApp/App Links may open the app first). " +
                                        "Handing off to browser → destinationUrl, then closing the activity. " +
                                        "Use DEEP_LINK for in-app screen/params.",
                                openBrowserUrl = dest,
                                finishAfterBrowserOpen = !dest.isNullOrBlank(),
                            )
                        }
                        return@launch
                    }
                    withContext(Dispatchers.IO) { resolver.postAppOpen(p.linkCode) }
                    _state.update {
                        it.copy(
                            resolution = ResolutionStatus.Success,
                            screen = p.screen,
                            paramsJson = p.paramsJson,
                            metadataJson = p.metadataJson,
                            requiresAuth = p.requiresAuth,
                            payloadJson = p.toPrettyJson(),
                            errorMessage = null,
                            rawApiNote = "DEEP_LINK resolved via GET /deep-links/resolve/$code",
                        )
                    }
                }
                is ResolveResult.Failure -> {
                    val expired = result.httpCode == 410 ||
                        result.message.contains("expired", ignoreCase = true)
                    val display =
                        if (expired) {
                            "This link has expired"
                        } else {
                            val hint =
                                if (result.httpCode == 404) {
                                    " Tip: STANDARD short links have no deep-link payload — create type DEEP_LINK for App Links demos, " +
                                        "or open the short URL in a browser without the app catching it."
                                } else {
                                    ""
                                }
                            result.message + (result.httpCode?.let { c -> " (HTTP $c)" } ?: "") + hint
                        }
                    _state.update {
                        it.copy(
                            resolution = ResolutionStatus.Error,
                            errorMessage = display,
                            rawApiNote = if (expired) "LINK_EXPIRED (HTTP 410)" else "Resolve failed",
                        )
                    }
                }
            }
        }
    }
}
