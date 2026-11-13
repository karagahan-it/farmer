package ir.topograde.app

import ir.topograde.app.geo.CalibrationControl
import ir.topograde.app.geo.LocalCalibration
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalCalibrationTest {
    @Test fun twoPointSimilarityRoundTrip() {
        val t=LocalCalibration.fromTwoControls(CalibrationControl(100.0,200.0,0.0,0.0),CalibrationControl(110.0,200.0,20.0,0.0))
        val p=t.apply(105.0,205.0)
        val inv=t.inverse(p.first,p.second)
        assertEquals(105.0,inv.first,1e-8); assertEquals(205.0,inv.second,1e-8)
    }
}