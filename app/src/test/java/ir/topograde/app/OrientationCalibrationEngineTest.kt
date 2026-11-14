package ir.topograde.app

import ir.topograde.app.guidance.Orientation
import ir.topograde.app.guidance.OrientationCalibrationEngine
import ir.topograde.app.guidance.MountingCalibration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OrientationCalibrationEngineTest {
    @Test fun stableSamplesProduceZeroOffsets() {
        val samples = List(80) { Orientation(20.0, 1.0 + (it % 2) * 0.01, -0.5 + (it % 2) * 0.01) }
        val result = OrientationCalibrationEngine.analyze(samples)!!
        assertTrue(result.stable)
        val calibrated = OrientationCalibrationEngine.applyAsMountingZero(MountingCalibration(5.0, 0.0, 0.0), result)
        assertEquals(5.0, calibrated.headingMountOffsetDeg, 1e-9)
        assertEquals(result.meanRollDeg, calibrated.rollMountOffsetDeg, 1e-9)
        assertEquals(result.meanPitchDeg, calibrated.pitchMountOffsetDeg, 1e-9)
    }

    @Test fun movingMachineSamplesAreRejectedAsUnstable() {
        val samples = List(80) { Orientation(0.0, if (it % 2 == 0) -2.0 else 2.0, 0.0) }
        assertFalse(OrientationCalibrationEngine.analyze(samples)!!.stable)
    }

    @Test fun tooFewSamplesCannotCalibrate() {
        assertNull(OrientationCalibrationEngine.analyze(List(10) { Orientation() }))
    }
}
