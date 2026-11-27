package com.infoezeer.urlshortner.deeplink

import android.content.Context
import android.os.RemoteException
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse
import com.android.installreferrer.api.InstallReferrerStateListener
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * First-party Play Install Referrer recovery (ADR-019).
 * Does not block forever — connection failures return a typed result.
 */
class InstallReferrerDeferredRecovery(
    private val context: Context,
    private val store: DeferredRecoveryStore = DeferredRecoveryStore(context),
) {
    sealed class Result {
        data class Token(val token: String) : Result()

        data class Terminal(val state: String, val message: String) : Result()

        data class Retryable(val message: String) : Result()
    }

    suspend fun recover(): Result {
        val terminal = store.referrerTerminalState()
        if (terminal == DeferredRecoveryStore.STATE_CLAIMED || store.claimSucceeded()) {
            return Result.Terminal(terminal ?: DeferredRecoveryStore.STATE_CLAIMED, "Already claimed")
        }
        store.pendingToken()?.let { return Result.Token(it) }

        if (terminal == DeferredRecoveryStore.STATE_EMPTY ||
            terminal == DeferredRecoveryStore.STATE_INVALID ||
            terminal == DeferredRecoveryStore.STATE_UNSUPPORTED
        ) {
            return Result.Terminal(terminal, "Prior referrer terminal state")
        }

        return fetchFromPlay()
    }

    private suspend fun fetchFromPlay(): Result =
        suspendCancellableCoroutine { cont ->
            val client = InstallReferrerClient.newBuilder(context).build()
            cont.invokeOnCancellation {
                runCatching { client.endConnection() }
            }
            try {
                client.startConnection(
                    object : InstallReferrerStateListener {
                        override fun onInstallReferrerSetupFinished(responseCode: Int) {
                            try {
                                when (responseCode) {
                                    InstallReferrerResponse.OK -> {
                                        val details = client.installReferrer
                                        val raw = details.installReferrer
                                        Log.i(
                                            TAG,
                                            "referrer received clickTs=${details.referrerClickTimestampSeconds} " +
                                                "installTs=${details.installBeginTimestampSeconds} " +
                                                "version=${details.installVersion}",
                                        )
                                        val token = InstallReferrerParser.parseDeferredToken(raw)
                                        if (token == null) {
                                            store.setReferrerTerminalState(DeferredRecoveryStore.STATE_EMPTY)
                                            cont.resume(
                                                Result.Terminal(
                                                    DeferredRecoveryStore.STATE_EMPTY,
                                                    "Empty or non-deferred referrer (sideload / no cw_dl)",
                                                ),
                                            )
                                        } else {
                                            store.savePendingToken(token)
                                            Log.i(TAG, "deferred token ${InstallReferrerParser.redact(token)}")
                                            cont.resume(Result.Token(token))
                                        }
                                    }
                                    InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                                        store.setReferrerTerminalState(DeferredRecoveryStore.STATE_UNSUPPORTED)
                                        cont.resume(
                                            Result.Terminal(
                                                DeferredRecoveryStore.STATE_UNSUPPORTED,
                                                "Install Referrer not supported on this device",
                                            ),
                                        )
                                    }
                                    InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                                        cont.resume(Result.Retryable("Install Referrer service unavailable"))
                                    }
                                    else -> {
                                        cont.resume(Result.Retryable("Install Referrer response=$responseCode"))
                                    }
                                }
                            } catch (e: RemoteException) {
                                cont.resume(Result.Retryable(e.message ?: "RemoteException"))
                            } finally {
                                runCatching { client.endConnection() }
                            }
                        }

                        override fun onInstallReferrerServiceDisconnected() {
                            // no-op; may retry later
                        }
                    },
                )
            } catch (e: Exception) {
                cont.resume(Result.Retryable(e.message ?: "startConnection failed"))
            }
        }

    companion object {
        private const val TAG = "DeferredRecovery"
    }
}
