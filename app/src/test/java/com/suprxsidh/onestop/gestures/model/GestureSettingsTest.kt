package com.suprxsidh.onestop.gestures.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GestureSettingsTest {

    @Test
    fun `default settings are enabled with no mappings and empty blocklist`() {
        val settings = GestureSettings()
        assertEquals(true, settings.enabled)
        assertEquals(GlobalActionType.NONE, settings.actionFor(GestureType.SHAKE))
        assertEquals(GlobalActionType.NONE, settings.actionFor(GestureType.ROTATE))
        assertEquals(emptySet<String>(), settings.blockedPackages)
    }

    @Test
    fun `actionFor returns the mapped action when present`() {
        val settings = GestureSettings(mappings = mapOf(GestureType.SHAKE to GlobalActionType.BACK))
        assertEquals(GlobalActionType.BACK, settings.actionFor(GestureType.SHAKE))
    }

    @Test
    fun `actionFor returns NONE for a gesture type missing from mappings`() {
        val settings = GestureSettings(mappings = emptyMap())
        assertEquals(GlobalActionType.NONE, settings.actionFor(GestureType.ROTATE))
    }
}
