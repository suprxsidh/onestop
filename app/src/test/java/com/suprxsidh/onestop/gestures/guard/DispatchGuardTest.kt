package com.suprxsidh.onestop.gestures.guard

import com.suprxsidh.onestop.gestures.model.GestureSettings
import com.suprxsidh.onestop.gestures.model.GestureType
import com.suprxsidh.onestop.gestures.model.GlobalActionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DispatchGuardTest {

    private val mapped = GestureSettings(
        enabled = true,
        mappings = mapOf(GestureType.SHAKE to GlobalActionType.BACK),
        blockedPackages = setOf("com.supermoney.app")
    )

    @Test
    fun `disabled settings always resolve to null`() {
        val disabled = mapped.copy(enabled = false)
        val result = DispatchGuard.resolveAction("com.other.app", disabled, GestureType.SHAKE)
        assertNull(result)
    }

    @Test
    fun `blocked foreground package resolves to null even when mapped`() {
        val result = DispatchGuard.resolveAction("com.supermoney.app", mapped, GestureType.SHAKE)
        assertNull(result)
    }

    @Test
    fun `unmapped gesture (NONE) resolves to null`() {
        val result = DispatchGuard.resolveAction("com.other.app", mapped, GestureType.ROTATE)
        assertNull(result)
    }

    @Test
    fun `enabled, unblocked, mapped gesture resolves to the mapped action`() {
        val result = DispatchGuard.resolveAction("com.other.app", mapped, GestureType.SHAKE)
        assertEquals(GlobalActionType.BACK, result)
    }

    @Test
    fun `unknown foreground package (null) does not match the blocklist`() {
        // Fail-open on a transient/unknown foreground state (e.g. before the
        // first window-state-changed event has ever fired) rather than
        // silently dropping every gesture until one arrives.
        val result = DispatchGuard.resolveAction(null, mapped, GestureType.SHAKE)
        assertEquals(GlobalActionType.BACK, result)
    }
}
