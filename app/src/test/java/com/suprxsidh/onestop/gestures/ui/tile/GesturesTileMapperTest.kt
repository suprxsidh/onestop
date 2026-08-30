package com.suprxsidh.onestop.gestures.ui.tile

import com.suprxsidh.onestop.gestures.model.GestureSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class GesturesTileMapperTest {

    @Test
    fun `default settings map to enabled with zero suppressed apps`() {
        val state = GesturesTileMapper.toGesturesTileState(GestureSettings())
        assertEquals(true, state.enabled)
        assertEquals(0, state.suppressedAppCount)
    }

    @Test
    fun `disabled settings map to disabled state`() {
        val state = GesturesTileMapper.toGesturesTileState(GestureSettings(enabled = false))
        assertEquals(false, state.enabled)
    }

    @Test
    fun `blocklist size maps to suppressedAppCount`() {
        val state = GesturesTileMapper.toGesturesTileState(
            GestureSettings(blockedPackages = setOf("com.supermoney.app", "com.other.app"))
        )
        assertEquals(2, state.suppressedAppCount)
    }
}
