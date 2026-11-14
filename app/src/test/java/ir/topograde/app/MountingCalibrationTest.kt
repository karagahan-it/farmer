package ir.topograde.app

import ir.topograde.app.guidance.*
import org.junit.Assert.assertEquals
import org.junit.Test

class MountingCalibrationTest {
    @Test fun subtractsMountOffsets(){
        val o=MountingCalibrationEngine.apply(Orientation(100.0,4.0,-2.0),MountingCalibration(10.0,1.5,-0.5))
        assertEquals(90.0,o.headingDeg,1e-9)
        assertEquals(2.5,o.rollDeg,1e-9)
        assertEquals(-1.5,o.pitchDeg,1e-9)
    }
}