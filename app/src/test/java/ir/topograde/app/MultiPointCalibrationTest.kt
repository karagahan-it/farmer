package ir.topograde.app

import ir.topograde.app.geo.CalibrationControl
import ir.topograde.app.geo.MultiPointCalibration
import org.junit.Assert.assertEquals
import org.junit.Test

class MultiPointCalibrationTest {
    @Test fun solvesSimilarity() {
        val c=listOf(
            CalibrationControl(0.0,0.0,100.0,200.0),
            CalibrationControl(10.0,0.0,120.0,200.0),
            CalibrationControl(0.0,10.0,100.0,220.0)
        )
        val s=MultiPointCalibration.solve(c)
        assertEquals(2.0,s.transform.scale,1e-9)
        assertEquals(0.0,s.rms,1e-9)
    }
}