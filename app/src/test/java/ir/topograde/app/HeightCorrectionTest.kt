package ir.topograde.app

import ir.topograde.app.survey.HeightCorrection
import ir.topograde.app.survey.HeightInputType
import ir.topograde.app.survey.HeightSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class HeightCorrectionTest {
    @Test fun ggaMslSubtractsPoleHeight() {
        assertEquals(98.0,HeightCorrection.groundOrthometricHeight(100.0,HeightSettings(2.0,HeightInputType.GGA_ORTHOMETRIC,0.0)),1e-9)
    }
    @Test fun ellipsoidSubtractsGeoidAndPole() {
        assertEquals(68.0,HeightCorrection.groundOrthometricHeight(100.0,HeightSettings(2.0,HeightInputType.ELLIPSOIDAL,30.0)),1e-9)
    }
}