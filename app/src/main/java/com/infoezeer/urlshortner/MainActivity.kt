package com.infoezeer.urlshortner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.infoezeer.urlshortner.deeplink.DeepLinkViewModel
import com.infoezeer.urlshortner.ui.DeepLinkDemoScreen
import com.infoezeer.urlshortner.ui.theme.ShortnerAndroidTheme

class MainActivity : ComponentActivity() {
    private val deepLinkViewModel: DeepLinkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        deepLinkViewModel.handleIntent(intent)
        setContent {
            ShortnerAndroidTheme {
                val state by deepLinkViewModel.state.collectAsState()
                LaunchedEffect(state.openBrowserUrl) {
                    val url = state.openBrowserUrl ?: return@LaunchedEffect
                    val finishAfter = state.finishAfterBrowserOpen
                    // Prefer a browsable VIEW so STANDARD destination opens in a browser,
                    // not back into this app via App Links (destination is usually off-host).
                    val view =
                        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            addCategory(Intent.CATEGORY_BROWSABLE)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    runCatching { startActivity(view) }
                    deepLinkViewModel.consumeBrowserOpen()
                    if (finishAfter) {
                        finish()
                    }
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DeepLinkDemoScreen(
                        state = state,
                        onRetry = deepLinkViewModel::retry,
                        onClear = deepLinkViewModel::clear,
                        onOpenInspector = deepLinkViewModel::reopenInspector,
                        onOpenDestination = deepLinkViewModel::openDestinationInBrowser,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkViewModel.onNewIntent(intent)
    }
}
