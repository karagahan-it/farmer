package ir.topograde.app

import ir.topograde.app.sensors.SensorCalibrationEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class SensorCalibrationEngineTest {
    @Test fun twoReferencePointsCreateCorrectLinearCalibration() {
        val calibration = SensorCalibrationEngine.fromTwoReferencePoints(1.0, 0.0, 3.0, 100.0)
        assertEquals(50.0, calibration.scale, 1e-9)
        assertEquals(-50.0, calibration.offset, 1e-9)
        assertEquals(25.0, calibration.convert(1.5), 1e-9)
    }

    @Test(expected = IllegalArgumentException::class)
    fun identicalRawReferencesAreRejected() {
        SensorCalibrationEngine.fromTwoReferencePoints(2.0, 0.0, 2.0, 10.0)
    }
}
