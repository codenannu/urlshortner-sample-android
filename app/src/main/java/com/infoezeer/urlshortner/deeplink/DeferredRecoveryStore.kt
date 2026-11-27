package com.infoezeer.urlshortner.deeplink

import android.content.Context

/**
 * Persist pending deferred token + install id for claim retries.
 * Terminal states avoid re-querying Install Referrer on every launch.
 */
class DeferredRecoveryStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getOrCreateInstallId(): String {
        val existing = prefs.getString(KEY_INSTALL_ID, null)
        if (!existing.isNullOrBlank()) return existing
        val id = java.util.UUID.randomUUID().toString()
        prefs.edit().putString(KEY_INSTALL_ID, id).apply()
        return id
    }

    fun pendingToken(): String? = prefs.getString(KEY_PENDING_TOKEN, null)

    fun savePendingToken(token: String) {
        prefs.edit().putString(KEY_PENDING_TOKEN, token).apply()
    }

    fun clearPendingToken() {
        prefs.edit().remove(KEY_PENDING_TOKEN).apply()
    }

    fun referrerTerminalState(): String? = prefs.getString(KEY_REFERRER_STATE, null)

    fun setReferrerTerminalState(state: String) {
        prefs.edit().putString(KEY_REFERRER_STATE, state).apply()
    }

    fun claimSucceeded(): Boolean = prefs.getBoolean(KEY_CLAIM_OK, false)

    fun markClaimSucceeded() {
        prefs
            .edit()
            .putBoolean(KEY_CLAIM_OK, true)
            .remove(KEY_PENDING_TOKEN)
            .putString(KEY_REFERRER_STATE, STATE_CLAIMED)
            .apply()
    }

    companion object {
        private const val PREFS = "deferred_recovery"
        private const val KEY_INSTALL_ID = "install_id"
        private const val KEY_PENDING_TOKEN = "pending_token"
        private const val KEY_REFERRER_STATE = "referrer_state"
        private const val KEY_CLAIM_OK = "claim_ok"

        const val STATE_CLAIMED = "claimed"
        const val STATE_EMPTY = "empty_referrer"
        const val STATE_UNSUPPORTED = "service_unavailable"
        const val STATE_INVALID = "invalid_referrer"
    }
}
