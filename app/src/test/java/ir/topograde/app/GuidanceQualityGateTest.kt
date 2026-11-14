package ir.topograde.app

import ir.topograde.app.gnss.*
import ir.topograde.app.guidance.*
import org.junit.Assert.assertEquals
import org.junit.Test

class GuidanceQualityGateTest {
    @Test fun blocksFloat(){
        val s=GuidanceQualityGate.evaluate(GnssFix(1.0,2.0,3.0,12,0.8,FixType.RTK_FLOAT),GnssQuality())
        assertEquals(GuidanceQualityLevel.BLOCKED,s.level)
    }
    @Test fun cautionsOldCorrections(){
        val s=GuidanceQualityGate.evaluate(GnssFix(1.0,2.0,3.0,12,0.8,FixType.RTK_FIXED,correctionAgeSeconds=7.0),GnssQuality())
        assertEquals(GuidanceQualityLevel.CAUTION,s.level)
    }
}