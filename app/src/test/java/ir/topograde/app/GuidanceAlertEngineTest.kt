package ir.topograde.app

import ir.topograde.app.guidance.*
import org.junit.Assert.assertTrue
import org.junit.Test

class GuidanceAlertEngineTest {
    @Test fun warnsLargeElevationAndSlope(){
        val q=GuidanceQualityStatus(GuidanceQualityLevel.GOOD,emptyList())
        val a=GuidanceAlertEngine.evaluate(0.20,1.5,q)
        assertTrue(a.any{it.message.contains("Elevation")})
        assertTrue(a.any{it.message.contains("Cross-slope")})
    }
}