package ir.topograde.app

import ir.topograde.app.guidance.*
import org.junit.Assert.assertEquals
import org.junit.Test

class DualGnssHeadingTest {
    @Test fun northAndEastBaselines(){
        assertEquals(0.0,DualGnssHeading.headingDeg(GnssAntennaPoint(0.0,0.0),GnssAntennaPoint(0.0,2.0)),1e-9)
        assertEquals(90.0,DualGnssHeading.headingDeg(GnssAntennaPoint(0.0,0.0),GnssAntennaPoint(2.0,0.0)),1e-9)
    }
}