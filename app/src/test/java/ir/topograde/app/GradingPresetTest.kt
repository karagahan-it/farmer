package ir.topograde.app

import ir.topograde.app.grading.GradingMode
import ir.topograde.app.grading.GradingPreset
import org.junit.Assert.assertEquals
import org.junit.Test

class GradingPresetTest {
    @Test fun dualSlopeUsesAzimuthAxes() {
        val s=GradingPreset(GradingMode.DUAL_SLOPE,0.0,0.0,100.0,2.0,1.0,0.0).toSurface()
        assertEquals(102.0,s.designZ(0.0,100.0),1e-9)
        assertEquals(101.0,s.designZ(100.0,0.0),1e-9)
    }
}