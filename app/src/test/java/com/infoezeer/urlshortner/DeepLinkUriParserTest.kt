package com.infoezeer.urlshortner

import com.infoezeer.urlshortner.deeplink.DeepLinkParseResult
import com.infoezeer.urlshortner.deeplink.DeepLinkPayloadParser
import com.infoezeer.urlshortner.deeplink.DeepLinkUriParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepLinkUriParserTest {
    private val hosts = setOf("short.example.com", "go.example.com")

    @Test
    fun extractsCodeFromRootPath() {
        val result = DeepLinkUriParser.parseUrl("https://short.example.com/abc123", hosts)
        assertTrue(result is DeepLinkParseResult.Ok)
        result as DeepLinkParseResult.Ok
        assertEquals("abc123", result.value.shortCode)
        assertEquals("short.example.com", result.value.host)
    }

    @Test
    fun extractsCodeFromDeeplinkPath() {
        val result = DeepLinkUriParser.parseUrl("https://short.example.com/deeplink/xyz_9", hosts)
        assertTrue(result is DeepLinkParseResult.Ok)
        assertEquals("xyz_9", (result as DeepLinkParseResult.Ok).value.shortCode)
    }

    @Test
    fun rejectsDisallowedHost() {
        val result = DeepLinkUriParser.parseUrl("https://evil.example/abc123", hosts)
        assertTrue(result is DeepLinkParseResult.Err)
    }

    @Test
    fun rejectsHttpScheme() {
        val result = DeepLinkUriParser.parseUrl("http://short.example.com/abc123", hosts)
        assertTrue(result is DeepLinkParseResult.Err)
    }

    @Test
    fun rejectsShortCode() {
        val result = DeepLinkUriParser.parseUrl("https://short.example.com/ab", hosts)
        assertTrue(result is DeepLinkParseResult.Err)
    }

    @Test
    fun detectsDiagramModeQuery() {
        val result = DeepLinkUriParser.parseUrl("https://short.example.com/abc123?d=1", hosts)
        assertTrue(result is DeepLinkParseResult.Ok)
        assertTrue((result as DeepLinkParseResult.Ok).value.diagramMode)
    }

    @Test
    fun rejectsNonExactDiagramFlag() {
        val result = DeepLinkUriParser.parseUrl("https://short.example.com/abc123?d=true", hosts)
        assertTrue(result is DeepLinkParseResult.Ok)
        assertTrue(!(result as DeepLinkParseResult.Ok).value.diagramMode)
    }

    @Test
    fun firstDWinsForDuplicates() {
        assertTrue(DeepLinkUriParser.isDiagramMode("d=1&d=0"))
        assertTrue(!DeepLinkUriParser.isDiagramMode("d=0&d=1"))
    }
}

class DeepLinkPayloadParserTest {
    @Test
    fun unwrapsSuccessEnvelope() {
        val body =
            """
            {"success":true,"data":{"version":1,"linkCode":"abc123","type":"deep_link",
            "screen":"course_detail","params":{"courseId":"1"},"metadata":{},"expiresAt":null,
            "requiresAuth":false,"platformOverrides":{},"fallbacks":{"android":"https://play.google.com"}}}
            """.trimIndent()
        val payload = DeepLinkPayloadParser.parseEnvelope(body)
        assertEquals("abc123", payload.linkCode)
        assertEquals("course_detail", payload.screen)
        assertTrue(payload.paramsJson.contains("courseId"))
        assertTrue(payload.fallbacksJson!!.contains("play.google.com"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsFailedEnvelope() {
        val body = """{"success":false,"error":"Link not active"}"""
        DeepLinkPayloadParser.parseEnvelope(body)
    }
}

class DeepLinkResolverErrorParseTest {
    @Test
    fun parsesNestedLinkExpired() {
        val body =
            """
            {"success":false,"error":{"code":"LINK_EXPIRED","message":"This link has expired"},"requestId":"r1"}
            """.trimIndent()
        assertEquals(
            "This link has expired",
            com.infoezeer.urlshortner.deeplink.DeepLinkResolver.parseErrorMessage(body, 410),
        )
    }

    @Test
    fun fallsBackForPlainErrorString() {
        val body = """{"success":false,"error":"boom"}"""
        assertEquals(
            "boom",
            com.infoezeer.urlshortner.deeplink.DeepLinkResolver.parseErrorMessage(body, 400),
        )
    }
}
