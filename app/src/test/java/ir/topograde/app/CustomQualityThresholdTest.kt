package ir.topograde.app

import ir.topograde.app.gnss.*
import ir.topograde.app.guidance.*
import org.junit.Assert.assertEquals
import org.junit.Test

class CustomQualityThresholdTest {
    @Test fun customSatelliteThresholdTriggersCaution(){
        val fix=GnssFix(1.0,2.0,3.0,10,0.8,FixType.RTK_FIXED)
        val q=GuidanceQualityGate.evaluate(fix,GnssQuality(),GuidanceQualityThresholds(minSatellites=12))
        assertEquals(GuidanceQualityLevel.CAUTION,q.level)
    }
}