package ir.topograde.app

import ir.topograde.app.guidance.Orientation
import ir.topograde.app.guidance.OrientationFilter
import ir.topograde.app.guidance.OrientationFilterConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class OrientationFilterTest {
    @Test fun smoothsRollPitchAndWrapsHeading(){
        val f=OrientationFilter(OrientationFilterConfig(0.5,0.5))
        f.update(Orientation(350.0,0.0,0.0))
        val o=f.update(Orientation(10.0,10.0,-10.0))
        assertEquals(0.0,o.headingDeg,1e-9)
        assertEquals(5.0,o.rollDeg,1e-9)
        assertEquals(-5.0,o.pitchDeg,1e-9)
    }
}