package ir.topograde.app

import ir.topograde.app.gnss.FixType
import ir.topograde.app.gnss.GnssFix
import ir.topograde.app.gnss.GnssQuality
import ir.topograde.app.sensors.SensorHealthEngine
import ir.topograde.app.sensors.SensorHealthLevel
import ir.topograde.app.sensors.SensorId
import org.junit.Assert.assertEquals
import org.junit.Test

class SensorHealthEngineTest {
    private val fix = GnssFix(35.0, 58.0, 100.0, 14, 0.8, FixType.RTK_FIXED, "101010.00", 1.0, "BASE")

    @Test fun goodFreshRtkIsGood() {
        val result = SensorHealthEngine.evaluateGnss(SensorId.PRIMARY_GNSS, "GPS", true, fix, 9000L,
            GnssQuality(1.2, 0.8, 1.5, 0.02, 0.03, 0.02), 10000L)
        assertEquals(SensorHealthLevel.GOOD, result.level)
    }

    @Test fun staleRtkIsBlocked() {
        val result = SensorHealthEngine.evaluateGnss(SensorId.PRIMARY_GNSS, "GPS", true, fix, 6000L,
            GnssQuality(), 10000L)
        assertEquals(SensorHealthLevel.BLOCKED, result.level)
    }

    @Test fun floatRtkIsCaution() {
        val floatFix = fix.copy(fixType = FixType.RTK_FLOAT)
        val result = SensorHealthEngine.evaluateGnss(SensorId.PRIMARY_GNSS, "GPS", true, floatFix, 9500L,
            GnssQuality(1.2, 0.8, 1.5, 0.02, 0.03, 0.02), 10000L)
        assertEquals(SensorHealthLevel.CAUTION, result.level)
    }
}
