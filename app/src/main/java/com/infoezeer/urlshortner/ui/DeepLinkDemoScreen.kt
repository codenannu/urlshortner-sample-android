package com.infoezeer.urlshortner.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.infoezeer.urlshortner.deeplink.DeepLinkUiState
import com.infoezeer.urlshortner.deeplink.LaunchKind
import com.infoezeer.urlshortner.deeplink.ResolutionStatus

@Composable
fun DeepLinkDemoScreen(
    state: DeepLinkUiState,
    onRetry: () -> Unit,
    onClear: () -> Unit,
    onOpenInspector: () -> Unit = {},
    onOpenDestination: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Shortener Android Sample", style = MaterialTheme.typography.headlineSmall)
        Text(
            "DEEP_LINK: App Links → resolve API → show screen/params. " +
                "STANDARD: classic short URL — no app payload; sample opens destinationUrl in the browser. " +
                "?d=1 opens Flow Inspector. Deferred install-referrer is Not supported.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        StatusCard(state)

        if (state.resolution == ResolutionStatus.Loading) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        if (state.launchKind == LaunchKind.DiagramInspector) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Flow Inspector", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "This App Link requested diagram mode (exact d=1). " +
                            "The public inspector opens in the system browser — " +
                            "App Links would otherwise keep the user inside the app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Mono("Inspector URL", state.inspectorUrl)
                    Button(
                        onClick = onOpenInspector,
                        enabled = !state.inspectorUrl.isNullOrBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Open Flow Inspector")
                    }
                }
            }
        }

        PayloadCard(state)
        DebugCard(state)

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = {
                    state.incomingUrl?.let { copy(context, "Incoming URL", it) }
                },
                enabled = !state.incomingUrl.isNullOrBlank(),
            ) { Text("Copy URL") }
            OutlinedButton(
                onClick = {
                    state.payloadJson?.let { copy(context, "Payload JSON", it) }
                },
                enabled = !state.payloadJson.isNullOrBlank(),
            ) { Text("Copy JSON") }
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onRetry,
                enabled =
                    state.launchKind == LaunchKind.DeepLink &&
                        !state.shortCode.isNullOrBlank() &&
                        state.resolution != ResolutionStatus.Loading,
            ) { Text("Retry") }
            OutlinedButton(
                onClick = onOpenDestination,
                enabled = !state.openBrowserUrl.isNullOrBlank() || !state.incomingUrl.isNullOrBlank(),
            ) { Text("Open destination") }
            OutlinedButton(onClick = onClear) { Text("Clear") }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatusCard(state: DeepLinkUiState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Application status", style = MaterialTheme.typography.titleMedium)
            Text(
                when (state.launchKind) {
                    LaunchKind.Normal -> "Normal launch (no deep link)"
                    LaunchKind.DeepLink -> "Deep link opened"
                    LaunchKind.DiagramInspector -> "Flow Inspector (d=1) — open in browser"
                },
            )
            Text("Incoming URL: ${state.incomingUrl ?: "—"}", fontFamily = FontFamily.Monospace)
            Text(
                "Resolution: ${state.resolution.name}",
                color =
                    when (state.resolution) {
                        ResolutionStatus.Success -> MaterialTheme.colorScheme.primary
                        ResolutionStatus.Error -> MaterialTheme.colorScheme.error
                        ResolutionStatus.Idle, ResolutionStatus.Loading -> MaterialTheme.colorScheme.onSurface
                    },
            )
        }
    }
}

@Composable
private fun PayloadCard(state: DeepLinkUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Payload", style = MaterialTheme.typography.titleMedium)
            Mono("Short code", state.shortCode)
            Mono("Screen", state.screen)
            Mono("Requires auth", state.requiresAuth.toString().takeIf { state.payloadJson != null })
            Text("Parameters", style = MaterialTheme.typography.labelLarge)
            Text(state.paramsJson ?: "—", fontFamily = FontFamily.Monospace)
            Text("Metadata / custom", style = MaterialTheme.typography.labelLarge)
            Text(state.metadataJson ?: "—", fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun DebugCard(state: DeepLinkUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Debug", style = MaterialTheme.typography.titleMedium)
            Mono("Note", state.rawApiNote)
            if (!state.errorMessage.isNullOrBlank()) {
                Text(
                    state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Text("API response", style = MaterialTheme.typography.labelLarge)
            Text(state.payloadJson ?: "—", fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun Mono(label: String, value: String?) {
    Text("$label: ${value ?: "—"}", fontFamily = FontFamily.Monospace)
}

private fun copy(context: Context, label: String, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
}
