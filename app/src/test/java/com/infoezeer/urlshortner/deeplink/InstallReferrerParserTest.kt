package com.infoezeer.urlshortner.deeplink

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InstallReferrerParserTest {
    @Test
    fun parsesCwDlToken() {
        assertEquals(
            "opaqueTokenValue",
            InstallReferrerParser.parseDeferredToken("cw_dl=opaqueTokenValue&v=1"),
        )
    }

    @Test
    fun rejectsMissingToken() {
        assertNull(InstallReferrerParser.parseDeferredToken("utm_source=google-play"))
        assertNull(InstallReferrerParser.parseDeferredToken(""))
        assertNull(InstallReferrerParser.parseDeferredToken(null))
    }

    @Test
    fun rejectsWrongVersion() {
        assertNull(InstallReferrerParser.parseDeferredToken("cw_dl=abc&v=2"))
    }

    @Test
    fun redactsToken() {
        val redacted = InstallReferrerParser.redact("abcdefghijklmnop")
        assertTrue(redacted.contains("…"))
        assertTrue(!redacted.contains("abcdefghijklmnop"))
    }
}
