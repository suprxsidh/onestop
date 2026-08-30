package com.suprxsidh.onestop.battery.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DesignCapacityResolverTest {
    @Test
    fun `prefers cached value over all else`() {
        assertEquals(4000.0, DesignCapacityResolver.resolve(4000.0, 4500.0, 4200.0)!!, 0.001)
    }

    @Test
    fun `falls back to reflected value when no cache`() {
        assertEquals(4500.0, DesignCapacityResolver.resolve(null, 4500.0, 4200.0)!!, 0.001)
    }

    @Test
    fun `falls back to derived value when no cache or reflection`() {
        assertEquals(4200.0, DesignCapacityResolver.resolve(null, null, 4200.0)!!, 0.001)
    }

    @Test
    fun `null when nothing available`() {
        assertNull(DesignCapacityResolver.resolve(null, null, null))
    }
}
