package com.suprxsidh.onestop.battery.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HealthEstimatorTest {
    @Test
    fun `estimates full capacity from charge counter delta`() {
        // 3000 mAh delta over a 60pct-point charge -> 5000 mAh full capacity
        val result = HealthEstimator.estimateFullCapacityMah(
            chargeCounterStartUah = 1_000_000L,
            chargeCounterEndUah = 4_000_000L,
            startPct = 20,
            endPct = 80
        )
        assertEquals(5000.0, result!!, 0.001)
    }

    @Test
    fun `returns null when pct delta is zero or negative`() {
        assertNull(HealthEstimator.estimateFullCapacityMah(1_000_000L, 1_500_000L, 50, 50))
        assertNull(HealthEstimator.estimateFullCapacityMah(1_000_000L, 1_500_000L, 80, 20))
    }

    @Test
    fun `health pct is estimated over design capacity`() {
        assertEquals(93.33, HealthEstimator.healthPct(4200.0, 4500.0), 0.01)
    }

    @Test
    fun `rolling median smooths noisy sessions`() {
        val values = listOf(90.0, 91.0, 60.0, 92.0, 91.5)
        // last 3 values: 60.0, 92.0, 91.5 -> sorted 60.0, 91.5, 92.0 -> median 91.5
        assertEquals(91.5, HealthEstimator.rollingMedian(values, window = 3), 0.001)
    }

    @Test
    fun `rolling median averages the two middle values for an even window`() {
        val values = listOf(90.0, 91.0, 60.0, 92.0, 91.5)
        // last 4 values: 91.0, 60.0, 92.0, 91.5 -> sorted 60.0, 91.0, 91.5, 92.0 -> average of middle two = 91.25
        assertEquals(91.25, HealthEstimator.rollingMedian(values, window = 4), 0.001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects a non-positive window`() {
        HealthEstimator.rollingMedian(listOf(1.0, 2.0), window = 0)
    }
}
