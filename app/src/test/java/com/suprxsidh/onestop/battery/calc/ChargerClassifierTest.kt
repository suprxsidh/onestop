package com.suprxsidh.onestop.battery.calc

import org.junit.Assert.assertEquals
import org.junit.Test

class ChargerClassifierTest {
    @Test
    fun `zero or negative watts is unknown`() {
        assertEquals(ChargerType.UNKNOWN, ChargerClassifier.classify(0f))
        assertEquals(ChargerType.UNKNOWN, ChargerClassifier.classify(-2f))
    }

    @Test
    fun `low wattage is slow`() {
        assertEquals(ChargerType.SLOW, ChargerClassifier.classify(5f))
    }

    @Test
    fun `mid wattage is standard`() {
        assertEquals(ChargerType.STANDARD, ChargerClassifier.classify(10f))
    }

    @Test
    fun `high wattage is fast`() {
        assertEquals(ChargerType.FAST, ChargerClassifier.classify(18f))
    }

    @Test
    fun `very high wattage is super fast`() {
        assertEquals(ChargerType.SUPER_FAST, ChargerClassifier.classify(30f))
    }

    @Test
    fun `7 watts boundary is standard not slow`() {
        assertEquals(ChargerType.STANDARD, ChargerClassifier.classify(7f))
    }

    @Test
    fun `12 watts boundary is fast not standard`() {
        assertEquals(ChargerType.FAST, ChargerClassifier.classify(12f))
    }

    @Test
    fun `22 watts boundary is super fast not fast`() {
        assertEquals(ChargerType.SUPER_FAST, ChargerClassifier.classify(22f))
    }
}
